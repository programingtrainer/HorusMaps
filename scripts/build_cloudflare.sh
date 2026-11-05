#!/usr/bin/env bash
set -euo pipefail

rm -rf cloudflare-dist
mkdir -p cloudflare-dist

cat > cloudflare-dist/index.html <<'HTML'
<!doctype html>
<html lang="ar" dir="rtl">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="theme-color" content="#071b3a">
  <meta name="description" content="Horus Maps — استكشف العالم واعثر على الأماكن والطرق بسهولة.">
  <title>Horus Maps | استكشف بثقة</title>
  <style>
    :root { color-scheme: dark; font-family: system-ui, -apple-system, "Segoe UI", sans-serif; }
    * { box-sizing: border-box; }
    body { margin: 0; min-height: 100vh; display: grid; place-items: center; padding: 24px; color: #f4f8ff; background: radial-gradient(circle at 15% 10%, #123d70 0, transparent 38%), linear-gradient(145deg, #06142b, #0b2850 58%, #071b3a); }
    main { width: min(560px, 100%); text-align: center; padding: 42px 30px; border: 1px solid #ffffff20; border-radius: 28px; background: #ffffff0b; box-shadow: 0 28px 90px #0005; backdrop-filter: blur(18px); }
    .mark { width: 76px; height: 76px; display: grid; place-items: center; margin: 0 auto 22px; border-radius: 22px; background: #ffffff12; border: 1px solid #ffffff24; font-size: 34px; font-weight: 800; letter-spacing: -3px; }
    h1 { margin: 0; font-size: clamp(32px, 7vw, 48px); letter-spacing: -.04em; }
    p { margin: 14px auto 28px; max-width: 390px; color: #c1d2e8; line-height: 1.9; }
    a { display: inline-flex; justify-content: center; align-items: center; min-height: 54px; width: min(320px, 100%); padding: 0 22px; border-radius: 15px; color: #071b3a; background: #8ed9ff; font-weight: 800; text-decoration: none; transition: transform .2s, background .2s; }
    a:hover { transform: translateY(-2px); background: #b2e7ff; }
    small { display: block; margin-top: 22px; color: #9fb4ce; }
    #status { min-height: 22px; margin-top: 14px; color: #b9d6f4; font-size: 13px; }
  </style>
</head>
<body>
  <main>
    <div class="mark" aria-hidden="true">H</div>
    <h1>Horus Maps</h1>
    <p>اكتشف الأماكن من حولك، ابحث عن وجهتك، وخطط لطريقك بتجربة خرائط مصممة لتكون أسرع وأوضح.</p>
    <a href="/HorusMaps.apk">تنزيل تطبيق Android</a>
    <div id="status" role="status" aria-live="polite">جارٍ التحقق من حالة الخدمة...</div>
    <small>Horus Maps · استكشف بثقة</small>
  </main>
  <script>
    fetch("/api/health").then(response => response.json()).then(data => {
      document.getElementById("status").textContent = data.ok ? "الخدمة تعمل بشكل طبيعي" : "الخدمة تعمل مع وجود مشكلة في قاعدة البيانات";
    }).catch(() => {
      document.getElementById("status").textContent = "تعذر التحقق من حالة الخدمة الآن";
    });
  </script>
</body>
</html>
HTML
