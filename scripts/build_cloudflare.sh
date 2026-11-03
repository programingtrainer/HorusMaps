#!/usr/bin/env bash
set -euo pipefail

SDK_ROOT="${ANDROID_HOME:-/tmp/android-sdk}"
export ANDROID_HOME="$SDK_ROOT"
export ANDROID_SDK_ROOT="$SDK_ROOT"

JDK_ROOT="/tmp/jdk17"

if ! command -v java >/dev/null 2>&1 || ! java -version 2>&1 | grep -q '17\|21'; then
  rm -rf /tmp/jdk17.tar.gz "$JDK_ROOT"
  curl -fsSL "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.13%2B11/OpenJDK17U-jdk_x64_linux_hotspot_17.0.13_11.tar.gz" -o /tmp/jdk17.tar.gz
  mkdir -p "$JDK_ROOT"
  tar -xzf /tmp/jdk17.tar.gz --strip-components=1 -C "$JDK_ROOT"
fi

if [[ -x "$JDK_ROOT/bin/java" ]]; then
  export JAVA_HOME="$JDK_ROOT"
else
  export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
fi
export PATH="$JAVA_HOME/bin:$PATH"

if [[ ! -x "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]]; then
  mkdir -p "$SDK_ROOT/cmdline-tools"
  curl -fsSL "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -o /tmp/android-command-tools.zip
  rm -rf /tmp/android-command-tools
  unzip -q /tmp/android-command-tools.zip -d /tmp/android-command-tools
  mv /tmp/android-command-tools/cmdline-tools "$SDK_ROOT/cmdline-tools/latest"
fi

mkdir -p "$SDK_ROOT/licenses"
printf '8933bad161af4178b1185d1a37fbf41ea5269c55\nd56f5187479451eabf01fb78af6dfcb131a6481e\n' > "$SDK_ROOT/licenses/android-sdk-license"
printf '84831b9409646a918e30573bab4c9c91346d8abd\n' >> "$SDK_ROOT/licenses/android-sdk-preview-license"

yes | "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK_ROOT" --licenses >/dev/null
"$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK_ROOT" \
  "platform-tools" "platforms;android-36" "build-tools;35.0.0" "build-tools;36.0.0"

printf 'sdk.dir=%s\n' "$SDK_ROOT" > local.properties
./gradlew --no-daemon --max-workers=2 :app:assembleDebug

rm -rf cloudflare-dist
mkdir -p cloudflare-dist
cp app/build/outputs/apk/debug/app-debug.apk cloudflare-dist/HorusMaps.apk
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
