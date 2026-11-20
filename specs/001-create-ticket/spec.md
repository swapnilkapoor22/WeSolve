# Feature Specification: Create a Ticket

**Feature Branch**: `001-create-ticket`

**Created**: 2026-11-18

**Status**: Draft

**Input**: User description: "Users can create a ticket with a title, description, priority and assignee. Every ticket gets a unique ticket ID. New tickets must have the status OPEN. Ticket details must be saved."

## Clarifications

### Session 2026-11-20

- Q: Should unspecified choices remain in an assumptions list? → A: No. Each best guess is a requirement.
- Q: What screen behavior must be specified? → A: Fields, choices, messages, and empty, loading, and error states.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create a ticket (Priority: P1)

A signed-in support team member opens a new-ticket screen, fills in the title, description, priority, and assignee, and submits it. The screen then shows the new ticket with a unique ID and the status Open.

**Why this priority**: Nothing else in the support workflow is useful until an issue can be recorded and kept.

**Independent Test**: Open the new-ticket screen, submit a valid ticket, and confirm the detail screen shows a unique ID, Open, and the same title, description, priority, and assignee.

**Acceptance Scenarios**:

1. **Given** a signed-in support team member on the new-ticket screen, **When** they enter a title, a description, a priority, and an assignee and submit, **Then** the ticket is saved, the member is shown that ticket's detail screen, and that screen shows a unique ID the member did not type and the status Open.
2. **Given** a ticket was just created, **When** the member leaves and opens that ticket again, **Then** the title, description, priority, assignee, status, and ID are unchanged.
3. **Given** two tickets are created, **When** their IDs are compared on their detail screens, **Then** the IDs are different.
4. **Given** the member submits a blank title, a blank description, or no assignee, **When** the screen checks the form, **Then** the message sits beside the invalid field, the other fields keep what was typed, and no ticket is created.
5. **Given** a person who is not a signed-in support team member, **When** they try to open the new-ticket screen, **Then** they cannot create a ticket.

### Edge Cases

- A title or description that is only spaces is treated as blank. The field message says it is required, and nothing is saved.
- A title longer than 150 characters, or a description longer than 5,000 characters, is rejected beside that field and nothing is saved.
- Priority offers only Low, Medium, High, and Urgent. The member cannot submit a priority outside that list.
- Assignee offers only existing support team members by name. The member cannot leave it empty or type a name that is not in the list.
- The screen has no ticket ID field. The member cannot choose or edit the ID.
- While the submit is in progress, the screen shows that it is saving and does not create a second ticket if the member activates submit again.
- If saving fails, the screen shows an error, keeps the typed values, and does not show a new ticket ID.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A signed-in member who holds Create tickets MUST be able to open a new-ticket screen and create a ticket. A customer, and a signed-in member without Create tickets, MUST NOT see that screen.
- **FR-002**: The screen MUST show four required fields: Title, Description, Priority, and Assignee. It MUST NOT show a ticket ID field, and it MUST NOT offer delete, attachments, notifications, due dates, or automatic assignment.
- **FR-003**: Title MUST be required and no longer than 150 characters. Description MUST be required and no longer than 5,000 characters. Values that are only spaces MUST be treated as blank.
- **FR-004**: Priority MUST be a closed choice of Low, Medium, High, and Urgent, with no empty choice.
- **FR-005**: Assignee MUST be a closed choice of existing support team members, shown by name, with no empty choice. This screen MUST NOT add or remove members.
- **FR-006**: The system MUST assign each new ticket a unique ID that the member does not choose, and that ID MUST stay the same for the life of the ticket.
- **FR-007**: Every new ticket MUST start with the status Open. The detail screen MUST show that status after creation.
- **FR-008**: The system MUST save each ticket so its details remain available when the member opens it again. The first history entry MUST record Open and the assignee chosen on this screen.
- **FR-009**: On an invalid submit, each invalid field MUST show its explanation beside that field, the other fields MUST keep what was typed, and no partial ticket MUST be saved.
- **FR-010**: After a valid submit, the member MUST see the new ticket's detail screen with its ID and the status Open.
- **FR-011**: The member MUST be able to complete the form from the keyboard. A failure MUST name the field it refers to.
- **FR-012**: A member with Create tickets MUST be able to assign a new ticket to any person who can be assigned tickets.
- **FR-013**: The screen MUST show the shared header, including Home. Home MUST open the member's ticket home.

### Key Entities

- **Support team member**: A signed-in person on the support team who creates a ticket and can be chosen as the assignee. This screen does not add or remove members.
- **Ticket**: A single support issue. On creation it has a unique ID, title, description, priority, assignee, status Open, and a created time.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A support team member can open the new-ticket screen, submit a valid ticket, and see its unique ID and Open status in under 1 minute.
- **SC-002**: In acceptance testing, 100% of new tickets have a unique ID and the status Open.
- **SC-003**: In acceptance testing, 100% of invalid create attempts save nothing, keep the other typed values, and show the explanation beside the invalid field.
