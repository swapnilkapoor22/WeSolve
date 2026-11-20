# Feature Specification: Update a Ticket

**Feature Branch**: `003-update-ticket`

**Created**: 2026-11-18

**Status**: Draft

**Input**: User description: "Users can update title, description, priority, and assignee. All updates must be validated and saved."

## Clarifications

### Session 2026-11-20

- Q: Should unspecified choices remain in an assumptions list? → A: No. Each best guess is a requirement.
- Q: What screen behavior must be specified? → A: Fields, choices, messages, and empty, loading, and error states.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Update ticket details (Priority: P2)

On the ticket detail screen, a signed-in support team member edits the title, description, priority, or assignee and saves. Invalid values stay on the screen with a message beside the field. The status control is not part of this save.

**Why this priority**: Tickets are often logged with incomplete information and must be corrected without losing history or skipping the status rules.

**Independent Test**: On an existing ticket, change each editable field to a valid value and save, then repeat with invalid values and confirm the stored ticket did not change.

**Acceptance Scenarios**:

1. **Given** an existing ticket's detail screen, **When** a member changes its title, description, priority, or assignee to a valid value and saves, **Then** the screen shows the new values, the status is unchanged, and the last-updated time is later than it was before the change.
2. **Given** an existing ticket's detail screen, **When** a member saves a blank title, a blank description, or an assignee who is not a support team member, **Then** the message sits beside that field, the other fields keep what was typed, and the stored ticket is unchanged.
3. **Given** a member tries to open the edit for a ticket that does not exist, **When** the screen loads, **Then** they see a not-found message and no editable form.
4. **Given** two members save different valid changes to the same ticket, **When** both saves complete, **Then** the later valid save is the one shown the next time the ticket is opened.
5. **Given** an existing ticket, **When** a member picks a different support team member in the assignee choice and saves without changing the status, **Then** the screen shows the new assignee, the status stays the same, and the history shows the previous assignee and the new assignee.

### Edge Cases

- A title or description that is only spaces is treated as blank and rejected beside that field.
- A title longer than 150 characters, or a description longer than 5,000 characters, is rejected and the previous stored values remain.
- The assignee choice cannot be cleared. It lists existing support team members by name.
- Saving the same assignee does not add an assignment history entry.
- A valid field save does not move the status, including when the ticket is Closed or Cancelled. The status is visible and is not edited by this save.
- Title, description, and priority saves do not add a history entry.
- An unknown ticket ID shows a not-found message and does not create a ticket.
- While the save is in progress, the screen shows that it is saving. If the save fails, the typed values remain and an error is shown.
- A customer cannot open the edit controls.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A signed-in member who holds Update tickets MUST be able to edit any ticket. A customer, and a member without Update tickets, MUST NOT see the edit controls. This screen MUST NOT add or remove members.
- **FR-002**: The ticket detail screen MUST let the member edit Title, Description, Priority, and Assignee, and MUST provide a save control for those fields.
- **FR-003**: Priority MUST be a closed choice of Low, Medium, High, and Urgent. Assignee MUST be a closed choice of existing support team members by name and MUST NOT include an empty choice.
- **FR-004**: Title MUST be required and no longer than 150 characters. Description MUST be required and no longer than 5,000 characters. Values that are only spaces MUST be treated as blank.
- **FR-005**: A field save MUST NOT change the ticket's status. The status MUST remain visible and MUST NOT be part of this save control.
- **FR-006**: The system MUST reject a save when the title or description is blank or the assignee is not a support team member. The message MUST sit beside that field, the other fields MUST keep what was typed, and the stored ticket MUST stay unchanged.
- **FR-007**: Updating a ticket that does not exist MUST show a not-found message and MUST NOT show an editable form or create a ticket.
- **FR-008**: When two members save the same ticket, the later valid save MUST be the one kept and shown on the next open.
- **FR-009**: A successful save MUST show the new title, description, priority, and assignee on the detail screen, including a later last-updated time.
- **FR-010**: When the assignee changes from one member to another, the history MUST show the previous assignee and the new assignee, including when the status stays the same. Saving the current assignee MUST NOT add an assignment entry.
- **FR-011**: Title, description, and priority edits MUST NOT add a history entry.
- **FR-012**: The member MUST be able to edit and save from the keyboard. A failure MUST name the field it refers to. A failed save MUST keep the typed values.
- **FR-013**: The screen MUST show the shared header, including Home. Home MUST open the member's ticket home.

### Key Entities

- **Support team member**: A signed-in person on the support team who can update any ticket and can be chosen as the assignee.
- **Ticket**: An existing support issue whose title, description, priority, and assignee can change. Its ID and status do not change because of this save.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A member can correct a ticket's title, description, priority, or assignee on the detail screen and see the new values without leaving that ticket, in under 1 minute.
- **SC-002**: In acceptance testing, 100% of valid updates show the saved title, description, priority, and assignee on the detail screen, with the status unchanged.
- **SC-003**: In acceptance testing, 100% of invalid updates leave the stored ticket unchanged and show the explanation beside the invalid field.
