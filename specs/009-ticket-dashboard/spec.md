# Feature Specification: Ticket Home

**Feature Branch**: `009-ticket-dashboard`

**Created**: 2026-11-20

**Status**: Draft

**Input**: User description: "A dashboard of the tickets related to each user. This is the home screen after login, and a Home button returns the user to this page."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Land on my tickets (Priority: P1)

After a successful sign-in, a member sees their ticket home. It shows the tickets assigned to them, with a count for each status. A Home control on every signed-in screen returns to this page.

**Why this priority**: The member needs one place to resume their own work before they open the shared queue or create a ticket.

**Independent Test**: Sign in as a member who has assigned tickets in more than one status, confirm the home counts and list, open one ticket, then use Home and confirm the same page returns.

**Acceptance Scenarios**:

1. **Given** a member signs in successfully, **When** the session starts, **Then** the ticket home is the first screen they see.
2. **Given** the member is on any other signed-in screen, **When** they use Home, **Then** the ticket home opens.
3. **Given** tickets are assigned to the member, **When** the home opens, **Then** the list shows only those tickets, newest first, and each row shows the ticket ID, title, priority, status, assignee name, and created time.
4. **Given** the member has tickets in several statuses, **When** the home opens, **Then** a count is shown for Open, In Progress, Resolved, Closed, and Cancelled, and each count matches the list.
5. **Given** the member chooses a status count, **When** the list refreshes, **Then** only that member's tickets in that status are shown, and choosing All shows every assigned ticket again.
6. **Given** more than 25 assigned tickets match the current count, **When** the list is shown, **Then** it shows 25 per page and the member can move to the next and previous page.
7. **Given** a member who can view tickets chooses a row, **When** the row is activated, **Then** that ticket's detail screen opens.
8. **Given** the member has no assigned tickets, **When** the home opens, **Then** the counts are zero and the list says they have no assigned tickets. This is not an error.
9. **Given** the member's actions, **When** the home is shown, **Then** New ticket appears only with Create tickets, All tickets appears only with View tickets, and Manage users appears only with Manage users.

### Edge Cases

- Related tickets means tickets currently assigned to the signed-in member. Tickets they created but no longer hold are not in this list.
- Times are shown in the member's local time.
- While the home is loading, the screen shows that it is loading and does not show an empty list as if the member has no tickets. If loading fails, the member sees an error and a way to try again.
- A customer, and a person who is not signed in, cannot open the ticket home. They are shown the sign-in screen.
- A member without View tickets sees the counts and the list, and the rows do not open a ticket. The screen says they cannot open tickets.
- A member without Create tickets does not see New ticket. A member without View tickets does not see All tickets. A member without Manage users does not see Manage users.
- Sign out ends the session and shows the sign-in screen. Using Home after that shows the sign-in screen.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A successful sign-in MUST open the ticket home. The sign-in screen itself is not part of this feature.
- **FR-002**: Every signed-in screen MUST show a header with the member's name, a Home control, and a Sign out control. Home MUST open the ticket home. Sign out MUST end the session and show the sign-in screen.
- **FR-003**: The ticket home MUST list only tickets assigned to the signed-in member, newest created first. Each row MUST show the ticket ID, title, priority, status, assignee name, and created time in the member's local time.
- **FR-004**: The home MUST show a count for Open, In Progress, Resolved, Closed, Cancelled, and All. The count MUST equal the number of that member's tickets in that status. All MUST equal the full assigned list.
- **FR-005**: Choosing a count MUST filter the list to that status. Choosing All MUST show every assigned ticket. The chosen count MUST be visibly selected.
- **FR-006**: The list MUST show 25 tickets per page and MUST provide next and previous page controls until every matching ticket has been reachable.
- **FR-007**: A member who holds View tickets MUST be able to open a row onto that ticket's detail screen. A member without View tickets MUST see the rows and MUST NOT be able to open them, and the screen MUST say they cannot open tickets.
- **FR-008**: A member who holds Create tickets MUST see a New ticket control that opens the new-ticket screen. A member who holds View tickets MUST see an All tickets control that opens the shared queue. A member who holds Manage users MUST see a Manage users control that opens the user management screen. A member without the matching access MUST NOT see that control.
- **FR-009**: When the member has no assigned tickets, the list MUST say they have no assigned tickets and the counts MUST be zero. This MUST NOT be shown as an error.
- **FR-010**: While the home is loading, the screen MUST show that it is loading. If loading fails, the member MUST see an error and a way to try again.
- **FR-011**: The member MUST be able to use the counts, paging, row opening, New ticket, All tickets, Manage users, Home, and Sign out from the keyboard.
- **FR-012**: A person who is not signed in MUST NOT see the ticket home.

### Key Entities

- **Signed-in member**: The person whose name is shown in the header. The home lists tickets assigned to this person.
- **Assigned ticket**: A ticket whose current assignee is the signed-in member.
- **Status count**: The number of assigned tickets in one status, or in all statuses.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: After sign-in, the member sees their ticket home, including their name and their assigned-ticket counts, in under 5 seconds for a member with up to 10,000 assigned tickets, in 95% of attempts.
- **SC-002**: In acceptance testing, 100% of Home activations from another signed-in screen open this same page.
- **SC-003**: In acceptance testing, 100% of status counts match the assigned tickets shown when that count is selected.
- **SC-004**: A member can open one of their assigned tickets from the home, then return with Home, in under 1 minute.
