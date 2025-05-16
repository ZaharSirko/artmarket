DO $$
BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_database WHERE datname = 'order_db'
        ) THEN
            CREATE DATABASE order_db;
END IF;
END
$$;