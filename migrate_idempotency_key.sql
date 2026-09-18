-- Step 1: Add column with empty string default so existing rows don't fail NOT NULL
ALTER TABLE processing_jobs ADD COLUMN IF NOT EXISTS idempotency_key varchar(255) DEFAULT '' NOT NULL;

-- Step 2: Fill existing rows with unique UUIDs (since it must be unique)
UPDATE processing_jobs SET idempotency_key = gen_random_uuid()::text WHERE idempotency_key = '';

-- Step 3: Drop the default so future inserts must supply a value explicitly
ALTER TABLE processing_jobs ALTER COLUMN idempotency_key DROP DEFAULT;

-- Step 4: Add the unique constraint (only if it doesn't already exist)
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'uk4twrtnalil5g0ppvpt6m73e34'
  ) THEN
    ALTER TABLE processing_jobs ADD CONSTRAINT UK4twrtnalil5g0ppvpt6m73e34 UNIQUE (idempotency_key);
  END IF;
END $$;

-- Verify
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'processing_jobs' AND column_name = 'idempotency_key';
