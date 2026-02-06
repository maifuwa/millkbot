CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    relation TEXT NOT NULL DEFAULT 'Guest' CHECK(relation IN ('Master', 'Guest', 'Stranger')),
    custom_prompt TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
