# Implementation Plan: WeSolve Platform (all specs 001-011)

**Branch**: `011-platform-foundation` (no git branch is created; sequential numbering is used for directories only) | **Date**: 2026-11-20 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/011-platform-foundation/spec.md`, plus specs 001-010.

**Scope note**: This is one plan for the whole product. The platform spec (011) owns this directory so the plan, research, data model, contract, and quickstart sit in one place. Specs 001-010 are delivered by the same plan; the table below shows where each lands.

## Summary

WeSolve is a single page application for support teams to create, update, search, and track tickets, with an assistant that answers only from past tickets and cites them. The frontend is React with Redux Toolkit. The backend is a Java 21, Spring Boot modular monolith that serves the frontend and a REST API under `/api/v1`. PostgreSQL holds all data, including vectors through pgvector. The whole product builds into one Docker image that runs on a one-CPU, 1 GB server or under local Docker Compose.

Approach (full reasoning in [research.md](research.md)):

- One Gradle project with four packages (`platform`, `access`, `tickets`, `assistant`), with boundaries enforced by an ArchUnit test.
- Access comes from the database: user -> group -> role -> action. Sign-in is OIDC; the API validates tokens and loads actions per request.
- Every database change goes through triggers into a watermarked audit log. Ticket history is an insert-only table holding status changes, assignee changes, and comments.
- The assistant retrieves from pgvector, asks the model for structured output, and drops any citation that does not exist in the database. Embeddings are queued through a Postgres outbox.
- No Kafka and no WebSockets; no spec needs them (see Constitution Check).

### Spec coverage

| Spec | Delivers | Backend module | Frontend feature |
|---|---|---|---|
| 001 Create ticket | Create form and API | `tickets` | `features/tickets` |
| 002 View tickets | Queue, paging, detail | `tickets` | `features/tickets` |
| 003 Update ticket | Field and assignee edits | `tickets` | `features/tickets` |
| 004 Ticket status | Legal next steps, required comment | `tickets` | `features/tickets` |
| 005 Ticket comments | Free comments | `tickets` | `features/tickets` |
| 006 Search and filter | Keyword and status search | `tickets` | `features/tickets` |
| 007 Ticket assistant | Question, answer, citations | `assistant` | `features/assistant` |
| 008 Ticket history | Immutable history list | `tickets` | `features/tickets` |
| 009 Ticket dashboard | Home screen, counts, header | `tickets`, `access` | `features/dashboard` |
| 010 User management | Users, groups, roles, actions | `access` | `features/users`, `features/access` |
| 011 Platform foundation | Packaging, sign-in, first admin, audit, monitoring | `platform`, `access` | `features/auth` |

### Suggested implementation order

1. Platform skeleton: Gradle build, Docker image, compose, Flyway V1, health, tracing, ArchUnit.
2. Access and sign-in (011, part of 010): `users`, groups, roles, seed data, first admin, `/me`, `auth` feature, app shell.
3. Tickets core (001, 002, 008): create, queue, detail, history.
4. Ticket changes (003, 004, 005): combined PATCH, comments, status guard.
5. Dashboard (009) and search (006).
6. User management screens (010).
7. Assistant (007): vector store, outbox, retrieval, citation guard.
8. Observability dashboards and alerts, then the image-size and startup checks.

Each step is releasable. `/speckit-tasks` turns this order into tasks.

## Technical Context

**Language/Version**: Java 21 (backend), TypeScript 5.x strict (frontend)

**Primary Dependencies**:

- Backend: Spring Boot 3.5.x, Spring Web, Spring Security OAuth2 resource server, Spring Data JPA, Spring AI 1.1.x (OpenAI chat and embedding, pgvector store), Flyway, PostgreSQL JDBC driver, Lombok, Micrometer tracing with the OpenTelemetry bridge, Resilience4j (the contract is a hand-written `openapi.yaml`, so no springdoc)
- Frontend: React 18+, Redux Toolkit with RTK Query, React Router, `oidc-client-ts`, Vite, OpenTelemetry Web SDK
- Dependency additions are limited to what [research.md](research.md) justifies; every version is pinned and locked.

**Storage**: PostgreSQL 17 with `pgvector`, via `pgvector/pgvector:pg17`

**Testing**: JUnit 5, Mockito, Spring slice tests, Testcontainers, ArchUnit, contract tests against `openapi.yaml`; Vitest, React Testing Library, MSW, Playwright

**Target Platform**: Linux container (`eclipse-temurin:21-jre-alpine`), amd64 and arm64; browser SPA

**Project Type**: Web application served as a single image (the backend serves the built frontend)

**Performance Goals** (from the specs): search within 2 seconds, dashboard within 5 seconds, assistant answer within 10 seconds at p95, startup within 60 seconds on 1 CPU and 1 GB

**Constraints**: One image; one CPU and 1 GB must be enough; plain HTTP only on localhost in the local compose setup; TLS terminated by a reverse proxy elsewhere; no secrets in the repository or image; every database change audited

**Scale/Scope**: About 10,000 tickets, tens of concurrent members, 11 screens, 23 API operations

All items are resolved; no `NEEDS CLARIFICATION` remains.

## Constitution Check

*GATE: Passed before research and re-checked after design.*

| Principle | Status | How the plan meets it |
|---|---|---|
| I. Spec-driven development | Pass | Eleven specs with requirement checklists; this plan maps all of them; tasks come next |
| II. Test-first | Pass | Test layers listed above; tasks will order tests before code |
| III. Java 21 and Spring Boot | Pass with risk | Boot 3.5.x, Flyway, `ddl-auto=validate`, `open-in-view=false`, ProblemDetail. Verify the Boot 3.5 support window at scaffold time; a move to Boot 4 would need a constitution amendment |
| IV. API-first REST | Pass | [contracts/openapi.yaml](contracts/openapi.yaml) is written before code; `/api/v1`; `Idempotency-Key` on create, comment, and patch; pagination on lists |
| V. Kafka and message queues | Not applicable | No spec needs a broker. Embedding work uses a Postgres outbox, documented in R14 |
| VI. WebSockets | Not applicable | No spec needs push. Screens refresh on user action |
| VII. Spring AI | Pass with gate | Structured output, citation guard, redaction, timeouts, circuit breaker, switch `app.assistant.enabled`. Sending ticket text to an external provider needs documented approval before enabling in a shared environment |
| VIII. React and Redux | Pass | RTK Query client generated from the contract, strict TypeScript, tokens in memory only |
| IX. Observability | Pass | Tracing from browser to database, JSON logs with trace id, dashboards and alerts as code |
| X. Security and secrets | Pass | OIDC, per-request authorization from the database, no committed secrets, non-root container, port 8081 kept private |
| XI. Simplicity | Pass | One deployable, no extra services beyond PostgreSQL, derived tables rebuildable |

**Post-design re-check**: The design adds no new violations. The data model confirms no broker or socket is needed, and the contract keeps the one combined `PATCH` so one save is one history entry.

## Project Structure

### Documentation (this feature)

```text
specs/011-platform-foundation/
├── plan.md              # This file
├── research.md          # Decisions R1-R20
├── data-model.md        # Tables, rules, migrations
├── quickstart.md        # Run and validation guide
├── contracts/
│   └── openapi.yaml     # REST contract
└── tasks.md             # Created by /speckit-tasks
```

### Source Code (repository root)

```text
backend/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.lockfile
└── src/
    ├── main/
    │   ├── java/com/wesolve/
    │   │   ├── WeSolveApplication.java
    │   │   ├── platform/      # security config, problem details, idempotency, audit actor, SPA forwarding, observability
    │   │   ├── access/        # users, groups, roles, actions, members, bootstrap
    │   │   ├── tickets/       # tickets, history, search, dashboard
    │   │   └── assistant/     # retrieval, answer generation, citation guard, embedding outbox
    │   └── resources/
    │       ├── application.yml
    │       ├── db/migration/  # V1..V9 (see data-model.md)
    │       └── prompts/       # assistant prompt templates
    └── test/
        └── java/com/wesolve/  # unit, slice, integration, architecture, contract

frontend/
├── package.json
├── package-lock.json
├── vite.config.ts
└── src/
    ├── app/                   # store, router, app shell and header
    ├── api/                   # RTK Query client generated from openapi.yaml
    ├── features/
    │   ├── auth/
    │   ├── dashboard/
    │   ├── tickets/
    │   ├── assistant/
    │   ├── users/
    │   └── access/
    └── telemetry/
frontend/tests/                # Vitest, MSW handlers, Playwright specs

api/openapi.yaml               # Copied from this contract at scaffold time; contract tests read it
deploy/observability/grafana/  # dashboards and alert rules as code
Dockerfile                     # node build -> gradle build -> temurin JRE, non-root
docker-compose.yml             # app, postgres; profiles: auth, observability
.env.example                   # names and safe defaults, no secrets
```

**Structure Decision**: A web application with `backend/` and `frontend/`, built into one image. The build copies the Vite output into the backend's static resources so one process serves both. The backend stays one Gradle project with package-level modules because the user asked for a modular monolith and the constitution prefers simplicity; the ArchUnit test makes the boundaries real.

## Complexity Tracking

No constitution violations need justification. Two deliberate exceptions are recorded so they are visible:

| Item | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| Plain HTTP between browser and app on localhost in the local compose setup | Local one-command setup without certificates | TLS everywhere would block the "start locally with one command" requirement; production uses a TLS reverse proxy |
| Ticket text sent to an external LLM provider | The assistant needs a hosted model to answer | A local model would not meet the 1 CPU and 1 GB target; the assistant is off until a key is set and needs documented approval before shared use |

## Open risks

| Risk | Handling |
|---|---|
| Spring Boot 3.5 open-source support window may be near its end at implementation time | Check at scaffold; amend constitution III if moving to Boot 4 |
| Counter-row watermark serializes concurrent writes | Accepted for the target scale (tens of members); measured in a load test |
| Ticket text may contain personal data and goes to the LLM provider | Redaction before embedding and prompting; approval gate; `app.assistant.enabled=false` by default |
| `support_member` duplicates data from `users` | Updated in the same transaction; a test asserts they never differ |

## Follow-ups outside this plan

- Replace the two open items in the constitution: build tool is Gradle and sign-in is OIDC. Kafka schema format stays unset because no broker is used. Do this through `/speckit-constitution`.
- Remove the Sync Impact Report comment at the top of the constitution before committing.
