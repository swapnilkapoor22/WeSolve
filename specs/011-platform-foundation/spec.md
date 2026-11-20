# Feature Specification: Platform Foundation

**Feature Branch**: `011-platform-foundation`

**Created**: 2026-11-20

**Status**: Draft

**Input**: User description: "Create the project as one web application for the browser plus a service behind it, delivered as a single package that runs on a laptop and on a lightweight server, using default settings wherever a value is needed."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Run the whole product from one package (Priority: P1)

An operator builds one package, starts it together with its database using a single command on a laptop, and opens the product in a browser. The same package runs on a small server when pointed at a database.

**Why this priority**: None of the ticket, history, dashboard, assistant, or user-management screens can be used until the product starts.

**Independent Test**: Build the package on a clean machine, start the local setup, open the product address in a browser, and confirm the sign-in screen appears and the starting groups, roles, and actions exist.

**Acceptance Scenarios**:

1. **Given** the project and the default settings, **When** the operator builds the package and starts the local setup with one command, **Then** the product is reachable in a browser within 2 minutes of that command and shows the sign-in screen.
2. **Given** an empty database, **When** the product starts for the first time, **Then** the data structure and the starting groups, roles, and actions exist without any manual step.
3. **Given** data has been saved, **When** the product is stopped and started again, **Then** all saved tickets, history, people, and access settings are still there.
4. **Given** the same package and the address of a database, **When** it is started on a server with 1 CPU and 1 GB of memory, **Then** it behaves the same as the local setup.
5. **Given** a new version of the package, **When** it starts against an older database, **Then** the structure is upgraded automatically, the upgrade is recorded, and existing data is kept.

---

### User Story 2 - The first administrator gets in (Priority: P1)

The operator names the first administrator before the first start. That person signs in with the organization's sign-in and lands on the ticket home with user and access management available. Everyone else must be added by an administrator or a user manager before they can use the product.

**Why this priority**: Without a first administrator nobody can add people, so nobody else can ever sign in.

**Independent Test**: Start with an empty database and a named first administrator. Sign in as that person, add a second person, sign in as the second person, and sign in as someone who was never added.

**Acceptance Scenarios**:

1. **Given** a named first administrator and no active administrator, **When** that person signs in, **Then** they are created as an active administrator and the ticket home opens.
2. **Given** at least one active administrator exists, **When** the named first administrator setting is still present, **Then** it grants nothing to anyone who is not already in the product.
3. **Given** someone who was never added, **When** they sign in successfully with the organization's sign-in, **Then** they see that they do not have access and see no ticket, user, or assistant screens.
4. **Given** a signed-in member, **When** they use Sign out, **Then** the session ends and the sign-in screen appears.
5. **Given** a person who has been deactivated, **When** they try to use the product, **Then** they see that they do not have access.

---

### User Story 3 - Know whether the product is healthy (Priority: P2)

An operator can tell whether the product is running and ready, and can see request rates, errors, response times, and assistant response times on monitoring dashboards. Each request can be followed from the browser through the service to the database.

**Why this priority**: Operators need to find a failing screen or a slow answer before members report it.

**Independent Test**: Stop the database and confirm the product reports not ready. Restore it and confirm ready. Open the dashboards after browsing a few screens and confirm those requests appear.

**Acceptance Scenarios**:

1. **Given** the product and its database are working, **When** the operator checks readiness, **Then** it reports ready.
2. **Given** the database cannot be reached, **When** the operator checks readiness, **Then** it reports not ready, and reports ready again once the database is back.
3. **Given** members have used several screens, **When** the operator opens the dashboards, **Then** request rate, error rate, and response time for those screens are visible.
4. **Given** a member asked the assistant a question, **When** the operator opens the dashboards, **Then** the assistant's response time and the amount of text it processed are visible.
5. **Given** a slow request, **When** the operator follows it, **Then** the steps from the browser to the database are visible as one trace.

### Edge Cases

- Starting without a database address reports a clear error and does not start serving screens.
- Starting when the database is not yet ready waits and retries for up to 60 seconds before reporting an error.
- A secret value is never part of the package. Starting without a required secret reports which setting is missing, not its value.
- If the assistant provider is not configured, every other screen works. The assistant screen shows its error state from the assistant spec.
- A second copy of the product started against the same database does not duplicate the starting groups, roles, or actions.
- Opening any address that is not a screen address inside the product returns the product's own screen or a not-found response, never a raw server error.
- Monitoring dashboards and traces are optional to run locally. Their absence does not stop the product.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The product MUST be delivered as one package containing every screen and the service behind them.
- **FR-002**: The product MUST start with default settings plus a database address and any required secret, and MUST report which required setting is missing.
- **FR-003**: A single command MUST start the product together with its database on a laptop.
- **FR-004**: The same package MUST run on a server with 1 CPU and 1 GB of memory against a database it is pointed at.
- **FR-005**: On first start against an empty database, the product MUST create its data structure and the starting groups, roles, and actions from the user-management spec without a manual step. Starting again MUST NOT duplicate them.
- **FR-006**: Saved data MUST survive stopping and starting the product and upgrading the package. Structure upgrades MUST run automatically at start and MUST be recorded.
- **FR-007**: Every setting that differs between machines MUST come from the environment. Secrets MUST NOT be stored in the package.
- **FR-008**: People MUST sign in with the organization's sign-in. A successfully signed-in person who has not been added, or who is deactivated, MUST see that they do not have access and MUST NOT see ticket, user, or assistant screens.
- **FR-009**: The operator MUST be able to name the first administrator's sign-in id before the first start. That person MUST become an active administrator on first sign-in only while no active administrator exists.
- **FR-010**: Sign out MUST end the session and show the sign-in screen.
- **FR-011**: Every change to saved business data MUST be recorded with who made it, when, the values before and after, and a position in one ordered sequence that never repeats. Recorded changes MUST NOT be editable or deletable.
- **FR-012**: Every structure upgrade MUST be recorded, so the current structure version can be read.
- **FR-013**: The product MUST report whether it is alive and whether it is ready. Ready MUST require a reachable database.
- **FR-014**: The product MUST expose request rate, error rate, response time, and assistant response time for monitoring, and MUST provide dashboards and alerts for them.
- **FR-015**: A request MUST be traceable from the browser through the service to the database, and every log line MUST carry the request's trace identifier.
- **FR-016**: The assistant MUST work only when its provider is configured. Without it, only the assistant screen reports an error.
- **FR-017**: Addresses that are not API addresses and not static files MUST show the product's own screen so that a browser refresh on any screen works.

### Key Entities

- **Operator**: The person who builds, starts, configures, and monitors the product. Not a member.
- **First administrator**: The person named by the operator who becomes the first active administrator.
- **Change record**: One saved entry about a change to business data, with who, when, before and after values, and its place in one ordered sequence.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: From a built package, the local setup shows the sign-in screen within 2 minutes in 95% of starts.
- **SC-002**: On a 1 CPU, 1 GB server, the package is ready within 60 seconds in 95% of starts.
- **SC-003**: In acceptance testing, 100% of changes to saved business data have a change record with who, when, before, and after, and the records are in one unbroken, ordered sequence.
- **SC-004**: In acceptance testing, 100% of saved tickets, history, people, and access settings are present after a restart and after an upgrade.
- **SC-005**: In acceptance testing, a person who was never added is refused on 100% of sign-ins, and the named first administrator is admitted only while no active administrator exists.
- **SC-006**: In acceptance testing, readiness reports not ready within 15 seconds of the database becoming unreachable and ready within 30 seconds of it returning.
