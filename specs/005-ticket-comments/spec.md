# Feature Specification: Ticket Comments

**Feature Branch**: `005-ticket-comments`

**Created**: 2026-11-18

**Status**: Draft

**Input**: User description: "Users can add comments to existing tickets. Comments must be saved, linked to their tickets, and available to the assistant."

## Clarifications

### Session 2026-11-20

- Q: Should unspecified choices remain in an assumptions list? → A: No. Each best guess is a requirement.
- Q: What screen behavior must be specified? → A: Fields, choices, messages, and empty, loading, and error states.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Add comments (Priority: P3)

On the ticket detail screen, a signed-in support team member types a comment and adds it. The new comment appears on that ticket with the member's name and the time. A free comment does not change the status or the assignee. A comment typed while resolving or cancelling stays with that status change.

**Why this priority**: The investigation often lives in the conversation on the ticket, not only in the original description.

**Independent Test**: On an existing ticket, add a free comment, refresh the detail screen, and confirm the comment is only on that ticket and the status is unchanged.

**Acceptance Scenarios**:

1. **Given** a ticket detail screen, **When** a member types a comment and uses Add comment, **Then** the comment appears on that ticket with the member's name and the time, and the status and assignee stay the same.
2. **Given** a ticket has comments, **When** a member opens the ticket, **Then** those comments are shown on that ticket and not on any other ticket.
3. **Given** a member submits a blank comment, or opens a ticket that does not exist, **When** they try to add the comment, **Then** nothing is saved and the explanation is shown beside the comment box, or the screen shows not found and no comment box.
4. **Given** a ticket detail screen shows Closed or Cancelled, **When** a member adds a valid free comment, **Then** the comment appears and the status stays the same.
5. **Given** a member chooses Resolved or Cancelled and includes a comment, **When** the change is saved, **Then** that comment is shown with the status change and not as a free comment.
6. **Given** a saved comment is shown, **When** the member looks at it, **Then** there is no control to edit or delete it.

### Edge Cases

- A comment that is only spaces is treated as blank. The message sits beside the comment box and nothing is saved.
- A comment longer than 2,000 characters is rejected beside the box and nothing is saved.
- A comment cannot be moved to a different ticket. The screen does not offer that action.
- A free comment does not change the ticket's status, title, description, priority, or assignee.
- The comment box used with a status change is required for Resolved and Cancelled and optional for the other allowed status changes.
- An unknown ticket ID shows a not-found message, no comment box, and does not create a ticket.
- Comments cannot be edited or deleted. They stay for the life of the ticket.
- While the comment is saving, the screen shows that it is saving. If saving fails, the typed text remains and an error is shown.
- A customer cannot see the comment box.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A signed-in member who holds Comment on tickets MUST be able to add a comment on any ticket's detail screen, including a ticket that is Closed or Cancelled. A customer, and a member without Comment on tickets, MUST NOT see the comment box.
- **FR-002**: The detail screen MUST show a comment box and an Add comment control for a free comment.
- **FR-003**: Each free comment MUST be saved on that ticket, MUST show the signed-in member as the author and the time it was added, and MUST appear on the detail screen without a reload of a different ticket.
- **FR-004**: Saved comment text MUST remain available on that ticket for viewing, for search, and for the assistant for the life of the ticket.
- **FR-005**: A comment MUST be non-blank and no longer than 2,000 characters. A blank comment, including one that is only spaces, MUST be rejected beside the comment box and MUST NOT be saved.
- **FR-006**: Adding a comment to a ticket that does not exist MUST show a not-found message, MUST NOT show a comment box, and MUST NOT create a ticket.
- **FR-007**: A free comment MUST NOT change the ticket's status, title, description, priority, or assignee.
- **FR-008**: A shown comment MUST NOT have an edit control or a delete control.
- **FR-009**: The member MUST be able to save a comment together with a status change. That comment MUST stay shown with the change. The status screen's required comment for Resolved or Cancelled is that comment.
- **FR-010**: The member MUST be able to add a comment from the keyboard. A failure MUST name the comment box. A failed save MUST keep the typed text.
- **FR-011**: The screen MUST show the shared header, including Home. Home MUST open the member's ticket home.

### Key Entities

- **Support team member**: A signed-in person on the support team who writes a comment. The author shown is always the member who submitted it.
- **Ticket**: An existing support issue that owns zero or more comments.
- **Comment**: A note written by one member on one ticket, with the text and the time it was written. It stays attached to that ticket, either as a free comment or with one status change.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A member can add a comment on the detail screen and see it there, with their name and the time, in under 1 minute.
- **SC-002**: In acceptance testing, 100% of saved comments stay on the ticket they were added to and appear when that ticket's detail screen is opened again.
- **SC-003**: In acceptance testing, 100% of blank comments and comments on unknown tickets save nothing and show an explanation on the screen.
