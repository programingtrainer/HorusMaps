const json = (data, status = 200) => new Response(JSON.stringify(data), {
  status,
  headers: {
    "content-type": "application/json; charset=utf-8",
    "cache-control": "no-store",
    "access-control-allow-origin": "*",
    "access-control-allow-methods": "GET, POST, OPTIONS",
    "access-control-allow-headers": "content-type"
  }
});

const normalizeEmailHandle = value => String(value ?? "")
  .trim()
  .toLowerCase()
  .replace(/[^a-z0-9._]/g, "")
  .replace(/^\./, "")
  .slice(0, 20);

const passwordScore = password => {
  let score = 0;
  if (password.length >= 8) score++;
  if(/[A-Z]/.test(password)) score++;
  if(/[a-z]/.test(password)) score++;
  if(/\d/.test(password)) score++;
  if(/[^A-Za-z0-9]/.test(password)) score++;
  return score;
};

const toBase64 = bytes => btoa(String.fromCharCode(...new Uint8Array(bytes)));

const fromBase64 = value => Uint8Array.from(atob(value), character => character.charCodeAt(0));

const verifyPassword = async (password, saltBase64, hashBase64) => {
  const keyMaterial = await crypto.subtle.importKey("raw", new TextEncoder().encode(password), "PBKDF2", false, ["deriveBits"]);
  const derived = await crypto.subtle.deriveBits({ name: "PBKDF2", salt: fromBase64(saltBase64), iterations: 120000, hash: "SHA-256" }, keyMaterial, 256);
  const expected = fromBase64(hashBase64);
  const actual = new Uint8Array(derived);
  if (expected.length !== actual.length) return false;
  let difference = 0;
  for (let index = 0; index < expected.length; index++) difference |= expected[index] ^ actual[index];
  return difference === 0;
};

const hashPassword = async password => {
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const keyMaterial = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(password),
    "PBKDF2",
    false,
    ["deriveBits"]
  );
  const derived = await crypto.subtle.deriveBits(
    { name: "PBKDF2", salt, iterations: 120000, hash: "SHA-256" },
    keyMaterial,
    256
  );
  return { salt: toBase64(salt), hash: toBase64(derived) };
};

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    if (request.method === "OPTIONS") return json({ ok: true });

    if (url.pathname === "/api/health") {
      return json({ ok: true, service: "horus-maps-api", database: Boolean(env.Horus_maps) });
    }

    if (url.pathname === "/api/email/check") {
      const handle = normalizeEmailHandle(url.searchParams.get("handle"));
      if (handle.length < 3 || !/^[a-z][a-z0-9._]{2,19}$/.test(handle) || handle === "admin") {
        return json({ available: false, reason: "invalid" });
      }
      if (!env.Horus_maps) return json({ available: false, reason: "database_unconfigured" }, 503);
      const email = `${handle}@horus.com`;
      const existing = await env.Horus_maps.prepare("SELECT 1 FROM users WHERE horus_email = ? LIMIT 1").bind(email).first();
      return json({ available: !existing, email });
    }

    if (url.pathname === "/api/auth/login" && request.method === "POST") {
      if (!env.Horus_maps) return json({ error: "database_unconfigured" }, 503);
      let body;
      try { body = await request.json(); } catch { return json({ error: "invalid_json" }, 400); }
      const email = String(body.email ?? "").trim().toLowerCase();
      const password = String(body.password ?? "");
      if (!email || !password) return json({ error: "invalid_credentials" }, 400);
      const user = await env.Horus_maps.prepare("SELECT first_name, last_name, horus_email, password_salt, password_hash FROM users WHERE horus_email = ? LIMIT 1").bind(email).first();
      if (!user || !(await verifyPassword(password, user.password_salt, user.password_hash))) return json({ error: "invalid_credentials" }, 401);
      return json({ ok: true, user: { name: `${user.first_name} ${user.last_name}`, email: user.horus_email } });
    }

    if (url.pathname === "/api/auth/register" && request.method === "POST") {
      if (!env.Horus_maps) return json({ error: "database_unconfigured" }, 503);
      let body;
      try { body = await request.json(); } catch { return json({ error: "invalid_json" }, 400); }

      const firstName = String(body.firstName ?? "").trim();
      const lastName = String(body.lastName ?? "").trim();
      const phone = String(body.phone ?? "").replace(/[^0-9]/g, "");
      const country = String(body.country ?? "").trim();
      const dialCode = String(body.dialCode ?? "").trim();
      const emailHandle = normalizeEmailHandle(body.emailHandle);
      const password = String(body.password ?? "");
      const confirmPassword = String(body.confirmPassword ?? "");
      const email = `${emailHandle}@horus.com`;

      if (firstName.length < 2 || lastName.length < 2) return json({ error: "invalid_name" }, 400);
      if (!country || !/^\+[0-9]{1,4}$/.test(dialCode)) return json({ error: "invalid_country" }, 400);
      if (!/^[a-z][a-z0-9._]{2,19}$/.test(emailHandle) || emailHandle === "admin") return json({ error: "invalid_email" }, 400);
      if (password !== confirmPassword || passwordScore(password) < 3) return json({ error: "invalid_password" }, 400);

      let existing;
      try {
        existing = await env.Horus_maps.prepare("SELECT 1 FROM users WHERE horus_email = ? LIMIT 1").bind(email).first();
      } catch (error) {
        return json({ error: "database_query_failed", detail: String(error?.message ?? error) }, 500);
      }
      if (existing) return json({ error: "email_taken" }, 409);

      let credentials;
      try {
        credentials = await hashPassword(password);
      } catch (error) {
        return json({ error: "password_hash_failed", detail: String(error?.message ?? error) }, 500);
      }

      const now = new Date().toISOString();
      const id = crypto.randomUUID();

      try {
        await env.Horus_maps.prepare(`INSERT INTO users
          (id, first_name, last_name, phone, country, dial_code, horus_email, password_salt, password_hash, created_at, updated_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`)
          .bind(id, firstName, lastName, phone || null, country, dialCode, email, credentials.salt, credentials.hash, now, now)
          .run();
      } catch (error) {
        return json({ error: "database_insert_failed", detail: String(error?.message ?? error) }, 500);
      }

      return json({ ok: true, user: { id, name: `${firstName} ${lastName}`, email } }, 201);
    }

    if (url.pathname.startsWith("/api/")) return json({ error: "not_found" }, 404);
    return env.ASSETS.fetch(request);
  }
};
