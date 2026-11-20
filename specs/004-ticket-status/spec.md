# Feature Specification: Ticket Status Management

**Feature Branch**: `004-ticket-status`

**Created**: 2026-11-18

**Status**: Draft

**Input**: User description: "The system must enforce status changes: OPEN to IN_PROGRESS to RESOLVED to CLOSED, and OPEN or IN_PROGRESS to CANCELLED. No other status transition is allowed. Incorrect transitions must show an appropriate error."

## Clarifications

### Session 2026-11-20

- Q: Should unspecified choices remain in an assumptions list? → A: No. Each best guess is a requirement.
- Q: What screen behavior must be specified? → A: Fields, choices, messages, and empty, loading, and error states.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Move a ticket through its lifecycle (Priority: P2)

On the ticket detail screen, a signed-in support team member changes the status with a control that offers only the current status and the moves allowed from it. Resolved and Cancelled ask for a comment before the change is accepted. Any other move is refused and explained on the screen.

**Why this priority**: Tracking work depends on a status everyone can trust. Skipping or reversing steps would make the queue misleading.

**Independent Test**: On the detail screen, walk one ticket through Open, In Progress, Resolved, and Closed, cancel separate tickets from Open and from In Progress, and attempt each disallowed change.

**Acceptance Scenarios**:

1. **Given** a ticket detail screen shows Open, **When** a member chooses In Progress and saves, **Then** the screen shows In Progress, the title, description, priority, and assignee stay the same, existing comments stay as they were, and the last-updated time moves forward.
2. **Given** a ticket detail screen shows In Progress, **When** a member chooses Resolved, enters a comment, and saves, **Then** the screen shows Resolved and that comment stays with the change.
3. **Given** a ticket detail screen shows Resolved, **When** a member chooses Closed and saves, **Then** the screen shows Closed. The comment box is optional.
4. **Given** a ticket detail screen shows Open or In Progress, **When** a member chooses Cancelled, enters a comment, and saves, **Then** the screen shows Cancelled and that comment stays with the change.
5. **Given** a ticket can move to Resolved or Cancelled, **When** a member chooses that status and saves with no comment, **Then** the message sits beside the comment box, the screen still shows the previous status, and nothing is saved.
6. **Given** a ticket detail screen, **When** a member submits a change that is not offered (including reopening, skipping a step, or cancelling a Resolved, Closed, or Cancelled ticket), **Then** the screen keeps the current status and explains that the change is not allowed.
7. **Given** a member requests a status change for a ticket that does not exist, **When** the screen loads, **Then** they see a not-found message and no status control.
8. **Given** a ticket detail screen shows In Progress, **When** a member leaves the status on In Progress and saves, **Then** the ticket is unchanged and no error is shown.

### Edge Cases

- The status choice for Open offers In Progress and Cancelled, plus the current status.
- The status choice for In Progress offers Resolved and Cancelled, plus the current status.
- The status choice for Resolved offers Closed, plus the current status. It does not offer Cancelled.
- The status choice for Closed or Cancelled offers only the current status.
- Open cannot move directly to Resolved or Closed. In Progress cannot move back to Open or directly to Closed.
- Choosing the status the ticket already has does not change the ticket and shows no error.
- A resolve or cancel whose comment is blank, only spaces, or longer than 2,000 characters is rejected beside the comment box, and the status stays unchanged.
- A comment included with Open to In Progress, or with Resolved to Closed, is kept with that change. Those two changes also succeed when the comment box is left empty.
- Two members request different status changes at the same time: the change is checked against the status stored at the time of the save. The screen of the refused member explains that the change is no longer allowed.
- An unknown ticket ID shows a not-found message and does not create a ticket.
- While the change is saving, the screen shows that it is saving. A second activation does not apply the change twice.
- A customer cannot open the status control. No approver step is shown.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A signed-in member who holds Change ticket status MUST be able to change the status of any ticket from its detail screen. A customer, and a member without Change ticket status, MUST NOT see the status control.
- **FR-002**: The status control MUST offer only the current status and the allowed next statuses: Open to In Progress, In Progress to Resolved, Resolved to Closed, Open to Cancelled, and In Progress to Cancelled.
- **FR-003**: Any other status change MUST be rejected, the screen MUST keep the current status, and the member MUST see an explanation that the change is not allowed. This includes skipping a step, moving backward, and cancelling a ticket that is Resolved, Closed, or Cancelled.
- **FR-004**: Leaving the status on its current value MUST leave the ticket unchanged and MUST NOT show an error.
- **FR-005**: A status change for a ticket that does not exist MUST show a not-found message and MUST NOT show a status control.
- **FR-006**: The allowed change MUST be decided from the status stored at the time of the save, not from a status the member saw earlier. The refused member MUST see that the change is no longer allowed.
- **FR-007**: A successful change MUST show the new status on the detail screen, and the last-updated time MUST move forward.
- **FR-008**: Changing the status MUST NOT change the title, description, priority, or assignee. Existing comments MUST stay as they were.
- **FR-009**: When the member chooses Resolved or Cancelled, the screen MUST show a required comment box. A non-blank comment of at most 2,000 characters MUST be entered before the change is saved. Without it, the message MUST sit beside the comment box and the status MUST stay unchanged.
- **FR-010**: When the member chooses In Progress from Open, or Closed from Resolved, the comment box MUST be optional. A comment that is included MUST be kept with that change. An empty comment box MUST still allow the change.
- **FR-011**: Closed and Cancelled MUST be final on the screen. Resolved MUST NOT offer Cancelled or a return to an earlier status. The screen MUST NOT reopen a ticket or change status automatically.
- **FR-012**: The member MUST be able to change the status from the keyboard. A failure MUST name the status control or the comment box it refers to.
- **FR-013**: The screen MUST show the shared header, including Home. Home MUST open the member's ticket home.

### Key Entities

- **Support team member**: A signed-in person on the support team who can change the status of any ticket.
- **Ticket**: An existing support issue whose status moves through Open, In Progress, Resolved, Closed, and Cancelled only as this screen allows.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A member can move a ticket one allowed step on its detail screen and see the new status in under 1 minute.
- **SC-002**: In acceptance testing, 100% of disallowed status changes leave the status shown unchanged and show an explanation on that screen.
- **SC-003**: In acceptance testing, 100% of allowed paths end in the expected status when a resolve or cancel includes a comment, and 100% of resolve or cancel attempts without a comment leave the status unchanged and show the message beside the comment box.
