# Feature Specification: User Management

**Feature Branch**: `010-user-management`

**Created**: 2026-11-20

**Status**: Draft

**Input**: User description: "A user management page for admin and user manager roles."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Manage people and their access (Priority: P1)

An admin or a user manager opens the user management screen, finds a person, and updates their name, email, groups, and whether they can be assigned tickets. An admin can also change groups, roles, and which actions a role allows. A user manager cannot change that access setup. A support member without either role never sees the screen.

**Why this priority**: Ticket work depends on knowing who may sign in, who may be assigned, and which actions they may take.

**Independent Test**: Sign in as a user manager and add a person to the Support group. Sign in as an admin and remove Create tickets from a test role. Sign in as a support agent and confirm the user management screen is unavailable.

**Acceptance Scenarios**:

1. **Given** a member holds Manage users, **When** they use Manage users, **Then** the user management screen opens on a list of people.
2. **Given** a member holds neither Manage users nor Manage access, **When** they look at the header, **Then** Manage users is absent. If they open the screen directly, they see that they are not allowed, and no person is changed.
3. **Given** the user list, **When** an admin or user manager searches by name or email, **Then** the list shows matching people, 25 per page, with name, email, active state, groups, and whether they can be assigned tickets.
4. **Given** the add form, **When** an admin or user manager enters a name, an email, and a sign-in id and saves, **Then** the person appears in the list, is active, and is in no group until groups are chosen.
5. **Given** an existing person, **When** a user manager changes the name, email, active state, groups, or "Can be assigned tickets" and saves, **Then** those changes are shown on the next open of that person. The sign-in id stays as it was first saved.
6. **Given** a user manager, **When** they view the screen, **Then** they do not see a way to add a group, add a role, or change which actions a role allows.
7. **Given** an admin on the Access area, **When** they create a group, create a role, attach the role to the group, and choose the actions that role allows, **Then** a person placed in that group receives those actions and no others from that group.
8. **Given** a person is in more than one group, **When** their actions are checked, **Then** they can do an action if any of their groups' roles allows it.
9. **Given** the last active admin, **When** someone tries to deactivate that person or remove Manage access from every role that person still holds, **Then** the change is refused and the screen explains that one active admin must remain.
10. **Given** a new installation, **When** an admin opens Access, **Then** Support, User managers, and Administrators already exist with the Support agent, User manager, and Admin roles described for those groups.

### Edge Cases

- Name is required and no longer than 100 characters. Email is required, must contain a name and a domain, and must be unique. Sign-in id is required and unique. A blank or only-spaces value is rejected beside that field, and nothing is saved.
- The sign-in id cannot be edited after the person is created.
- Deactivating a person stops them signing in and removes them from new assignee choices. Their name remains on tickets and history already stored. There is no delete control.
- Turning off "Can be assigned tickets" removes the person from new assignee choices and leaves tickets already assigned to them unchanged.
- A group that still has people cannot be removed. The admin must take the people out of the group first, or mark the group inactive. An inactive group grants no actions and cannot be newly chosen.
- The action list is fixed: Create tickets, View tickets, Update tickets, Change ticket status, Comment on tickets, Search tickets, Use the assistant, Manage users, and Manage access. An admin cannot invent a tenth action.
- A user manager who opens the Access area directly sees that they are not allowed, and no group, role, or action is changed.
- While the list is loading, the screen shows that it is loading. A failed save keeps the typed values and shows an error. Nothing matched in search says that nobody matched, which is not an error.
- The shared header, including Home and Sign out, is shown on this screen.
- The screen does not set or show a password. Sign-in is already provided elsewhere.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The user management screen MUST be available to a signed-in member who holds Manage users or Manage access. The Users area MUST be available to both. The Access area MUST be available only to a member who holds Manage access.
- **FR-002**: A signed-in member without Manage users and without Manage access MUST NOT see Manage users. Opening the screen directly MUST show that they are not allowed and MUST NOT change any person, group, or role.
- **FR-003**: A user manager MUST NOT see controls to add or change groups, roles, or the actions on a role. Opening the Access area directly MUST show that they are not allowed.
- **FR-004**: The Users area MUST list people with name, email, whether they are active, their groups, and whether they can be assigned tickets. The list MUST show 25 people per page, with next and previous controls.
- **FR-005**: A search box MUST narrow the list by name or email. An empty search MUST show the full list. No match MUST say that nobody matched and MUST NOT be an error.
- **FR-006**: Add and edit MUST collect name, email, active state, groups, and "Can be assigned tickets". Add MUST also collect the sign-in id. After creation, the sign-in id MUST be shown and MUST NOT be editable.
- **FR-007**: Name MUST be required and no longer than 100 characters. Email MUST be required, unique, and formed as a name plus a domain. Sign-in id MUST be required and unique. An invalid field MUST show its message beside that field, the other fields MUST keep what was typed, and nothing MUST be saved.
- **FR-008**: A new person MUST start active and in no group. They MUST gain an action only by being placed in a group whose role allows that action.
- **FR-009**: A person in several groups MUST be allowed an action when any selected group's role allows it.
- **FR-010**: Deactivation MUST stop sign-in and MUST remove the person from new assignee choices. The screen MUST NOT offer delete. The person's name MUST remain on existing tickets and history.
- **FR-011**: "Can be assigned tickets" MUST control whether the person appears in assignee choices. Turning it off MUST NOT change tickets already assigned to them.
- **FR-012**: The Access area MUST let an admin add and rename a group, mark a group inactive, add and rename a role, attach roles to a group, and choose that role's actions from the fixed list: Create tickets, View tickets, Update tickets, Change ticket status, Comment on tickets, Search tickets, Use the assistant, Manage users, and Manage access.
- **FR-013**: A group that still has people MUST NOT be removed. An inactive group MUST grant nothing and MUST NOT be offered for a new assignment.
- **FR-014**: The screen MUST refuse a change that would leave no active person holding Manage access. The message MUST explain that one active admin must remain.
- **FR-015**: The screen MUST show the shared header, including Home and Sign out. It MUST NOT collect or display a password.
- **FR-016**: These starting groups and roles MUST exist: Support with the Support agent role and all seven ticket actions; User managers with the User manager role and Manage users; Administrators with the Admin role and both Manage users and Manage access.
- **FR-017**: Ticket screens MUST show a control only to a member who holds the matching action: Create tickets, View tickets, Update tickets, Change ticket status, Comment on tickets, Search tickets, or Use the assistant. A member without that action MUST NOT see the control.
- **FR-018**: The member MUST be able to search, page, add, edit, and, for an admin, change access from the keyboard. A failed save MUST keep the typed values.

### Key Entities

- **Person**: Someone who can be allowed to sign in. They have a name, email, sign-in id, active state, groups, and whether they can be assigned tickets.
- **Group**: A named set of people. A group has one or more roles. An inactive group grants nothing.
- **Role**: A named set of actions. Support agent, User manager, and Admin are the starting roles.
- **Action**: One allowed activity from the fixed list of nine. A person receives the combined actions of every role on every active group they belong to.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An admin or user manager can find a person and save a name, email, group, or assignment change, and see it on the list, in under 2 minutes.
- **SC-002**: In acceptance testing, 100% of members without Manage users and without Manage access do not see the entry point, and 100% of direct opens show not allowed and change nothing.
- **SC-003**: In acceptance testing, 100% of user-manager attempts to change a role or an action are refused, and 100% of attempts to remove the last active admin are refused.
- **SC-004**: In acceptance testing, a person receives an action only when one of their active groups grants it, in 100% of checked cases.
