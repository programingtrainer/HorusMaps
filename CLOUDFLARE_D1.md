# Horus Maps D1 database

1. Open Cloudflare Dashboard and create a D1 database named `horus-maps-db`.
2. Copy the generated Database ID.
3. Add it to `wrangler.toml`:

```toml
[[d1_databases]]
binding = "DB"
database_name = "horus-maps-db"
database_id = "PASTE_DATABASE_ID"
```

4. Apply the migration:

```bash
npx wrangler d1 execute horus-maps-db --remote --file=./migrations/0001_create_users.sql
```

API endpoints:

- `GET /api/health`
- `GET /api/email/check?handle=name`
- `POST /api/auth/register`
