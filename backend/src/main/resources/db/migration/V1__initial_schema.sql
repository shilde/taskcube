CREATE TABLE task (
    id          UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
    title       TEXT                NOT NULL,
    description TEXT,
    jira_id      VARCHAR(50),
    spent_time   BIGINT NOT NULL
);

CREATE TABLE face_config (
    face_id INTEGER PRIMARY KEY CHECK (face_id BETWEEN 1 AND 6),
    task_id UUID REFERENCES task (id)
);

INSERT INTO face_config (face_id) VALUES (1), (2), (3), (4), (5), (6);

CREATE FUNCTION prevent_face_config_delete() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Deleting face_config entries is not allowed';
END;
$$;

CREATE TRIGGER no_delete_face_config
    BEFORE DELETE ON face_config
    FOR EACH ROW EXECUTE FUNCTION prevent_face_config_delete();

CREATE TABLE tracking (
    id        UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id   UUID          REFERENCES task (id),
    starttime TIMESTAMPTZ   NOT NULL,
    endtime   TIMESTAMPTZ
);

CREATE INDEX ON tracking (starttime);
CREATE INDEX ON tracking (task_id, starttime);
