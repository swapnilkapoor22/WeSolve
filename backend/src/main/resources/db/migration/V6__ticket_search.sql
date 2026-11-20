CREATE TABLE ticket_search (
    ticket_id uuid PRIMARY KEY REFERENCES ticket (id),
    document tsvector NOT NULL
);

CREATE INDEX ticket_search_document_idx ON ticket_search USING gin (document);

CREATE FUNCTION rebuild_ticket_search(tid uuid) RETURNS void
LANGUAGE sql AS $$
    INSERT INTO ticket_search (ticket_id, document)
    SELECT t.id,
           to_tsvector(
               'simple',
               t.title || ' ' || t.description || ' ' || coalesce(string_agg(h.comment, ' '), '')
           )
    FROM ticket t
    LEFT JOIN ticket_history h ON h.ticket_id = t.id AND h.comment IS NOT NULL
    WHERE t.id = tid
    GROUP BY t.id, t.title, t.description
    ON CONFLICT (ticket_id) DO UPDATE SET document = EXCLUDED.document;
$$;

CREATE FUNCTION ticket_search_from_ticket() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        DELETE FROM ticket_search WHERE ticket_id = OLD.id;
        RETURN OLD;
    END IF;
    PERFORM rebuild_ticket_search(NEW.id);
    RETURN NEW;
END;
$$;

CREATE FUNCTION ticket_search_from_history() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM rebuild_ticket_search(NEW.ticket_id);
    RETURN NEW;
END;
$$;

CREATE TRIGGER ticket_search_ticket
    AFTER INSERT OR UPDATE OF title, description OR DELETE ON ticket
    FOR EACH ROW EXECUTE FUNCTION ticket_search_from_ticket();

CREATE TRIGGER ticket_search_history
    AFTER INSERT ON ticket_history
    FOR EACH ROW EXECUTE FUNCTION ticket_search_from_history();
