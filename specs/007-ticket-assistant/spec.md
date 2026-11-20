# Feature Specification: Ticket Assistant

**Feature Branch**: `007-ticket-assistant`

**Created**: 2026-11-18

**Status**: Draft

**Input**: User description: "An AI assistant answers questions using historical ticket data. Every AI answer must be based on actual tickets and include references to the tickets used."

## Clarifications

### Session 2026-11-20

- Q: Should unspecified choices remain in an assumptions list? → A: No. Each best guess is a requirement.
- Q: What screen behavior must be specified? → A: Fields, choices, messages, and empty, loading, and error states.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ask the assistant about past tickets (Priority: P4)

A signed-in support team member types a question and asks. The answer area shows either a cited answer, a statement that the tickets differ, or a statement that no ticket supports an answer. Each cited ticket ID and title can be opened.

**Why this priority**: The assistant is valuable only after real tickets and comments exist. It must not become a source of answers the team cannot trace.

**Independent Test**: Ask a question whose answer is contained in known tickets, open a cited ticket, then ask a question that no ticket supports and confirm the answer area declines instead of inventing an answer.

**Acceptance Scenarios**:

1. **Given** saved tickets and comments contain the facts needed, **When** a member types that question and asks, **Then** the answer area uses only those facts and lists each ticket it used, with the ticket ID and title.
2. **Given** an answer lists a ticket, **When** the member activates that reference, **Then** the ticket's detail screen opens, and the ticket contains the information the answer relied on.
3. **Given** no saved ticket supports the question, **When** a member asks it, **Then** the answer area says that no supporting tickets were found, shows no factual answer, and shows no references.
4. **Given** a comment holds a fact that is not in the ticket title or description, **When** a member asks about that fact, **Then** the answer can use the comment and the reference names the parent ticket.
5. **Given** two saved tickets disagree about the same fact, **When** a member asks which is true, **Then** the answer area says the tickets differ, lists both references, and does not pick one as fact.
6. **Given** the member has asked and the answer is not back yet, **When** they look at the answer area, **Then** it shows that the question is being answered and does not show an empty answer as a finished result.

### Edge Cases

- A question outside the ticket history shows "no supporting tickets" and no invented facts.
- The assistant cites only tickets it actually used. A reference always belongs to a real ticket whose title, description, or comments contain the supporting information.
- When cited tickets disagree, the answer says the tickets differ and lists them.
- An answer that states information without at least one real ticket reference is not shown.
- Comments are part of the material the assistant may use. Tickets in every status, including Closed and Cancelled, may be cited.
- The answer area has no control to create, edit, close, or comment on a ticket.
- If the question cannot be answered because the request fails, the answer area shows an error and the member can ask again. The typed question remains.
- A blank question is not sent. The message sits beside the question box.
- A customer cannot open the assistant screen.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A signed-in member who holds Use the assistant MUST be able to open an assistant screen with a question box and an Ask control. A customer, and a member without Use the assistant, MUST NOT see that screen.
- **FR-002**: The assistant MUST base answers only on saved ticket titles, descriptions, and comments. It MUST NOT present facts that are not in those tickets, and it MUST NOT use any other source.
- **FR-003**: Every answer that states information MUST list the tickets it used in the answer area. Each reference MUST show the ticket ID and title of a real ticket that contains the supporting information, and MUST open that ticket's detail screen when activated.
- **FR-004**: When no saved ticket supports an answer, the answer area MUST say that no supporting tickets were found, MUST NOT show a factual answer, and MUST NOT show references.
- **FR-005**: When the tickets it uses disagree, the answer area MUST say that the tickets differ, MUST list those tickets, and MUST NOT choose one version as fact.
- **FR-006**: The assistant MUST NOT cite a ticket it did not use, and MUST NOT show an ID that does not belong to a real ticket.
- **FR-007**: Comments MUST be usable as supporting material, and the reference MUST name the parent ticket. Tickets in every status MAY be used.
- **FR-008**: The assistant screen MUST NOT provide a control to create, update, close, or comment on a ticket.
- **FR-009**: A blank question MUST NOT be sent, and the message MUST sit beside the question box. While an answer is pending, the answer area MUST show that it is working. A failed request MUST show an error, keep the question text, and let the member ask again.
- **FR-010**: The member MUST be able to type a question, ask it, and open a reference from the keyboard.
- **FR-011**: The screen MUST show the shared header, including Home. Home MUST open the member's ticket home.

### Key Entities

- **Support team member**: A signed-in person on the support team who asks a question and opens cited tickets.
- **Ticket**: A saved support issue. Its title, description, and comments are the only material the assistant may use.
- **Comment**: A note stored on a ticket. It counts as part of that ticket's history.
- **Assistant answer**: The text in the answer area. It either states information together with the tickets used, reports that the cited tickets disagree, or states that no supporting tickets were found.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In a reviewed sample, 100% of assistant answers that state information list one or more real tickets containing the supporting information.
- **SC-002**: In that same sample, 100% of questions with no supporting ticket show "no supporting tickets" in the answer area, with no invented facts and no references.
- **SC-003**: A member can open every cited ticket from the answer area, and in acceptance testing 100% of those tickets exist and contain the cited support.
- **SC-004**: A member sees an answer, or a clear "no supporting tickets" outcome, within 10 seconds for 95% of questions asked against a history of up to 10,000 tickets.
