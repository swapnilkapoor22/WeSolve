---
description: "Implementation tasks for specs 001-011, in dependency order"
---

# Tasks: WeSolve (specs 001-011)

**Input**: Design documents from `/specs/011-platform-foundation/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md, and specs 001-010

**Tests**: Included. Constitution II requires a failing test before implementation. Write the test task, see it fail, then do the implementation tasks in that phase.

**Non-breaking rule** (applies to every task): Implement the contract and schema that already exist. Do not remove, rename, or retype a path, schema, required property, enum value, or success status in `api/openapi.yaml` or `specs/011-platform-foundation/contracts/openapi.yaml`. Do not edit a Flyway file after the task that added it is done. A later schema change is a new `V{n}__` file. A later API change adds an optional field or a new path. Omitting a new optional field must keep the previous response. Each phase leaves every earlier phase's screens and status codes working.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an unfinished task)
- **[Story]**: User-story label. Setup and Foundational have none.

## Story map

Labels follow dependency order, not the spec number. Each spec has one story except 011, which has three.

| Label | Spec | Story | Priority |
|-------|------|-------|----------|
| US1 | 011 | Run the whole product from one package | P1 |
| US2 | 011 | The first administrator gets in | P1 |
| US3 | 009 | Land on my tickets | P1 |
| US4 | 001 | Create a ticket | P1 |
| US5 | 002 | List and view tickets | P1 |
| US6 | 008 | Read the history of a ticket | P1 |
| US7 | 003 | Update ticket details | P2 |
| US8 | 004 | Move a ticket through its lifecycle | P2 |
| US9 | 005 | Add comments | P3 |
| US10 | 006 | Search and filter the queue | P3 |
| US11 | 010 | Manage people and their access | P1 |
| US12 | 007 | Ask the assistant about past tickets | P4 |
| US13 | 011 | Know whether the product is healthy | P2 |

US3 comes before ticket screens because spec 011 sends a successful sign-in to the ticket home, and an empty home is a valid 009 state. US13 comes last so dashboards observe flows that already exist. Readiness itself is part of US1 so the image health check has an endpoint to call.

## Path Conventions

- Backend: `backend/src/main/java/com/wesolve/{platform,access,tickets,assistant}/`
- Backend tests: `backend/src/test/java/com/wesolve/`
- Migrations: `backend/src/main/resources/db/migration/`
- Frontend: `frontend/src/features/{auth,dashboard,tickets,assistant,users,access}/`
- Contract source: `specs/011-platform-foundation/contracts/openapi.yaml` copied to `api/openapi.yaml`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Empty build for both sides. No behavior yet.

- [X] T001 Create the Gradle Kotlin DSL build in `backend/settings.gradle.kts`, `backend/build.gradle.kts`, and `backend/gradle/libs.versions.toml` for Java 21, Spring Boot 3.5.x, Spring AI 1.1.x, Flyway, PostgreSQL, Lombok, Micrometer tracing OTLP, and Resilience4j, with versions pinned and `backend/gradle.lockfile` committed
- [X] T002 [P] Create the Vite React TypeScript app in `frontend/package.json`, `frontend/tsconfig.json` (`strict: true`), and `frontend/vite.config.ts`, with React, Redux Toolkit, React Router, and `oidc-client-ts`
- [X] T003 [P] Add Spotless and SpotBugs to `backend/build.gradle.kts`, and ESLint, Prettier, and `tsc --noEmit` scripts to `frontend/package.json`
- [ ] T004 [P] Add JaCoCo gates of 80% line and 70% branch to `backend/build.gradle.kts`
- [X] T005 [P] Add `.env.example` with names only for `POSTGRES_PASSWORD`, `BOOTSTRAP_ADMIN_SIGN_IN_ID`, `OIDC_ISSUER_URI`, `OIDC_CLIENT_ID`, and `OPENAI_API_KEY`, and ignore `.env` in `.gitignore`
- [X] T006 Copy `specs/011-platform-foundation/contracts/openapi.yaml` to `api/openapi.yaml` unchanged
- [X] T007 Create `backend/src/main/java/com/wesolve/WeSolveApplication.java` and empty `api` packages under `platform`, `access`, `tickets`, and `assistant`
- [X] T008 [P] Create `frontend/src/app/`, `frontend/src/api/`, `frontend/src/telemetry/`, and `frontend/src/features/{auth,dashboard,tickets,assistant,users,access}/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared plumbing every story uses. No story screen is reachable yet.

**CRITICAL**: Finish this phase before any user story.

- [X] T009 Write `backend/src/test/java/com/wesolve/architecture/ModuleBoundaryTest.java` so `assistant` depends only on `tickets`, `tickets` only on `access`, `access` only on `platform`, and only through each module's `api` package
- [X] T010 [P] Add RFC 9457 handling in `backend/src/main/java/com/wesolve/platform/web/ApiExceptionHandler.java` with `code` and `traceId` for `VALIDATION_FAILED`, `NOT_FOUND`, `NO_ACCESS`, `FORBIDDEN`, `STATUS_CHANGE_NOT_ALLOWED`, `COMMENT_REQUIRED`, `LAST_ADMIN`, `DUPLICATE`, `IDEMPOTENCY_CONFLICT`, and `ASSISTANT_UNAVAILABLE`
- [X] T011 [P] Set `spring.threads.virtual.enabled=true`, `spring.jpa.hibernate.ddl-auto=validate`, `spring.jpa.open-in-view=false`, application port 8080, management port 8081, and JSON logs in `backend/src/main/resources/application.yml`
- [X] T012 [P] Add the JWT resource-server filter in `backend/src/main/java/com/wesolve/platform/security/SecurityConfig.java`, permitting only `GET /api/v1/public-config` and denying every other `/api/**` path until a later story opens it
- [X] T013 [P] Add the Redux store and router shell in `frontend/src/app/store.ts` and `frontend/src/app/router.tsx`, with a route-level error boundary in `frontend/src/app/App.tsx`
- [ ] T014 [P] Add Micrometer tracing OTLP export in `backend/src/main/resources/application.yml` and `traceparent` injection in `frontend/src/telemetry/tracing.ts`
- [X] T015 [P] Add the Testcontainers base `backend/src/test/java/com/wesolve/support/PostgresIntegrationTest.java` using `pgvector/pgvector:pg17`
- [ ] T016 [P] Add the MSW server in `frontend/src/test/msw.ts` and generate the RTK Query client from `api/openapi.yaml` into `frontend/src/api/generated.ts` without hand-editing it
- [X] T017 Add `backend/src/main/java/com/wesolve/platform/audit/ActorContext.java` to set `app.actor` with `set_config(..., true)` at transaction start

**Checkpoint**: The build, boundaries, errors, and sign-in gate exist. No business data yet.

---

## Phase 3: User Story 1 - Run the whole product from one package (Priority: P1)

**Goal**: One image, one local command, empty database becomes the full structure, sign-in screen loads. Spec 011 story 1.

**Independent Test**: `docker compose up --build` on a clean machine shows the sign-in screen, readiness is UP, and a second start does not duplicate seed rows (quickstart V1 setup, spec 011 scenarios 1-3 and 5).

### Tests for User Story 1

- [ ] T018 [P] [US1] Write a failing migration test in `backend/src/test/java/com/wesolve/platform/SchemaBootstrapIT.java`: empty database gains every table from `data-model.md`; applying migrations twice keeps one seed row per access code
- [X] T019 [P] [US1] Write a failing startup test in `backend/src/test/java/com/wesolve/platform/MissingSecretIT.java`: a missing `POSTGRES_PASSWORD` names that setting and does not print a secret value

### Implementation for User Story 1

- [ ] T020 [US1] Add `backend/src/main/resources/db/migration/V1__audit.sql` with one-row `audit_watermark` (`id = 1`) and append-only `audit_change` (`watermark` PK, `changed_at`, `actor_hash varchar(64)`, `table_name varchar(63)`, `operation` `INSERT`/`UPDATE`/`DELETE`, `row_key`, `before_data jsonb`, `after_data jsonb`, `transaction_id`). The trigger takes the watermark with `UPDATE audit_watermark SET value = value + 1 RETURNING value` and rejects `UPDATE` and `DELETE` on `audit_change`
- [ ] T021 [US1] Add `V2__access.sql` for `users` (`hash_key varchar(64)` unique matching `^[0-9a-f]{64}$`, immutable; `sign_in_id varchar(100)` unique, stored lower-case, immutable; `display_name varchar(100)` not blank; `email varchar(254)` unique, stored lower-case, matching `.+@.+`; `active boolean` default true; `can_be_assigned boolean` default true), plus `groups`, `roles`, `access_types` keyed by `code`, and `user_group`, `group_role`, `role_access`. Audit triggers on all of them. Depends on T020
- [ ] T022 [US1] Add `V3__support_member.sql`: `support_member` (`hash_key varchar(64)` unique, no foreign key to `users`; `display_name varchar(100)`; `assignable boolean`) and `support_member_user` (`support_member_id` PK FK, `user_hash_key` unique FK to `users.hash_key`). Depends on T021
- [ ] T023 [US1] Add `V4__ticket.sql`: `ticket_number_seq`, `ticket` (`ticket_number bigint` unique, `title varchar(150)` with `btrim(title) <> ''`, `description varchar(5000)` with `btrim(description) <> ''`, `priority` in `LOW|MEDIUM|HIGH|URGENT`, `status` in `OPEN|IN_PROGRESS|RESOLVED|CLOSED|CANCELLED` default `OPEN`, `assignee_id` FK to `support_member`). Status trigger allows only `OPEN→IN_PROGRESS`, `OPEN→CANCELLED`, `IN_PROGRESS→RESOLVED`, `IN_PROGRESS→CANCELLED`, `RESOLVED→CLOSED`, or the same status. Depends on T022
- [ ] T024 [US1] Add `V5__ticket_history.sql`: insert-only `ticket_history` (`id bigint` identity, `ticket_id`, `actor_id`, `from_status`, `to_status`, `from_assignee_id`, `to_assignee_id`, `comment varchar(2000)`). Checks: at least one of `to_status`, `to_assignee_id`, `comment` is set; `RESOLVED` or `CANCELLED` requires a non-blank comment; status pair and assignee pair are both null or both set, except the creation row which sets only `to_status` and `to_assignee_id`. Triggers reject `UPDATE` and `DELETE`. Depends on T023
- [ ] T025 [US1] Add `V6__ticket_search.sql`: `ticket_search(ticket_id, document tsvector)` built with `to_tsvector('simple', ...)`, GIN index, rebuild trigger on `ticket` and `ticket_history`. Not audited. Depends on T024
- [ ] T026 [US1] Add `V7__seed.sql` inserting the nine codes `TICKET_CREATE`, `TICKET_VIEW`, `TICKET_UPDATE`, `TICKET_STATUS`, `TICKET_COMMENT`, `TICKET_SEARCH`, `TICKET_ASSISTANT`, `USER_MANAGE`, `ACCESS_MANAGE`; groups Support, User managers, Administrators; roles Support agent (seven `TICKET_*` codes), User manager (`USER_MANAGE`), Admin (`USER_MANAGE`, `ACCESS_MANAGE`). Use `ON CONFLICT DO NOTHING`. Depends on T021
- [ ] T027 [US1] Add `V8__idempotency.sql`: `idempotency_record` PK (`actor_hash`, `idempotency_key varchar(100)`), `request_hash varchar(64)`, `response_status`, `response_body jsonb`, `created_at`. Not audited. Depends on T020
- [ ] T028 [US1] Add `V9__vector.sql`: `vector_store` (`embedding vector(1536)`, HNSW `vector_cosine_ops`, metadata json) and `embedding_task` (`kind` `TICKET|COMMENT`, `status` `PENDING|DONE|FAILED`, `attempts` default 0). Not audited. `spring.ai.vectorstore.pgvector.initialize-schema=false`. Depends on T020
- [ ] T029 [US1] Add the multi-stage `Dockerfile` (Node 22 build, Gradle JDK 21 `bootJar`, `eclipse-temurin:21-jre-alpine`, non-root, `HEALTHCHECK` on liveness) and `docker-compose.yml` with `pgvector/pgvector:pg17` and the app. Profiles `auth` and `observability` are empty until T117 and T124
- [ ] T030 [US1] Forward unknown non-API paths to `index.html` in `backend/src/main/java/com/wesolve/platform/web/SpaForwardingConfig.java` (spec 011 FR-017)
- [ ] T031 [US1] Implement `GET /api/v1/public-config` in `backend/src/main/java/com/wesolve/platform/web/PublicConfigController.java` returning `issuer` and `clientId` from configuration
- [ ] T032 [US1] Add the sign-in screen in `frontend/src/features/auth/SignInPage.tsx` and liveness plus readiness (readiness requires the database) in `backend/src/main/resources/application.yml`

**Checkpoint**: The package starts, migrates, and shows sign-in. No person can enter yet.

---

## Phase 4: User Story 2 - The first administrator gets in (Priority: P1)

**Goal**: Named first administrator becomes Admin once. Unknown and deactivated people see no access. Spec 011 story 2.

**Independent Test**: Sign in as the named admin and reach home; sign in as an unknown person and see no access; sign out returns to sign-in (quickstart V1, V2, V3).

### Tests for User Story 2

- [ ] T033 [P] [US2] Write a failing contract test for `GET /api/v1/me` (200 `CurrentMember`, 403 `NO_ACCESS`) in `backend/src/test/java/com/wesolve/access/MeContractTest.java` against `api/openapi.yaml`
- [ ] T034 [P] [US2] Write a failing integration test in `backend/src/test/java/com/wesolve/access/BootstrapAdminIT.java`: first matching sign-in with `email_verified=true` and no active `ACCESS_MANAGE` holder creates the admin; a second start does not; a deactivated user gets `NO_ACCESS`

### Implementation for User Story 2

- [ ] T035 [US2] Map `users` and `support_member` in `backend/src/main/java/com/wesolve/access/persistence/UserEntity.java` and `SupportMemberEntity.java`. Lombok only `@Getter`, `@Setter`, and protected `@NoArgsConstructor` on entities. `hash_key` is SHA-256 hex of the lower-cased sign-in id
- [ ] T036 [US2] Implement one-time bootstrap in `backend/src/main/java/com/wesolve/access/BootstrapAdminService.java` under a PostgreSQL advisory lock, creating `users`, `support_member`, and `support_member_user` in one transaction and placing the person in Administrators. Depends on T035
- [ ] T037 [US2] Load action codes on each request with no cache in `backend/src/main/java/com/wesolve/access/ActionAuthorityService.java`, and return `GET /api/v1/me` from `backend/src/main/java/com/wesolve/access/MeController.java`. Depends on T033, T036
- [ ] T038 [P] [US2] Sign in with authorization code and PKCE, tokens in memory only, in `frontend/src/features/auth/authService.ts`
- [ ] T039 [P] [US2] Add the no-access screen in `frontend/src/features/auth/NoAccessPage.tsx` and Sign out in `frontend/src/app/Header.tsx`. Header shows the member name. Home targets `/`

**Checkpoint**: An administrator can sign in and sign out. Everyone else sees no access.

---

## Phase 5: User Story 3 - Land on my tickets (Priority: P1)

**Goal**: `/` is the ticket home: assigned tickets, status counts, header. Spec 009. Ticket rows stay empty until US4.

**Independent Test**: After sign-in the home opens. With no assigned tickets the list says so and every count is 0. Home from any later screen returns here (quickstart V18).

### Tests for User Story 3

- [ ] T040 [P] [US3] Write failing contract tests for `GET /api/v1/dashboard/summary` and `GET /api/v1/dashboard/tickets` in `backend/src/test/java/com/wesolve/tickets/DashboardContractTest.java`. Page default 25, maximum 100. An unknown person gets 403
- [ ] T041 [P] [US3] Write a failing home test in `frontend/src/features/dashboard/TicketHomePage.test.tsx`: empty copy is not an error; counts render All, Open, In Progress, Resolved, Closed, Cancelled; a member without View tickets sees rows that do not open

### Implementation for User Story 3

- [ ] T042 [US3] Implement summary and assigned-ticket queries in `backend/src/main/java/com/wesolve/tickets/DashboardService.java` (newest `ticket_number` first, status filter optional, 25 per page) and `backend/src/main/java/com/wesolve/tickets/DashboardController.java`. Depends on T040
- [ ] T043 [US3] Build `frontend/src/features/dashboard/TicketHomePage.tsx` with counts, paging, loading, error-and-retry, and keyboard access. New ticket, All tickets, and Manage users stay hidden until the member holds `TICKET_CREATE`, `TICKET_VIEW`, or `USER_MANAGE`. Depends on T041, T042
- [ ] T044 [US3] Route `/` to the home and point Header Home at `/` in `frontend/src/app/router.tsx`. Do not change Sign out from T039

**Checkpoint**: Sign-in lands on a working home. No ticket can be created yet.

---

## Phase 6: User Story 4 - Create a ticket (Priority: P1)

**Goal**: Create form and `POST /api/v1/tickets`. Spec 001. The first history row is the creation entry from spec 008 FR-003.

**Independent Test**: Submit a valid ticket and land on its detail with `TKT-<n>`, status Open, and one history entry. An invalid field saves nothing (quickstart V4, V5, V6).

### Tests for User Story 4

- [ ] T045 [P] [US4] Write a failing contract test for `POST /api/v1/tickets` and `GET /api/v1/assignees` in `backend/src/test/java/com/wesolve/tickets/CreateTicketContractTest.java` (201 + `Location`, 400, 403, 422)
- [ ] T046 [P] [US4] Write a failing integration test in `backend/src/test/java/com/wesolve/tickets/CreateTicketIT.java`: title `varchar(150)` and description `varchar(5000)` reject blanks and over-length values; priority is only `LOW|MEDIUM|HIGH|URGENT`; status is `OPEN`; one history row has `to_status=OPEN` and `to_assignee_id` and null `from_status` and `from_assignee_id`; a repeated `Idempotency-Key` with the same body replays one ticket

### Implementation for User Story 4

- [ ] T047 [US4] Map `ticket` and `ticket_history` in `backend/src/main/java/com/wesolve/tickets/persistence/TicketEntity.java` and `TicketHistoryEntity.java`. Depends on T023, T024
- [ ] T048 [US4] Implement create plus the creation history row in one transaction in `backend/src/main/java/com/wesolve/tickets/CreateTicketService.java`, and list assignable members from `support_member WHERE assignable` in `backend/src/main/java/com/wesolve/tickets/AssigneeController.java`. Requires `TICKET_CREATE`. Depends on T045, T047
- [ ] T049 [US4] Store and replay `Idempotency-Key` (8 to 100 characters) in `backend/src/main/java/com/wesolve/platform/web/IdempotencyFilter.java`. Same key and different body returns 422 `IDEMPOTENCY_CONFLICT`. Depends on T027, T048
- [ ] T050 [US4] Add the create form in `frontend/src/features/tickets/CreateTicketPage.tsx` with Title, Description, Priority, and Assignee only. No ticket-id field, delete, attachments, notifications, due dates, or automatic assignment. Field errors stay beside the field and keep the other typed values. A new idempotency key is sent per submit and the control disables while saving. Depends on T046, T048
- [ ] T051 [US4] Show New ticket on the home only when `GET /api/v1/me` includes `TICKET_CREATE`, linking to `/tickets/new` in `frontend/src/features/dashboard/TicketHomePage.tsx`. Do not change the empty-state copy from T043

**Checkpoint**: A member who holds Create tickets can save a ticket. The queue screen does not exist yet.

---

## Phase 7: User Story 5 - List and view tickets (Priority: P1)

**Goal**: Shared queue and detail. Spec 002. Keyword search is not this phase.

**Independent Test**: 26 tickets show 25 on page 1, newest first, and the oldest on page 2. Detail shows fields and an empty comment area when there are none. An unknown number is not found (quickstart V7).

### Tests for User Story 5

- [ ] T052 [P] [US5] Write failing contract tests for `GET /api/v1/tickets` and `GET /api/v1/tickets/{number}` in `backend/src/test/java/com/wesolve/tickets/ViewTicketContractTest.java`. Absent `q` and `status` return the full queue. `size` above 100 is rejected
- [ ] T053 [P] [US5] Write a failing queue test in `frontend/src/features/tickets/TicketQueuePage.test.tsx` for loading, empty, error-and-retry, and 25 rows with next and previous

### Implementation for User Story 5

- [ ] T054 [US5] Implement the queue (newest `ticket_number` first, 25 per page) and detail read in `backend/src/main/java/com/wesolve/tickets/TicketQueryService.java` and `TicketController.java`. Require `TICKET_VIEW`. Do not apply `q` yet. Depends on T052
- [ ] T055 [US5] Add `frontend/src/features/tickets/TicketQueuePage.tsx` and `TicketDetailPage.tsx`. Times use `Intl.DateTimeFormat`. Detail shows id, title, description, priority, assignee, status, created time, and last-updated time. Comments and history render from later phases without a second layout change: leave a list region that is empty until T062. Depends on T053, T054
- [ ] T056 [US5] Show All tickets on the home only for `TICKET_VIEW`, linking to `/tickets` in `frontend/src/features/dashboard/TicketHomePage.tsx`. A member without `TICKET_VIEW` still cannot open a home row (T043)

**Checkpoint**: Queue and detail work. Fields are read-only.

---

## Phase 8: User Story 6 - Read the history of a ticket (Priority: P1)

**Goal**: Oldest-first history on the detail screen. Spec 008. Creation rows already exist from US4. This phase does not add edit or delete.

**Independent Test**: Open a new ticket and see one Created entry with Open, the assignee, the author, and the time, and no earlier status (spec 008 FR-003). No edit or delete control (quickstart V4).

### Tests for User Story 6

- [ ] T057 [P] [US6] Write a failing contract test for `GET /api/v1/tickets/{number}/history` in `backend/src/test/java/com/wesolve/tickets/HistoryContractTest.java`: `afterId` default 0, `limit` default 100 maximum 200, order `id ASC`, 404 for an unknown number, 403 without `TICKET_VIEW`
- [ ] T058 [P] [US6] Write a failing integration test in `backend/src/test/java/com/wesolve/tickets/HistoryImmutabilityIT.java` that a direct `UPDATE` or `DELETE` on `ticket_history` is rejected

### Implementation for User Story 6

- [ ] T059 [US6] Implement paged history reads in `backend/src/main/java/com/wesolve/tickets/HistoryQueryService.java` and `HistoryController.java`. Depends on T057
- [ ] T060 [US6] Render entries oldest-first in `frontend/src/features/tickets/HistoryList.tsx` inside the detail region from T055. Show only the parts that are set. No edit or delete control. Local time, loading, and retry. Depends on T059

**Checkpoint**: History is visible and immutable. It still only contains creation entries.

---

## Phase 9: User Story 7 - Update ticket details (Priority: P2)

**Goal**: Save title, description, priority, and assignee. Spec 003. `PATCH` accepts only those fields in this phase.

**Independent Test**: One save changes title, priority, and assignee and writes one history entry for the assignee only. Title-only saves write no history entry. A later save wins field by field (quickstart V8, V9).

### Tests for User Story 7

- [ ] T061 [P] [US7] Write a failing contract test for `PATCH /api/v1/tickets/{number}` in `backend/src/test/java/com/wesolve/tickets/UpdateTicketContractTest.java`. Exercise requests that send only `title`, `description`, `priority`, and `assigneeId`. Leave `status` and `comment` in `api/openapi.yaml`. `minProperties: 1`. 404 does not create a ticket
- [ ] T062 [P] [US7] Write a failing integration test in `backend/src/test/java/com/wesolve/tickets/UpdateTicketIT.java`: blank or whitespace title and description are rejected; status does not change; saving the same assignee writes no history row; a real assignee change writes one row with both assignee ids; two overlapping saves keep the later value per field (`SELECT ... FOR UPDATE`)

### Implementation for User Story 7

- [ ] T063 [US7] Implement the field diff and single history row in `backend/src/main/java/com/wesolve/tickets/UpdateTicketService.java`. Require `TICKET_UPDATE`. Ignore `status` and `comment` if present so this phase cannot change lifecycle behavior. Depends on T061, T062
- [ ] T064 [US7] Accept `Idempotency-Key` on this `PATCH` in `backend/src/main/java/com/wesolve/platform/web/IdempotencyFilter.java` without changing POST replay from T049
- [ ] T065 [US7] Add edit controls and one save button for the four fields in `frontend/src/features/tickets/TicketDetailPage.tsx`. Status stays visible and is not on this control. Failed saves keep typed values. Depends on T063

**Checkpoint**: Field edits work. Status still cannot be changed from the screen.

---

## Phase 10: User Story 8 - Move a ticket through its lifecycle (Priority: P2)

**Goal**: Legal status steps, with a required comment to resolve or cancel. Spec 004. This phase adds optional `status` and `comment` to the existing `PATCH`. A body without them must behave exactly as US7.

**Independent Test**: Open cannot jump to Resolved. In Progress to Resolved with a blank comment is refused. Cancel of an Open ticket with a comment succeeds and writes one history entry (quickstart V10, V11, V12).

### Tests for User Story 8

- [ ] T066 [P] [US8] Extend `backend/src/test/java/com/wesolve/tickets/UpdateTicketContractTest.java` so a `PATCH` that also sends `status` and `comment` is valid, and a `PATCH` that sends neither still returns the US7 shape. Add 409 `STATUS_CHANGE_NOT_ALLOWED`
- [ ] T067 [P] [US8] Write a failing integration test in `backend/src/test/java/com/wesolve/tickets/StatusTransitionIT.java` for the only allowed pairs (`OPEN→IN_PROGRESS`, `OPEN→CANCELLED`, `IN_PROGRESS→RESOLVED`, `IN_PROGRESS→CANCELLED`, `RESOLVED→CLOSED`). `RESOLVED` and `CANCELLED` require a non-blank `comment` of at most 2,000 characters. The same status is a no-op with no history row. The decision uses the locked stored status

### Implementation for User Story 8

- [ ] T068 [US8] Handle `status` and `comment` inside `backend/src/main/java/com/wesolve/tickets/UpdateTicketService.java` under the same row lock. `status` requires `TICKET_STATUS`. A comment on a status change is allowed without `TICKET_COMMENT`. One request still writes at most one history row. A request that omits `status` and `comment` stays on the US7 path. Depends on T066, T067
- [ ] T069 [US8] Add the status control in `frontend/src/features/tickets/StatusControl.tsx`, offering only the current status and the legal next statuses. Show a required comment box for Resolved and Cancelled, and an optional one for In Progress and Closed. A second click does not send a second request. Depends on T068

**Checkpoint**: Status moves only along the legal path. Field edits from US7 still work when status is omitted.

---

## Phase 11: User Story 9 - Add comments (Priority: P3)

**Goal**: Free comments, including on Closed and Cancelled tickets. Spec 005. New endpoint only. `PATCH` behavior from US7 and US8 stays as it is.

**Independent Test**: A comment on a Closed ticket appears at the end of history and does not change status, title, description, priority, or assignee (quickstart V13).

### Tests for User Story 9

- [ ] T070 [P] [US9] Write a failing contract test for `POST /api/v1/tickets/{number}/comments` in `backend/src/test/java/com/wesolve/tickets/CommentContractTest.java`. `comment` length 1 to 2,000. 201 returns the history entry. 404 creates nothing
- [ ] T071 [P] [US9] Write a failing integration test in `backend/src/test/java/com/wesolve/tickets/CommentIT.java`: a whitespace-only comment is rejected; a free comment sets only `comment`; `ticket.updated_at` does not move; Closed and Cancelled accept the comment

### Implementation for User Story 9

- [ ] T072 [US9] Implement the free-comment insert in `backend/src/main/java/com/wesolve/tickets/CommentService.java` and `CommentController.java`. Require `TICKET_COMMENT`. Accept `Idempotency-Key`. Depends on T070, T071
- [ ] T073 [US9] Add the comment box and Add comment control in `frontend/src/features/tickets/CommentForm.tsx` on the detail screen, with no edit or delete control. A failed save keeps the typed text. Depends on T072

**Checkpoint**: Free comments append history. Status changes and field edits are unchanged.

---

## Phase 12: User Story 10 - Search and filter the queue (Priority: P3)

**Goal**: Keyword and status filter on the existing queue. Spec 006. Omitting `q` and `status` must still return the US5 queue.

**Independent Test**: Search "refund" with All, then with Resolved. A word split across title and a comment still matches. No match is an empty list, not an error (quickstart V14).

### Tests for User Story 10

- [ ] T074 [P] [US10] Write a failing contract test in `backend/src/test/java/com/wesolve/tickets/SearchContractTest.java`: `q` max length 200; `status` only `OPEN|IN_PROGRESS|RESOLVED|CLOSED|CANCELLED`; missing both matches `GET /api/v1/tickets` from US5; non-empty `q` without `TICKET_SEARCH` returns 403 while the no-query queue still needs only `TICKET_VIEW`
- [ ] T075 [P] [US10] Write a failing integration test in `backend/src/test/java/com/wesolve/tickets/SearchIT.java` using `plainto_tsquery('simple', :q)` so every word must appear somewhere in title, description, or comments

### Implementation for User Story 10

- [ ] T076 [US10] Apply `q` and `status` inside `backend/src/main/java/com/wesolve/tickets/TicketQueryService.java` without changing the no-parameter query. Depends on T074, T075
- [ ] T077 [US10] Add the keyword box, status choice (All, Open, In Progress, Resolved, Closed, Cancelled), and search control in `frontend/src/features/tickets/TicketQueuePage.tsx`, visible only with `TICKET_SEARCH`. Empty keyword plus All shows the full queue. Depends on T076

**Checkpoint**: Search narrows the same queue. The unfiltered queue is unchanged.

---

## Phase 13: User Story 11 - Manage people and their access (Priority: P1)

**Goal**: Users area and Access area. Spec 010. Ticket endpoints are not modified.

**Independent Test**: Create a person, add them to Support, and refuse removing the last active holder of Manage access (quickstart V20). A user manager cannot open Access.

### Tests for User Story 11

- [ ] T078 [P] [US11] Write failing contract tests for `/api/v1/users`, `/api/v1/groups`, `/api/v1/roles`, and `GET /api/v1/access-types` in `backend/src/test/java/com/wesolve/access/UserAdminContractTest.java`. `USER_MANAGE` can read users and cannot call group or role writes. `ACCESS_MANAGE` can. 409 `LAST_ADMIN` and 409 `DUPLICATE` stay distinct
- [ ] T079 [P] [US11] Write a failing integration test in `backend/src/test/java/com/wesolve/access/LastAdminIT.java`: deactivating the last active `ACCESS_MANAGE` holder, or emptying that grant, is refused and leaves the rows unchanged. `sign_in_id` cannot be updated (trigger from V2)

### Implementation for User Story 11

- [ ] T080 [US11] Implement user create and patch in `backend/src/main/java/com/wesolve/access/UserAdminService.java` and `UserAdminController.java`. Name `varchar(100)` required; email `varchar(254)` unique and matching `.+@.+`; sign-in id `varchar(100)` required on create and immutable after. New people start active and in no group. Keep `support_member.display_name` and `assignable` (`active AND can_be_assigned`) in the same transaction. Page size 25, maximum 100. Depends on T078, T079
- [ ] T081 [US11] Implement group and role writes in `backend/src/main/java/com/wesolve/access/AccessAdminService.java` and `AccessAdminController.java`. Group and role names `varchar(100)`, unique case-insensitively. An inactive group grants nothing. A group with members cannot be deleted (no delete endpoint). Actions are only the nine seeded codes. The last-admin check runs in the same transaction. Depends on T079
- [ ] T082 [US11] Add the Users area in `frontend/src/features/users/UsersPage.tsx`: list, search by name or email, add, edit. No password field and no delete control. Invalid fields keep the other typed values. Depends on T080
- [ ] T083 [US11] Add the Access area in `frontend/src/features/access/AccessPage.tsx` for groups, roles, and the fixed action list. Hidden without `ACCESS_MANAGE`; a direct open shows not allowed and saves nothing. Depends on T081
- [ ] T084 [US11] Show Manage users on the home only for `USER_MANAGE` or `ACCESS_MANAGE`, linking to `/users` in `frontend/src/features/dashboard/TicketHomePage.tsx`

**Checkpoint**: People and access can be managed. Ticket create, queue, detail, history, and search still behave as before.

---

## Phase 14: User Story 12 - Ask the assistant about past tickets (Priority: P4)

**Goal**: Grounded answers with citations. Spec 007. The feature stays off until `OPENAI_API_KEY` is set. Every other screen keeps working (spec 011 FR-016).

**Independent Test**: With no key, the assistant screen says it is unavailable and the home still loads (quickstart V17). With a stub model, a question that matches tickets returns real numbers and titles; a question that matches nothing does not call the chat model (quickstart V15, V16).

### Tests for User Story 12

- [ ] T085 [P] [US12] Write a failing contract test for `POST /api/v1/assistant/questions` in `backend/src/test/java/com/wesolve/assistant/AssistantContractTest.java`. Outcomes `ANSWERED`, `CONFLICT`, `NO_SUPPORT`. Question length 1 to 1,000. 403 without `TICKET_ASSISTANT`. 503 `ASSISTANT_UNAVAILABLE` when disabled
- [ ] T086 [P] [US12] Write a failing pipeline test in `backend/src/test/java/com/wesolve/assistant/CitationGuardTest.java` using stub `ChatModel` and `EmbeddingModel`: unknown ticket numbers are dropped; titles come from `ticket`; an answer with no remaining citation becomes `NO_SUPPORT`; `conflict=true` becomes `CONFLICT` and lists every citing ticket; zero hits above the threshold never call the chat model

### Implementation for User Story 12

- [ ] T087 [US12] Enqueue `embedding_task` rows in the ticket and comment transactions from `backend/src/main/java/com/wesolve/tickets/CreateTicketService.java`, `UpdateTicketService.java`, and `CommentService.java`. A title or description change enqueues a `TICKET` replacement. Do not change the HTTP response bodies of those methods
- [ ] T088 [US12] Add the outbox worker in `backend/src/main/java/com/wesolve/assistant/EmbeddingWorker.java`: `FOR UPDATE SKIP LOCKED`, 5 attempts, backoff starting at 30 seconds, then `FAILED`. Redact emails, phone numbers, and long digit runs before embedding. Depends on T028
- [ ] T089 [US12] Add the system prompt in `backend/src/main/resources/prompts/assistant-system.st` and the answer flow in `backend/src/main/java/com/wesolve/assistant/AssistantService.java`: `topK=8`, cosine threshold `0.55`, structured `{ answer, usedTicketNumbers, conflict }`, timeouts 20s chat and 5s embedding, one retry, Resilience4j circuit breaker. `app.assistant.enabled=false` until a provider key is present. Depends on T085, T086, T088
- [ ] T090 [US12] Expose `POST /api/v1/assistant/questions` from `backend/src/main/java/com/wesolve/assistant/AssistantController.java`. Require `TICKET_ASSISTANT`. Depends on T089
- [ ] T091 [US12] Add `frontend/src/features/assistant/AssistantPage.tsx` with a question box and Ask. No create, update, close, or comment control. References link to the ticket detail. Blank questions are not sent. A failure keeps the question text. Depends on T090

**Checkpoint**: The assistant answers only from stored tickets. With the key unset, only this screen reports unavailable.

---

## Phase 15: User Story 13 - Know whether the product is healthy (Priority: P2)

**Goal**: Dashboards, alerts, and a browser-to-database trace. Spec 011 story 3. Readiness already exists from T032; this phase must not change the `UP`/`DOWN` values that health check already returns.

**Independent Test**: Stop PostgreSQL and readiness is `DOWN`; restore it and readiness is `UP`. A ticket create shows one trace from the browser to the database (quickstart V23, V24).

### Tests for User Story 13

- [ ] T092 [P] [US13] Write a failing test in `backend/src/test/java/com/wesolve/platform/ReadinessIT.java`: readiness is `DOWN` when the database is unreachable and `UP` when it is back, while liveness stays `UP`
- [ ] T093 [P] [US13] Write a failing test in `frontend/src/telemetry/tracing.test.ts` that an API call sends `traceparent`

### Implementation for User Story 13

- [ ] T094 [US13] Add RED, JVM, pool, audit, and assistant panels as new files under `deploy/observability/grafana/dashboards/` and alerts (error rate, p95 latency, readiness, embedding queue depth, assistant failures) in `deploy/observability/grafana/alerts.yaml`. Labels must not include user ids, ticket numbers, or raw URLs
- [ ] T095 [US13] Fill the `observability` profile in `docker-compose.yml` with `grafana/otel-lgtm` and provision the dashboards from T094. Do not change the default `docker compose up` service list from T029
- [ ] T096 [US13] Add spans for ticket transitions, search, and assistant stages in `backend/src/main/java/com/wesolve/tickets/` and `backend/src/main/java/com/wesolve/assistant/AssistantService.java` without changing response bodies

**Checkpoint**: An operator can see health, dashboards, and one trace. Application behavior is unchanged.

---

## Phase 16: Polish & Cross-Cutting Concerns

**Purpose**: Prove the whole set still works and that no phase broke an earlier one.

- [ ] T097 [P] Add Playwright coverage for quickstart V1 through V25 in `frontend/tests/e2e/quickstart.spec.ts`, running against the compose stack
- [ ] T098 [P] Add the `auth` profile realm import with a client and no users in `deploy/keycloak/realm.json`, wired from `docker-compose.yml` without changing the default profile
- [ ] T099 Confirm a fresh database migrates V1 through V9 in order and that no file under `backend/src/main/resources/db/migration/` was modified after its phase, recorded in `backend/src/test/java/com/wesolve/platform/MigrationOrderIT.java`
- [ ] T100 Confirm `api/openapi.yaml` still matches `specs/011-platform-foundation/contracts/openapi.yaml` and that the only allowed difference from the original contract is none, via `backend/src/test/java/com/wesolve/platform/ContractUnchangedTest.java`
- [ ] T101 Run the image with `--cpus=1 --memory=1g` and record the home-screen result against quickstart V25 in `specs/011-platform-foundation/quickstart.md` (do not change scenario text)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup. Blocks every story.
- **US1 (Phase 3)**: Depends on Foundational. Blocks every later story because the schema and package come from here.
- **US2 (Phase 4)**: Depends on US1. Blocks every screen that needs a signed-in member.
- **US3 (Phase 5)**: Depends on US2. Later screens link back to this home and must not replace it.
- **US4 (Phase 6)**: Depends on US3.
- **US5 (Phase 7)**: Depends on US4.
- **US6 (Phase 8)**: Depends on US5. Creation rows already exist from US4.
- **US7 (Phase 9)**: Depends on US6.
- **US8 (Phase 10)**: Depends on US7. Adds optional `status` and `comment` only.
- **US9 (Phase 11)**: Depends on US6. Can follow US8 so the status comment and the free comment are not built in the same change.
- **US10 (Phase 12)**: Depends on US5 and US9, so comments are already in the search document.
- **US11 (Phase 13)**: Depends on US2. It does not depend on ticket stories. Do it after US10 so access screens are not in the same increment as search.
- **US12 (Phase 14)**: Depends on US4, US7, and US9 (indexing hooks) and on US1 (vector tables).
- **US13 (Phase 15)**: Depends on US1 for readiness and on US12 for assistant panels.
- **Polish (Phase 16)**: Depends on the stories you intend to ship.

### User Story Dependencies

- **US1**: After Foundational. No other story.
- **US2**: After US1. No ticket story.
- **US3**: After US2. Works with zero tickets.
- **US4**: After US3. First writer of tickets and history.
- **US5**: After US4. Read-only.
- **US6**: After US5. Read-only on top of US4 rows.
- **US7**: After US6. First `PATCH`, field subset only.
- **US8**: After US7. Optional fields on that `PATCH`.
- **US9**: After US8. New POST, no `PATCH` change.
- **US10**: After US9. Optional query parameters.
- **US11**: After US2 for a correct build; scheduled after US10.
- **US12**: After US9 and US11 so indexed comments and the assistant action exist.
- **US13**: After US12.

### Within Each User Story

- Tests are written and fail before implementation tasks in that phase.
- Migrations before entities, entities before services, services before controllers, controllers before screens.
- A story is checked at its checkpoint before the next phase starts.

### Parallel Opportunities

- Setup T002-T005 and T008 can run together after T001 starts the backend build.
- Foundational T010-T016 can run together after T009.
- The two test tasks at the start of each story phase can run together.
- US11 backend user tests and US10 frontend search can be written by different people only after US9 is done; do not merge them into one change.

---

## Parallel Example: User Story 4

```bash
# Tests together, both failing first:
Task: "T045 Contract test POST /api/v1/tickets in backend/src/test/java/com/wesolve/tickets/CreateTicketContractTest.java"
Task: "T046 Integration test CreateTicketIT.java"

# After both are red, entities then the service. The screen waits on the service.
```

---

## Implementation Strategy

### MVP First

1. Phase 1 Setup
2. Phase 2 Foundational
3. Phase 3 US1 package
4. Phase 4 US2 first administrator
5. Phase 5 US3 ticket home
6. Phase 6 US4 create ticket
7. Stop and run quickstart V1-V6. This is the first usable slice: an admin can sign in, see home, and create a ticket.

### Incremental Delivery

Each later phase adds one spec and leaves the previous checkpoints passing. Ship in this order: US5 view, US6 history, US7 update, US8 status, US9 comments, US10 search, US11 user management, US12 assistant, US13 monitoring, then polish.

### Non-breaking checks before moving on

- Replay the previous story's tests. They must still pass without edits.
- Diff `api/openapi.yaml` against the contract. No removals or renames.
- Diff `backend/src/main/resources/db/migration/`. Only the new version file, if that phase added one. Story phases US2 onward add no migration unless a checkpoint proves a new `V{n}` is required, and that file must only add objects.

---

## Notes

- [P] tasks use different files and do not need an unfinished task.
- Story labels match the story map at the top, not the spec folder number.
- Spec 011 is US1, US2, and US13. Specs 001-010 are one phase each.
- Do not introduce Kafka, WebSockets, or a second service. Embedding work stays on `embedding_task`.
- Commit after each phase checkpoint, not in the middle of a red test.
