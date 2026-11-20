# Feature Specification: Search and Filter Tickets

**Feature Branch**: `006-search-filter-tickets`

**Created**: 2026-11-18

**Status**: Draft

**Input**: User description: "Search tickets using keywords across titles, descriptions, and comments. Filter tickets by status. Support combining search and filters."

## Clarifications

### Session 2026-11-20

- Q: Should unspecified choices remain in an assumptions list? → A: No. Each best guess is a requirement.
- Q: What screen behavior must be specified? → A: Fields, choices, messages, and empty, loading, and error states.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Search and filter the queue (Priority: P3)

On the queue screen, a signed-in support team member types keywords, chooses one status or All, and searches. The same list area shows the matching tickets. The member can open a result.

**Why this priority**: A growing queue cannot be managed by scrolling alone.

**Independent Test**: Create tickets that differ by status and by whether the keyword sits in the title, description, or a comment, then use the keyword box, the status choice, and both together.

**Acceptance Scenarios**:

1. **Given** the queue screen, **When** a member types a keyword and searches, **Then** the list shows tickets whose title, description, or comments contain that keyword, and hides tickets that do not.
2. **Given** tickets in several statuses, **When** a member chooses one status and searches with an empty keyword box, **Then** the list shows only tickets in that status.
3. **Given** a keyword and a status, **When** a member applies both, **Then** a ticket is shown only when it matches the keyword and the status.
4. **Given** no ticket matches, **When** the member searches, **Then** the list area says that nothing matched. The queue controls stay usable. This is not an error.
5. **Given** the keyword box is empty and the status choice is All, **When** a member searches, **Then** the list shows the full queue.
6. **Given** more than 25 tickets match, **When** the results are shown, **Then** they are ordered newest first, 25 per page, and the member can move to the next page.
7. **Given** a result row, **When** the member chooses it, **Then** that ticket's detail screen opens.

### Edge Cases

- A keyword that appears only in a comment still finds the parent ticket. A status choice still excludes that ticket when the status does not match.
- Search is not case-sensitive. "Login" and "login" return the same tickets.
- A multi-word search matches a ticket only when every word appears somewhere in its title, description, or comments. The words do not need to be next to each other or in the same field.
- A search or filter with no matches shows "nothing matched" in the list area, not an error.
- Results stay newest-created first and use pages of 25 when there are more than 25 matches.
- The status choice offers All, Open, In Progress, Resolved, Closed, and Cancelled. All means no status filter. Closed and Cancelled can be selected.
- The screen has no saved-search control and no assignee or priority filter.
- While results are loading, the list shows that it is loading and does not say that nothing matched. If the search fails, the member sees an error and can try again.
- A customer cannot use the search controls.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A signed-in member who holds Search tickets MUST be able to search every ticket from the queue screen. A customer, and a member without Search tickets, MUST NOT see the search controls.
- **FR-002**: The queue screen MUST show a keyword box, a status choice, and a search control. The status choice MUST offer only All, Open, In Progress, Resolved, Closed, and Cancelled.
- **FR-003**: A keyword search MUST look at titles, descriptions, and comments. It MUST be case-insensitive. When several words are entered, a ticket MUST match only if every word appears somewhere in those texts.
- **FR-004**: The member MUST be able to use the keyword box and the status choice together. A ticket MUST be shown only when it matches both. All with an empty keyword box MUST show the full queue.
- **FR-005**: A match that exists only in a comment MUST still return the parent ticket. The comment itself MUST NOT appear as its own row.
- **FR-006**: Results MUST use the same rows as the queue: ticket ID, title, priority, status, assignee name, and created time. They MUST be ordered newest-created first and shown 25 per page, with a control for the next and previous page.
- **FR-007**: When nothing matches, the list area MUST say that nothing matched and MUST NOT show an error. The keyword box and status choice MUST stay usable.
- **FR-008**: Choosing a result row MUST open that ticket's detail screen.
- **FR-009**: The screen MUST NOT offer saved searches, an assignee filter, a priority filter, or fuzzy matching.
- **FR-010**: While results are loading, the list MUST show that it is loading. A failed search MUST show an error and a way to try again.
- **FR-011**: The member MUST be able to type the keyword, change the status choice, run the search, page the results, and open a row from the keyboard.
- **FR-012**: The screen MUST show the shared header, including Home. Home MUST open the member's ticket home.

### Key Entities

- **Support team member**: A signed-in person on the support team who searches and filters the queue.
- **Ticket**: A saved support issue whose title, description, status, and comments can be matched.
- **Comment**: Text stored on a ticket. It is included in keyword matching and does not appear as its own result.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In a set of moderated finding tasks, members locate a known ticket from the queue search on the first attempt at least 95% of the time, including when the only match is in a comment.
- **SC-002**: For a queue of up to 10,000 tickets, search and filtered results are visible on the queue screen within 2 seconds in 95% of attempts.
- **SC-003**: In acceptance testing, 100% of combined search-and-filter checks show only tickets that match both the words and the status.
