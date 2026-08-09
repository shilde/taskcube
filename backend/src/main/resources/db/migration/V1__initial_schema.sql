CREATE TABLE task (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    title       TEXT        NOT NULL,
    description TEXT,
    jiraId     VARCHAR(50)
);

CREATE TABLE face_config (
    faceId INTEGER PRIMARY KEY CHECK (faceId BETWEEN 1 AND 6),
    taskId UUID REFERENCES task (id)
);

INSERT INTO face_config (faceId) VALUES (1), (2), (3), (4), (5), (6);

CREATE FUNCTION prevent_face_config_delete() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Deleting face_config entries is not allowed';
END;
$$;

CREATE TRIGGER no_delete_face_config
    BEFORE DELETE ON face_config
    FOR EACH ROW EXECUTE FUNCTION prevent_face_config_delete();
