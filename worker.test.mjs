import test from "node:test";
import assert from "node:assert/strict";
import worker from "./worker.js";

function createDatabase() {
  const users = new Map();
  return {
    prepare(sql) {
      let values = [];
      return {
        bind(...args) {
          values = args;
          return this;
        },
        async first() {
          if (sql === "SELECT 1") return { ok: 1 };
          const email = values[0];
          if (sql.startsWith("SELECT 1 FROM users")) {
            return users.has(email) ? { found: 1 } : null;
          }
          if (sql.startsWith("SELECT first_name")) return users.get(email) ?? null;
          return null;
        },
        async run() {
          const [id, firstName, lastName, phone, country, dialCode, email, salt, hash] = values;
          if (users.has(email)) throw new Error("UNIQUE constraint failed: users.horus_email");
          users.set(email, {
            id,
            first_name: firstName,
            last_name: lastName,
            phone,
            country,
            dial_code: dialCode,
            horus_email: email,
            password_salt: salt,
            password_hash: hash
          });
          return { success: true };
        }
      };
    }
  };
}

const postJson = (path, body) => new Request(`https://horus.test${path}`, {
  method: "POST",
  headers: { "content-type": "application/json" },
  body: JSON.stringify(body)
});

const validRegistration = {
  firstName: "Test",
  lastName: "User",
  phone: "01012345678",
  country: "Egypt",
  dialCode: "+20",
  emailHandle: "testuser",
  password: "StrongPass1!",
  confirmPassword: "StrongPass1!"
};

test("health endpoint verifies the D1 binding", async () => {
  const response = await worker.fetch(new Request("https://horus.test/api/health"), {
    Horus_maps: createDatabase()
  });
  assert.equal(response.status, 200);
  assert.deepEqual(await response.json(), {
    ok: true,
    service: "horus-maps-api",
    database: true
  });
});

test("email availability rejects invalid handles and checks D1", async () => {
  const db = createDatabase();
  const invalid = await worker.fetch(new Request("https://horus.test/api/email/check?handle=ad%21min"), { Horus_maps: db });
  assert.equal((await invalid.json()).reason, "invalid");

  const available = await worker.fetch(new Request("https://horus.test/api/email/check?handle=testuser"), { Horus_maps: db });
  assert.equal((await available.json()).available, true);
});

test("registration creates an account and reports duplicate handles as conflict", async () => {
  const db = createDatabase();
  const first = await worker.fetch(postJson("/api/auth/register", validRegistration), { Horus_maps: db });
  assert.equal(first.status, 201);
  assert.equal((await first.json()).user.email, "testuser@horus.com");

  const duplicate = await worker.fetch(postJson("/api/auth/register", validRegistration), { Horus_maps: db });
  assert.equal(duplicate.status, 409);
  assert.equal((await duplicate.json()).error, "email_taken");
});

test("login verifies the password and rejects incorrect credentials", async () => {
  const db = createDatabase();
  const registration = await worker.fetch(postJson("/api/auth/register", validRegistration), { Horus_maps: db });
  assert.equal(registration.status, 201);

  const login = await worker.fetch(postJson("/api/auth/login", {
    email: "testuser@horus.com",
    password: validRegistration.password
  }), { Horus_maps: db });
  assert.equal(login.status, 200);
  assert.equal((await login.json()).user.name, "Test User");

  const wrongPassword = await worker.fetch(postJson("/api/auth/login", {
    email: "testuser@horus.com",
    password: "WrongPass1!"
  }), { Horus_maps: db });
  assert.equal(wrongPassword.status, 401);
});

test("API endpoints reject unsupported HTTP methods", async () => {
  const response = await worker.fetch(new Request("https://horus.test/api/auth/login"), {
    Horus_maps: createDatabase()
  });
  assert.equal(response.status, 405);
});

test("APK endpoint streams the binary and forwards byte-range requests", async () => {
  const originalFetch = globalThis.fetch;
  globalThis.fetch = async (url, options) => {
    assert.equal(url, "https://raw.githubusercontent.com/programingtrainer/HorusMaps/main/release-assets/HorusMaps.apk");
    assert.equal(options.headers.get("range"), "bytes=0-6");
    return new Response("APKDATA", {
      status: 206,
      headers: { "content-range": "bytes 0-6/7", "content-length": "7" }
    });
  };
  try {
    const response = await worker.fetch(new Request("https://horus.test/HorusMaps.apk", {
      headers: { range: "bytes=0-6" }
    }), {});
    assert.equal(response.status, 206);
    assert.equal(response.headers.get("content-type"), "application/vnd.android.package-archive");
    assert.equal(response.headers.get("content-disposition"), 'attachment; filename="HorusMaps.apk"');
    assert.equal(await response.text(), "APKDATA");
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("nearby places endpoint validates input and returns normalized Overpass data", async () => {
  const invalid = await worker.fetch(new Request("https://horus.test/api/places?lat=999&lon=31&category=health"), {});
  assert.equal(invalid.status, 400);

  const originalFetch = globalThis.fetch;
  const requests = [];
  globalThis.fetch = async (url, options) => {
    requests.push({ url, options });
    return new Response(JSON.stringify({ elements: [{ id: 1, tags: { name: "Test Hospital" } }] }), {
      status: 200,
      headers: { "content-type": "application/json" }
    });
  };
  try {
    const response = await worker.fetch(new Request(
      "https://horus.test/api/places?lat=30.0444&lon=31.2357&category=health"
    ), {});
    assert.equal(response.status, 200);
    assert.match(response.headers.get("cache-control"), /s-maxage=120/);
    const data = await response.json();
    assert.equal(data.elements[0].tags.name, "Test Hospital");
    assert.equal(requests.length, 2);
    const queries = requests.map(item => new URLSearchParams(item.options.body).get("data"));
    assert.ok(queries.every(query => query.includes("around:5000,30.0444,31.2357")));
    assert.ok(queries.every(query => query.includes("hospital|clinic|doctors|pharmacy")));
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("nearby places endpoint safely searches Arabic brand names", async () => {
  const originalFetch = globalThis.fetch;
  let requestBody = "";
  globalThis.fetch = async (_url, options) => {
    requestBody = new URLSearchParams(options.body).get("data") || "";
    return new Response(JSON.stringify({ elements: [] }), { status: 200 });
  };
  try {
    const response = await worker.fetch(new Request(
      "https://horus.test/api/places?lat=24.7&lon=46.7&category=shopping&q=" + encodeURIComponent("بندة")
    ), {});
    assert.equal(response.status, 200);
    assert.match(requestBody, /Panda\|بندة\|بنده/);
  } finally {
    globalThis.fetch = originalFetch;
  }
});
