# Horus Maps D1 database

The production database is named `horus_maps_db`. The Worker binding is `Horus_maps`.

The database ID is configured in `wrangler.toml`. Keep the binding name and database ID aligned with the Cloudflare dashboard.

Apply the schema migration to the remote database:

```bash
npx wrangler d1 execute horus_maps_db --remote --file=./migrations/0001_create_users.sql
```

Verify the schema:

```bash
npx wrangler d1 execute horus_maps_db --remote --command="SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"
```

API endpoints:

- `GET /api/health` — checks the Worker and D1 connection.
- `GET /api/email/check?handle=name` — checks Horus email availability.
- `POST /api/auth/register` — creates an account.
- `POST /api/auth/login` — verifies credentials.
- `GET /HorusMaps.apk` — streams the Android APK through the Worker; the APK is not stored in Workers Static Assets because it exceeds the per-asset size limit.
