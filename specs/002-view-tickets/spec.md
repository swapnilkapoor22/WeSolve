# Feature Specification: List and View Tickets

**Feature Branch**: `002-view-tickets`

**Created**: 2026-11-18

**Status**: Draft

**Input**: User description: "Users can view all tickets and individual ticket details. Ticket details must include comments and timestamps. Show a meaningful error if a ticket does not exist."

## Clarifications

### Session 2026-11-20

- Q: Should unspecified choices remain in an assumptions list? → A: No. Each best guess is a requirement.
- Q: What screen behavior must be specified? → A: Fields, choices, messages, and empty, loading, and error states.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - List and view tickets (Priority: P1)

A signed-in support team member opens the queue, moves through pages of tickets, and opens one ticket. The detail screen shows the ticket fields, timestamps, and comments. An unknown ticket shows a not-found message instead of a blank ticket.

**Why this priority**: The team cannot manage or track work they cannot see.

**Independent Test**: With tickets already saved, open the queue, move past the first page, open a ticket with comments, open a ticket with none, and open an ID that does not exist.

**Acceptance Scenarios**:

1. **Given** several saved tickets, **When** a member opens the queue, **Then** each row shows the ticket ID, title, priority, status, assignee, and created time, newest first, 25 rows per page.
2. **Given** more than 25 tickets, **When** the member uses the page control, **Then** they can reach every later page and return to an earlier page.
3. **Given** a saved ticket that has comments, **When** a member opens it from the queue, **Then** the detail screen shows its ID, title, description, priority, assignee, status, created time, last-updated time, and each comment with its text, author, and time, in the member's local time.
4. **Given** a saved ticket with no comments, **When** a member opens it, **Then** the ticket details are shown and the comment area says there are no comments yet, which is not an error.
5. **Given** a member requests a ticket ID that does not exist, **When** the detail screen opens, **Then** it shows that the ticket was not found and shows no ticket fields.
6. **Given** a ticket is Closed or Cancelled, **When** a member opens the queue and then that ticket, **Then** the row and the detail screen show that status and the rest of the ticket.
7. **Given** the queue is still loading, **When** the member looks at the list, **Then** the screen shows that it is loading and does not show an empty queue as if no tickets exist.

### Edge Cases

- A queue longer than 25 tickets is shown in successive pages, and the member can reach every ticket.
- Closed and Cancelled tickets remain in the list and can be opened.
- Times are shown in the viewing member's local time, including comment times.
- An unknown ticket ID shows a not-found message and does not display an empty ticket as if it existed.
- If the queue or the detail screen cannot be loaded, the member sees an error and a way to try again. No blank ticket is shown.
- A customer cannot open the queue or a ticket detail screen.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A signed-in member who holds View tickets MUST be able to open the queue and every ticket. A customer, and a member without View tickets, MUST NOT see these screens. Tickets are not private to the assignee.
- **FR-002**: The queue MUST list tickets newest created first. Each row MUST show the ticket ID, title, priority, status, assignee name, and created time.
- **FR-003**: The queue MUST show 25 tickets per page and MUST provide a control to move to the next and previous page until every ticket has been reachable.
- **FR-004**: Choosing a row MUST open that ticket's detail screen.
- **FR-005**: The detail screen MUST show the ticket ID, title, description, priority, assignee name, status, created time, and last-updated time.
- **FR-006**: The detail screen MUST show each comment with its text, author name, and time. When there are no comments, it MUST say that there are no comments yet and MUST NOT treat that as an error.
- **FR-007**: Times on the queue and the detail screen MUST be shown in the viewing member's local time.
- **FR-008**: When a member requests a ticket that does not exist, the detail screen MUST show a not-found message and MUST NOT show empty ticket fields.
- **FR-009**: Tickets in every status, including Closed and Cancelled, MUST remain listed and openable. The status shown MUST be Open, In Progress, Resolved, Closed, or Cancelled.
- **FR-010**: While the queue or detail screen is loading, the member MUST see that it is loading. If loading fails, the member MUST see an error and a way to try again.
- **FR-011**: The member MUST be able to move through the queue and open a ticket from the keyboard.
- **FR-012**: The screen MUST show the shared header, including Home. Home MUST open the member's ticket home.

### Key Entities

- **Support team member**: A signed-in person on the support team who can view every ticket.
- **Ticket**: A saved support issue with a unique ID, title, description, priority, status, assignee, created time, and last-updated time.
- **Comment**: A note already stored on a ticket. This screen shows comments; it does not add them.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A member can open the queue and then open a known ticket in under 1 minute.
- **SC-002**: A member can read a ticket's full comment list, with author and time for each comment, without leaving that ticket's detail screen.
- **SC-003**: In acceptance testing, 100% of requests for an unknown ticket ID show a not-found message and no ticket fields.
- **SC-004**: In acceptance testing, every saved ticket appears in the queue, including tickets past the first page of 25.
