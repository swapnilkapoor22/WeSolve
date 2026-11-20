# Quickstart: Run and Validate WeSolve

A run and validation guide. Design details live in [data-model.md](data-model.md) and [contracts/openapi.yaml](contracts/openapi.yaml). Decisions live in [research.md](research.md).

## Prerequisites

| Tool | Version | Needed for |
|---|---|---|
| Docker with Compose v2 | 24 or newer | Running the product |
| JDK | 21 | Backend tests outside Docker |
| Node.js | 22 LTS | Frontend tests outside Docker |

## 1. Create local settings

```bash
cp .env.example .env
```

Edit `.env`:

| Variable | Value |
|---|---|
| `POSTGRES_PASSWORD` | A random value, for example the output of `openssl rand -hex 24`. Never commit `.env` |
| `BOOTSTRAP_ADMIN_SIGN_IN_ID` | The sign-in id of the first administrator |
| `OIDC_ISSUER_URI`, `OIDC_CLIENT_ID` | Your sign-in provider. Not needed with the `auth` profile |
| `OPENAI_API_KEY` | Optional. Without it the assistant answers "not available" and everything else works |

`.env` is ignored by git. `.env.example` holds names and safe defaults only.

## 2. Start the product

```bash
docker compose up --build
```

| Check | Expected |
|---|---|
| `http://localhost:8080` | Sign-in page, then the home screen |
| `http://localhost:8081/actuator/health/readiness` | `{"status":"UP"}` after the structure is created |
| Second `docker compose up` | No errors, no repeated seed rows, tickets from before are still there |

Optional profiles:

```bash
docker compose --profile auth up --build            # adds a local sign-in provider (Keycloak)
docker compose --profile observability up --build   # adds Grafana, traces, logs, metrics
```

With `auth`, create the first user in the Keycloak admin screen using the same sign-in id as `BOOTSTRAP_ADMIN_SIGN_IN_ID`. The imported realm has no users.

## 3. Run the tests

```bash
cd backend && ./gradlew test          # unit, slice, ArchUnit, Testcontainers integration
cd frontend && npm ci && npm test     # Vitest, React Testing Library, MSW
cd frontend && npm run e2e            # Playwright against the running compose stack
```

Integration tests start `pgvector/pgvector:pg17` through Testcontainers, so Docker must be running.

## 4. Run the image on a lightweight server

```bash
docker build -t wesolve:local .
docker run --rm -p 8080:8080 -p 8081:8081 --cpus=1 --memory=1g \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/wesolve \
  -e SPRING_DATASOURCE_USERNAME=wesolve \
  -e SPRING_DATASOURCE_PASSWORD \
  -e OIDC_ISSUER_URI -e OIDC_CLIENT_ID -e BOOTSTRAP_ADMIN_SIGN_IN_ID \
  wesolve:local
```

Put a reverse proxy with TLS in front for anything beyond a local machine. Keep port 8081 off the public network.

## 5. Validation scenarios

Each scenario follows the acceptance scenarios of the named spec.

| # | Scenario | Expected | Spec |
|---|---|---|---|
| V1 | Start from an empty database with `BOOTSTRAP_ADMIN_SIGN_IN_ID` set; sign in as that person | Home screen opens with Manage users shown; person holds Admin | 011 |
| V2 | Sign in as someone not in the system | "No access" screen with Sign out; nothing else reachable | 011 |
| V3 | Deactivate a person, then use their still-valid token | Next request is refused; the screen shows "No access" | 010, 011 |
| V4 | Create a ticket with all fields | Ticket `TKT-<n>` is Open with the chosen assignee; history shows a Created entry | 001, 008 |
| V5 | Create with a blank title, 151-character title, or 5,001-character description | Field messages shown beside the fields; nothing saved | 001 |
| V6 | Submit the create form twice quickly | One ticket exists | 001 |
| V7 | Open the queue after creating 26 tickets | 25 per page, newest first, page 2 holds the oldest | 002 |
| V8 | Change title, priority, and assignee in one save | One history entry carries the assignee change; other fields updated | 003, 008 |
| V9 | Edit the same ticket in two browser tabs | The later save wins per field; both saves are in history | 003 |
| V10 | Move Open to Resolved | Refused with a message; status unchanged | 004 |
| V11 | Move In Progress to Resolved with a blank comment | Refused; the comment box is flagged | 004 |
| V12 | Cancel an Open ticket with a comment | Status Cancelled; history entry holds status and comment | 004, 008 |
| V13 | Add a comment to a Closed ticket | Comment appears at the end of history; status unchanged | 005 |
| V14 | Search "refund" with status All, then with Resolved | Matching tickets from title, description, and comments; second search narrows | 006 |
| V15 | Ask the assistant something covered by resolved tickets | Answer with ticket numbers and titles that exist | 007 |
| V16 | Ask the assistant something no ticket covers | "No supporting tickets" outcome with no references; the model is not called | 007 |
| V17 | Unset `OPENAI_API_KEY`, ask a question | "Assistant not available"; rest of the app works | 007, 011 |
| V18 | Open the home screen | Assigned tickets and status counts; Home button returns here from any screen | 009 |
| V19 | Sign in as someone without View tickets | No ticket queue controls; direct links refused | 002, 009 |
| V20 | Create a user, add to Support, then remove the last Admin from Administrators | Last removal is refused with an explanation | 010 |
| V21 | Change any record, then read `audit_change` | Entry with actor, time, before, after, and a higher watermark than the previous entry | 011 |
| V22 | Try `UPDATE` or `DELETE` on `ticket_history` or `audit_change` in psql | Rejected | 008, 011 |
| V23 | Run the observability profile; create a ticket | One trace from the browser through the API to the database in Grafana | 011 |
| V24 | Stop PostgreSQL | Readiness turns `DOWN`; screens show a retry message, not a blank page | 011 |
| V25 | Run the image with `--cpus=1 --memory=1g` | Starts and serves the home screen within 60 seconds | 011 |

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Container exits with a database error | Password in `.env` changed after the volume was created | `docker compose down -v` (this deletes local data), then start again |
| Sign-in loops | `OIDC_ISSUER_URI` and the browser-facing issuer differ | Use the same issuer URL for both |
| Assistant says unavailable | No API key or provider down | Set `OPENAI_API_KEY`, restart |
