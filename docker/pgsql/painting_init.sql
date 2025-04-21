DO $$
BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_database WHERE datname = 'painting_db'
        ) THEN
            CREATE DATABASE painting_db;
END IF;
END
$$;