DO $$
BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_database WHERE datname = 'keycloak_db'
        ) THEN
            CREATE DATABASE keycloak_db;
END IF;
END
$$;