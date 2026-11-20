# Research: WeSolve Platform

Covers specs `001`-`011`. Each item resolves a point the stack choices left open. Items marked **Open risk** need a human decision before or during scaffolding.

## R1. Modular monolith shape

- **Decision**: One Gradle project, one deployable jar. Modules are top-level packages under `com.wesolve`: `platform`, `access`, `tickets`, `assistant`. A module exposes only its `api` sub-package. An ArchUnit test fails the build when a module reaches into another module's non-`api` packages or when dependencies point the wrong way (`assistant` -> `tickets` -> `access` -> `platform`).
- **Rationale**: Gives enforced boundaries without the build and release cost of several Gradle subprojects, and without a second framework.
- **Alternatives considered**: Gradle subproject per module (more build files, no benefit at this size); Spring Modulith (good verification, but a new framework the constitution says needs plan approval).

## R2. Versions and pinning

- **Decision**: Java 21 (Temurin). Spring Boot 3.5.x and the Spring AI 1.1.x BOM. Gradle wrapper with Kotlin DSL and a version catalog (`gradle/libs.versions.toml`). Node 22 LTS for the frontend build. Exact patch versions are pinned at scaffold time to the latest at that moment; lockfiles are committed (`gradle.lockfile`, `package-lock.json`).
- **Rationale**: Constitution III requires Java 21 and a supported Spring Boot 3.x. Spring AI 1.1 targets Boot 3.4/3.5.
- **Alternatives considered**: Spring Boot 4 with Spring AI 2 (not allowed by constitution III without an amendment).
- **Open risk**: Boot 3.5's open-source support window is time-limited. At scaffold time, confirm 3.5 is still supported. If it is not, amend constitution III before scaffolding rather than building on an unsupported line.

## R3. Persistence

- **Decision**: Spring Data JPA (Hibernate) on PostgreSQL, Flyway for every schema change, `ddl-auto=validate`, `open-in-view=false`. Search, counts, row locking, and audit use native SQL or `@Lock(PESSIMISTIC_WRITE)`.
- **Rationale**: The request names Lombok and PostgreSQL drivers, which fits entity-based access. Native SQL is used only where the query is not expressible in JPQL.
- **Lombok scope** (constitution III): `@Getter`/`@Setter`/`@NoArgsConstructor(access=PROTECTED)` on JPA entities, `@Slf4j`, `@RequiredArgsConstructor` on Spring beans. DTOs, events, and value objects are records. No `@Data`, `@Builder` on entities, or `@SneakyThrows`.
- **Alternatives considered**: Spring Data JDBC or `JdbcClient` (fewer surprises, but no benefit from Lombok and more mapping code); jOOQ (new dependency, code generation).

## R4. Identity and sign-in

- **Decision**: The product is an OAuth2 resource server that validates JWTs from the organization's OpenID Connect provider. The browser app signs in with the authorization-code flow with PKCE through `oidc-client-ts`, keeping tokens in memory only. The "sign-in id" in user management is matched to the token claim named by `app.security.signin-claim` (default `preferred_username`), lower-cased. The person's `hash_key` is the lowercase hex SHA-256 of that value.
- **Unknown or inactive person**: the API returns 403 with code `NO_ACCESS`; the app shows the "no access" message from spec 011.
- **Public configuration**: `GET /api/v1/public-config` (no token) returns the issuer URL and client id, so one image works in every environment and nothing is baked in at build time.
- **First administrator**: `app.bootstrap.admin-sign-in-id`. At the first authenticated request, if the claim matches, no active holder of `ACCESS_MANAGE` exists, and the token's `email_verified` is true, create the user and place them in Administrators in one transaction, under a PostgreSQL advisory lock so two simultaneous starts cannot both do it. Otherwise it is ignored.
- **Provider inside the container network**: set `OIDC_ISSUER_URI` for the expected `iss` and optionally `OIDC_JWK_SET_URI` for key retrieval when the two addresses differ (typical with a local Keycloak).
- **Rationale**: Constitution X (OAuth2/OIDC, JWT validation, no tokens in `localStorage`). Resolves `TODO(AUTH_PROVIDER)` as "any standards-compliant OIDC provider".
- **Alternatives considered**: Local username and password in the product (stores credentials, expands scope); server-side sessions (needs a session store and breaks the stateless rule).

## R5. Authorization

- **Decision**: Actions come from the database (`user_group` -> `group_role` -> `role_access`). On each request the resolver loads the person's action codes once and exposes them as Spring authorities, e.g. `TICKET_CREATE`. Controllers use `@PreAuthorize("hasAuthority('...')")`. The UI hides controls from `GET /api/v1/me`, but the server is the authority.
- **Nine fixed codes**: `TICKET_CREATE`, `TICKET_VIEW`, `TICKET_UPDATE`, `TICKET_STATUS`, `TICKET_COMMENT`, `TICKET_SEARCH`, `TICKET_ASSISTANT`, `USER_MANAGE`, `ACCESS_MANAGE`.
- **No cache**: the permission lookup joins four tiny, indexed tables. Add a cache only if measurements show a need.
- **Alternatives considered**: Roles embedded in the token (cannot change without a new sign-in, and the admin screens would not be authoritative).

## R6. Audit log and watermark

- **Decision**: A PL/pgSQL trigger on every business table writes one `audit_change` row per insert, update, or delete in the same transaction. The row's watermark comes from a one-row counter table (`audit_watermark`) updated with `UPDATE ... SET value = value + 1 RETURNING value`. The same trigger stamps the changed row's `last_watermark`. The acting person's hash key is passed per transaction with `set_config('app.actor', ..., true)`, set by a transaction-start hook, so database triggers know who acted. Changes made by migrations or the system use actor `system`.
- **Correction to the earlier proposal**: a database sequence is not commit-ordered and leaves gaps on rollback, so it cannot give "everything after watermark W". The counter row is locked until commit, which makes watermarks strictly commit-ordered and gap-free. Cost: all writing transactions serialize on that one row. At this scale (one writer per ticket action, tens per second at most) this is acceptable. Revisit only if write rates exceed that.
- **Immutability**: triggers reject `UPDATE`/`DELETE` on `audit_change` and `ticket_history`; the application role has no `TRUNCATE`.
- **Not audited**: derived tables (`ticket_search`, `vector_store`, `embedding_task`, `idempotency_record`). They are rebuildable and would double the log without adding information.
- **Schema watermark**: the Flyway history table; the highest successful version is the schema watermark. Spec 011 FR-012.
- **Alternatives considered**: Application-level auditing (misses direct SQL and bypasses); logical decoding or `pgaudit` (heavier operations burden for the lightweight server target).

## R7. Ticket number and queue order

- **Decision**: `ticket.id` is an internal UUID (`gen_random_uuid()`). `ticket_number` comes from a sequence, is unique, and is never reused. It is shown as `TKT-<number>` and is the only identifier in URLs and the API. The queue is ordered by `ticket_number DESC`, which equals newest created first and can use one index.
- **Rationale**: Spec 001 only needs a unique, stable, system-assigned ID. A sequence may skip numbers after a rolled-back create; that is acceptable.
- **Alternatives considered**: Order by `created_at` (needs a tiebreaker, no benefit); gap-free counter for ticket numbers (needless contention).

## R8. Concurrency

- **Decision**: Every ticket write runs in one transaction that first loads the ticket with `SELECT ... FOR UPDATE`. Status rules are evaluated against the locked, stored row. Field edits are last-write-wins with no version column. The database also guards status changes with a trigger, so a bug in the application cannot save an illegal move.
- **Rationale**: Matches specs 003, 004, 008: the later valid save wins and status is checked against stored status.
- **Alternatives considered**: Optimistic `@Version` (would reject a valid later save, contradicting spec 003 scenario 4).

## R9. One save, one history entry

- **Decision**: `PATCH /api/v1/tickets/{number}` accepts any of `title`, `description`, `priority`, `assigneeId`, `status`, `comment`. The server diffs against the locked row. If the assignee, the status, or a comment is present, exactly one `ticket_history` row is written. Per-field authority: title, description, priority, assignee need `TICKET_UPDATE`; status needs `TICKET_STATUS`; a comment is accepted when the caller holds `TICKET_COMMENT` or the same request changes the status. `POST /tickets/{number}/comments` adds a free comment (`TICKET_COMMENT`).
- **Rationale**: Spec 008 FR-009 (one entry when status and assignee change together) and spec 004 FR-009 (required comment on resolve and cancel) need one atomic action.
- **Alternatives considered**: Separate endpoints for each change (two saves cannot produce one entry atomically).

## R10. Search

- **Decision**: A derived table `ticket_search(ticket_id, document tsvector)` holds the title, description, and every comment of a ticket, built with the `simple` text-search configuration (no stemming, case-insensitive). Triggers on `ticket` and `ticket_history` rebuild one ticket's document. The query uses `plainto_tsquery('simple', :q)`, which requires every word, and a GIN index. Status filters and paging run on the same query.
- **Rationale**: Spec 006 requires every word to appear somewhere across title, description, and comments. A combined document is the only way to satisfy words split across fields.
- **Alternatives considered**: `ILIKE` joins (slow at 10,000 tickets and comment counts); separate tsvectors OR-ed (fails when words are split across fields); an external search engine (new service).

## R11. Idempotency

- **Decision**: `POST` create and comment endpoints, and `PATCH` ticket, accept an `Idempotency-Key` header. A filter stores `(actor_hash, key, request hash, response)` in `idempotency_record` and replays the stored response for the same key and body, or returns 422 for the same key with a different body. Records are purged after 24 hours by a scheduled job. The UI sends a new key per form submit and disables the submit control while saving.
- **Rationale**: Constitution IV; spec 001 and 004 edge cases (second activation must not double-apply).

## R12. Assistant pipeline

- **Decision**:
  1. **Indexing**: each ticket produces one `TICKET` document (title + description) and one `COMMENT` document per history comment. Metadata: `ticketNumber`, `kind`, `historyId`. Writes to `embedding_task` happen inside the originating transaction (outbox). A scheduled worker (`FOR UPDATE SKIP LOCKED`, bounded retries with backoff, `FAILED` after 5 attempts) embeds and writes `vector_store` rows. Title or description edits enqueue a replacement of the `TICKET` document.
  2. **Retrieval**: `VectorStore.similaritySearch` with `topK=8` and `similarityThreshold=0.55` (cosine). Zero results above the threshold returns `NO_SUPPORT` without calling the chat model.
  3. **Generation**: A `ChatClient` call with a system prompt loaded from `resources/prompts/assistant-system.st`. Retrieved text is passed as delimited, labelled context and the prompt states that it is data, not instructions. Output is structured (`BeanOutputConverter`): `{ answer, usedTicketNumbers[], conflict }`.
  4. **Citation guard** (spec 007 FR-003, FR-006): every used number must be in the retrieved set and must exist in `ticket`; invalid numbers are dropped; titles come from the database, never from the model. If an answer states information and no valid citation remains, the result is `NO_SUPPORT`. If `conflict` is true the result is `CONFLICT` and every citing ticket is listed.
  5. **Safety**: Redact emails, phone numbers, and long digit runs before text is embedded or placed in a prompt. Feature flag `app.assistant.enabled` is false until a provider key is present. Chat timeout 20 seconds, embedding timeout 5 seconds, one retry, Resilience4j circuit breaker around the provider calls.
  6. **Latency budget** (spec 007 SC-004, 10 s at p95): embed question about 0.3 s, vector search under 0.1 s, generation 2-6 s typical.
- **Alternatives considered**: One document per ticket with comments concatenated (loses which comment supported the answer and blows the context for long tickets); trusting model-written citations (spec forbids invented references).
- **Open risk (constitution VII)**: Ticket text can contain personal data. With an external provider, a documented approval is required before `app.assistant.enabled=true` in any shared environment. The redaction above is a minimum, not a substitute for approval.

## R13. Default model provider

- **Decision**: OpenAI through Spring AI. Chat `gpt-4o-mini`, embeddings `text-embedding-3-small` (1536 dimensions), temperature `0.1`, `max-tokens` 800. API key only from `OPENAI_API_KEY`. The vector column is `vector(1536)`.
- **Rationale**: No model hosting on a 1 CPU / 1 GB server. All values are properties.
- **Alternatives considered**: Ollama locally (needs far more memory than the lightweight target, and its default embedding model has a different dimension, which would need a new migration and full re-embedding).
- **Note**: Changing the embedding model to one with a different dimension requires a new Flyway migration and a full re-index. The plan does not hide that cost.

## R14. Frontend stack

- **Decision**: React 18+ with TypeScript `strict`, Vite, React Router, Redux Toolkit with RTK Query. API endpoints and types are generated from `openapi.yaml` with `@rtk-query/codegen-openapi`. Screen state kept in Redux slices only where it must survive navigation (queue filter and page, dashboard selection). Plain semantic HTML and CSS modules, no component library. Dates use `Intl.DateTimeFormat` so times are in the browser's local zone. Vitest, React Testing Library, MSW, and Playwright for tests; ESLint, Prettier, and `tsc --noEmit` as gates. OpenTelemetry Web (`sdk-trace-web`, fetch instrumentation) propagates `traceparent`.
- **Rationale**: Constitution VIII. No UI library keeps the bundle small and avoids a heavy new dependency; the specs need forms, lists, and tabs only.
- **Alternatives considered**: A component library (faster styling, but a large dependency and its own accessibility behaviour to verify); Redux for all server data by hand (forbidden by constitution VIII).

## R15. Packaging and runtime

- **Decision**: One multi-stage `Dockerfile` at the repository root.
  1. `node:22-alpine`: install, test, and build the frontend to `dist/`.
  2. `gradle` with JDK 21: copy `dist/` into `backend/src/main/resources/static/`, run `./gradlew bootJar` (layered jar).
  3. `eclipse-temurin:21-jre-alpine`: copy layers, run as non-root user, `EXPOSE 8080`, `HEALTHCHECK` on the liveness endpoint, `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError`.
  The jar serves the SPA and the API. A small `WebMvcConfigurer` forwards unknown non-API, non-file paths to `index.html` (spec 011 FR-017). `server.shutdown=graceful`.
- **Local**: `docker-compose.yml` with `db` (`pgvector/pgvector:pg17`) and `app`. Profile `auth` adds Keycloak with a realm import that defines the client and no users. Profile `observability` adds `grafana/otel-lgtm` (collector, Prometheus-compatible store, Tempo, Loki, and Grafana in one container) with dashboards and alert rules provisioned from the repository.
- **Lightweight server**: the same image, `docker run` with environment variables, the database address, and TLS by a reverse proxy (see R20).
- **Rationale**: Spec 011 FR-001 to FR-004. No Node Gradle plugin is needed because Docker builds the frontend first.
- **Alternatives considered**: Separate frontend and backend images (the request asks for one image); a Gradle plugin to run npm (extra plugin, slower local loop).

## R16. Observability

- **Decision**: Actuator (`health` with liveness and readiness groups, `prometheus`, `info`) on a management port (8081) that is not published by default. Micrometer Tracing with the OpenTelemetry bridge and OTLP export to `OTEL_EXPORTER_OTLP_ENDPOINT` (default `http://localhost:4318`). Boot structured logging in JSON (`logging.structured.format.console=logstash`) carrying `traceId` and `spanId`. Spring AI's built-in observations supply chat, embedding, and vector-store spans and token metrics. Custom spans and counters: ticket transitions, search, assistant stages (retrieve, generate, validate), embedding queue depth and failures.
- **Dashboards as code**: `deploy/observability/grafana/dashboards/*.json` (service RED, JVM and connection pool, database/audit writes, assistant latency/tokens/failures, embedding queue) and `deploy/observability/grafana/alerts.yaml` (error rate, p95 latency, readiness, queue depth, assistant failure rate).
- **Metric label rule**: no ticket numbers, user ids, or raw URLs as labels; use route templates.
- **Rationale**: Constitution IX, spec 011 FR-013 to FR-015.

## R17. Default configuration

| Setting | Default |
|---|---|
| HTTP port / management port | 8080 / 8081 |
| Database URL / name / user | `jdbc:postgresql://db:5432/wesolve` / `wesolve` / `wesolve` |
| Database password | `POSTGRES_PASSWORD` (required, generated into `.env` for local use) |
| Connection pool | 10 max, 2 min idle, 30 s connection timeout |
| Virtual threads | on |
| Page size / maximum page size | 25 / 100 |
| History batch size / maximum | 100 / 200 |
| Sign-in claim | `preferred_username` |
| `app.assistant.enabled` | `false` until `OPENAI_API_KEY` is set |
| Assistant top-K / threshold | 8 / 0.55 |
| Idempotency record retention | 24 hours |
| Embedding retries | 5, exponential backoff starting at 30 s |
| Graceful shutdown | 30 s |
| OTLP endpoint | `http://localhost:4318` (`http://lgtm:4318` in compose) |
| Log format | JSON |

## R18. Kafka and WebSockets

- **Decision**: Neither is used. No spec needs cross-service messaging or push. The only asynchronous work is embedding, handled by a PostgreSQL outbox table in the same process. If a later spec needs either, it must come with a constitution V or VI plan section first.
- **Rationale**: Constitution XI (start simple) and VI (WebSockets only where required). The constitution's technology table lists Kafka as the messaging standard, which applies when messaging exists; this plan has none.

## R19. Test strategy

- **Backend**: JUnit 5, AssertJ, Mockito. `@WebMvcTest` for controllers. Testcontainers with `pgvector/pgvector:pg17` for repositories, triggers, search, locking, and audit. Spring Security test for authorities. A stub `ChatModel` and `EmbeddingModel` replace the provider in every automated test. `swagger-request-validator-mockmvc` checks each request and response against `openapi.yaml`. An ArchUnit test enforces R1. JaCoCo enforces 80% line and 70% branch.
- **Frontend**: Vitest and Testing Library for components, slices, selectors; MSW generated from the same `openapi.yaml`; Playwright for the main flows against the compose stack.
- **Gates**: Spotless (formatting), SpotBugs, `tsc --noEmit`, ESLint, Prettier, dependency scan in CI.
- **Rationale**: Constitution II. Writing tests first means each task starts with its failing test.

## R20. TLS and secrets

- **Decision**: In production the product is served over TLS by a reverse proxy in front of the container, or with `server.ssl.*` and a mounted keystore. The database connection uses `sslmode=verify-full` in production and `prefer` locally. Plain HTTP on `localhost` is allowed only for the local compose setup. Secrets (`POSTGRES_PASSWORD`, `OPENAI_API_KEY`, Keycloak admin password) come from the environment; `.env` is git-ignored and `.env.example` holds placeholders only.
- **Rationale**: Constitution X forbids unencrypted transport and committed secrets. The local exception is recorded in the plan's Complexity Tracking.
