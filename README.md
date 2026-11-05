# Horus Maps

Horus Maps is an Android mapping application built with Kotlin, Jetpack Compose, and MapLibre. It combines map exploration, nearby-place discovery, geocoding, route planning, and a Cloudflare-backed account API.

## Architecture

- **Android:** Kotlin, Jetpack Compose, MapLibre Android SDK 11.8.0.
- **Map styles:** OpenFreeMap Liberty and Dark styles.
- **Nearby places:** OpenStreetMap data queried through Overpass API with multiple fallback endpoints.
- **Address search:** Nominatim and Photon geocoding providers.
- **Routing:** Valhalla-based road and pedestrian routes. Displayed travel times are estimates and are not live traffic predictions.
- **Backend:** Cloudflare Worker named `horus-maps`.
- **Database:** Cloudflare D1 database `horus_maps_db`, bound to the Worker as `Horus_maps`.

## Android features

- Interactive vector map with light and dark styles, zoom, compass, location recentering, and optional 3D camera tilt.
- Nearby categories for food, fuel, health, parks, shopping, hotels, banks/ATMs, mosques, and rail stations.
- Search for named places and addresses.
- Place details, favorites, distance calculation, and route planning.
- Arabic and English interface text based on the device locale.
- Account registration and login using the Horus email namespace.

## Build and validate Android

Requirements: JDK 17 and Android SDK for compile SDK 36.

```bash
gradle --no-daemon :app:assembleDebug
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug
```

The Android Build workflow runs on the development branch and pull requests to `main`. It builds the debug APK, runs unit-test/lint tasks, and uploads the APK as a short-lived GitHub Actions artifact.

## Worker development and tests

Requirements: Node.js 22 and npm.

```bash
npm install
npm run test:worker
npm run build
npx wrangler dev
```

The Worker regression tests cover D1 health checks, email availability, registration, duplicate account handling, login, and HTTP method validation.

## Cloudflare deployment

The production deployment workflow runs on pushes to `main` and requires these GitHub Actions secrets:

- `CLOUDFLARE_API_TOKEN`
- `CLOUDFLARE_ACCOUNT_ID`

The workflow runs Worker tests, builds the static landing page, validates the deployment bundle with Wrangler dry-run, and deploys the Worker.

The Android APK is larger than the Workers Static Assets per-file limit, so it is streamed by the Worker from the repository's public release asset at `/HorusMaps.apk`. It is intentionally not copied into `cloudflare-dist`.

D1 schema migration:

```bash
npx wrangler d1 execute horus_maps_db --remote --file=./migrations/0001_create_users.sql
```

See [CLOUDFLARE_D1.md](CLOUDFLARE_D1.md) for the production D1 details.

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/health` | Worker and D1 health check |
| GET | `/api/email/check?handle=name` | Check whether a Horus email handle is available |
| POST | `/api/auth/register` | Register an account |
| POST | `/api/auth/login` | Verify account credentials |
| GET | `/HorusMaps.apk` | Stream the Android APK download |

## Data and service limitations

- Place coverage and naming depend on OpenStreetMap contributors; not every business or branch is mapped or current.
- Overpass, Nominatim, Photon, and routing providers are third-party services. Availability and response times can vary.
- Road and walking travel times are estimates. Live traffic, guaranteed transit timetables, and booking availability are not provided.
- The Android app stores the signed-in display name and email locally. Account authentication is currently performed by the Worker at registration/login; protected account-data APIs and cross-device session synchronization are not yet implemented.
