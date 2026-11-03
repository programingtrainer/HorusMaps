#!/usr/bin/env bash
set -euo pipefail

rm -rf cloudflare-dist
mkdir -p cloudflare-dist
cp release-assets/HorusMaps.apk cloudflare-dist/HorusMaps.apk

cat > cloudflare-dist/index.html <<'HTML'
<!doctype html>
<html lang="ar" dir="rtl">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Horus Maps</title>
  <style>
    * { box-sizing: border-box; }
    body { margin: 0; min-height: 100vh; display: grid; place-items: center; background: #f7fbff; color: #062a6b; font-family: sans-serif; }
    main { width: min(420px, calc(100% - 32px)); text-align: center; }
    a { display: block; margin-top: 24px; padding: 16px; border-radius: 14px; background: #0879d8; color: white; text-decoration: none; font-weight: 700; }
  </style>
</head>
<body><main><h1>Horus Maps</h1><p>يمكنك تنزيل أحدث نسخة تجريبية من التطبيق.</p><a href="/HorusMaps.apk" download>تنزيل APK</a></main></body>
</html>
HTML
