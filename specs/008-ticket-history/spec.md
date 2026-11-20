# Feature Specification: Ticket History

**Feature Branch**: `008-ticket-history`

**Created**: 2026-11-20

**Status**: Draft

**Input**: User description: "Keep a history for each ticket. Record every state transition and every comment. A comment may be tied to a state change or be free. Cancelling or resolving a ticket requires a comment. Record assignment from one member to another whether or not the state also changes."

## Clarifications

### Session 2026-11-20

- Q: Should unspecified choices remain in an assumptions list? → A: No. Each best guess is a requirement.
- Q: What screen behavior must be specified? → A: Fields, choices, messages, and empty, loading, and error states.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Read the history of a ticket (Priority: P1)

On the ticket detail screen, a signed-in support team member reads a history list from oldest to newest. Each entry shows the time, the member who acted, and only what changed: status, assignee, comment, or a combination. Entries cannot be edited or removed.

**Why this priority**: Resolve and cancel are not trustworthy unless the reason is kept with the change, and later readers need the assignment trail.

**Independent Test**: Create a ticket, move it forward, resolve it with a comment, reassign it, add a free comment, and read the history list from the first entry.

**Acceptance Scenarios**:

1. **Given** a member creates a ticket, **When** they open its detail screen, **Then** the history list's first entry shows status Open, the assignee they chose, the member who created it, and the time, with no earlier status or assignee.
2. **Given** a ticket moves along an allowed status change, **When** the member looks at the history list, **Then** a new entry shows the earlier status, the new status, who made the change, and when.
3. **Given** a member adds a comment without changing status or assignee, **When** they look at the history list, **Then** the entry shows that comment and does not show a status change or an assignee change.
4. **Given** a member resolves a ticket and includes a comment, **When** the history list is shown, **Then** one entry shows the change to Resolved and that same comment.
5. **Given** a member cancels a ticket and includes a comment, **When** the history list is shown, **Then** one entry shows the change to Cancelled and that same comment.
6. **Given** a member tries to resolve or cancel a ticket without a comment, **When** they save, **Then** the status on the screen stays the same, no history entry is added, and the message sits beside the comment box.
7. **Given** a ticket is assigned from one member to another and the status stays the same, **When** the history list is shown, **Then** the entry shows the previous assignee, the new assignee, and no status change.
8. **Given** a ticket changes status and assignee in the same save, **When** the history list is shown, **Then** one entry shows the earlier status, the new status, the previous assignee, and the new assignee.
9. **Given** several history entries exist, **When** a member reads the list, **Then** they appear oldest first, and no entry has an edit or delete control.
10. **Given** a member changes only the title, description, or priority, **When** the history list is shown, **Then** no new entry is added.
11. **Given** a member asks for a ticket that does not exist, **When** the detail screen loads, **Then** they see a not-found message, no history list, and no entry is written.

### Edge Cases

- A comment that is only spaces does not satisfy the comment required to resolve or cancel. The message sits beside the comment box.
- A comment longer than 2,000 characters is rejected. A resolve, cancel, or free comment in that attempt is not saved, and the status and assignee stay as they were.
- Open to In Progress, and Resolved to Closed, succeed without a comment. A comment included with either change is shown on that same entry. "Resolved" means the change into Resolved. "Cancelled" means the change into Cancelled. Closing does not require a comment.
- Choosing the current assignee is not an assignment change and adds no history entry by itself.
- A rejected status change writes no history entry.
- A free comment does not change status, assignee, title, description, or priority.
- Title, description, and priority edits are not history entries.
- One save writes one history entry, even when it changes both status and assignee and includes a comment.
- Two members saving the same ticket: only the save that is kept gets a history entry, and that entry matches the status and assignee stored.
- A ticket with only its creation entry shows that one entry, not an empty history message.
- While the history list is loading, the screen shows that it is loading and does not show an empty history. If loading fails, the member sees an error and a way to try again.
- A customer cannot see the history list. There is no control that edits or deletes an entry, and no automatic entry that a member did not cause.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The ticket detail screen MUST show a history list to a signed-in member who holds View tickets. A customer, and a member without View tickets, MUST NOT see it. The list MUST be ordered oldest first.
- **FR-002**: Each entry MUST show the time in the viewer's local time, the acting member's name, and only the parts that changed.
- **FR-003**: Creating a ticket MUST write the first history entry, showing status Open, the first assignee, the member who created it, and the time. The list MUST NOT show an earlier status or assignee for that entry.
- **FR-004**: Every successful status change MUST add one history entry showing the earlier status, the new status, the acting member, and the time. The allowed paths are Open to In Progress, In Progress to Resolved, Resolved to Closed, Open to Cancelled, and In Progress to Cancelled.
- **FR-005**: A comment MUST be either a free entry or attached to one status entry. A free entry MUST show the comment and MUST NOT show a status or assignee change. A comment attached to a status change MUST be shown on that same entry.
- **FR-006**: A change whose new status is Resolved or Cancelled MUST include a non-blank comment of at most 2,000 characters, entered in the comment box on the detail screen. Without that comment, the status and assignee MUST stay unchanged, no history entry MUST be written, and the message MUST sit beside the comment box.
- **FR-007**: Open to In Progress, and Resolved to Closed, MUST succeed with an empty comment box. A comment supplied with one of those changes MUST be shown on that entry. Closing MUST NOT require a comment.
- **FR-008**: Every change of assignee from one member to another MUST add a history entry showing the previous assignee and the new assignee. This applies when the status stays the same and when the status changes in the same save.
- **FR-009**: When status and assignee change in one save, the screen MUST add one history entry that shows both changes, plus the comment when one was supplied.
- **FR-010**: A history entry MUST contain at least one of: a status change, an assignee change, or a comment. A save that changes only title, description, or priority MUST NOT add an entry.
- **FR-011**: History entries MUST NOT be edited or deleted, and the list MUST NOT show an edit or delete control. Comment text on an entry MUST stay available for viewing, search, and the assistant.
- **FR-012**: A blank comment, a comment over 2,000 characters, or a request for an unknown ticket MUST be rejected with an explanation and MUST NOT write an entry. An unknown ticket MUST show not found and no history list.
- **FR-013**: While the history list is loading, the screen MUST show that it is loading. If loading fails, the member MUST see an error and a way to try again.
- **FR-014**: The member MUST be able to read the history list from the keyboard.
- **FR-015**: The screen MUST show the shared header, including Home. Home MUST open the member's ticket home.

### Key Entities

- **Ticket history entry**: One saved event on one ticket, shown as one row in the history list. It records the acting member, the time, and any of: earlier status and new status, previous assignee and new assignee, and comment text. It is permanent.
- **Free comment**: A history row whose only change is the comment text.
- **State comment**: A comment shown on the same row as a status change. It is required when the new status is Resolved or Cancelled, and optional for Open to In Progress and Resolved to Closed.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A member can open a ticket and read its full history list, oldest first, in under 1 minute.
- **SC-002**: In acceptance testing, 100% of successful status changes and assignee changes appear in the history list with the earlier value, the new value, the actor, and the time.
- **SC-003**: In acceptance testing, 100% of resolve and cancel attempts without a comment leave the status unchanged, add no history entry, and show the message beside the comment box.
- **SC-004**: In acceptance testing, 100% of free comments appear as history rows without a status or assignee change, and 100% of resolve and cancel comments stay on the row for that change.
