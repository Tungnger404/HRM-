-- Fix training_recommendations table to allow NULL values
-- This allows managers to recommend training without specifying a specific program or evaluation
-- The program can be assigned later by HR or the manager

-- Step 1: Drop the foreign key constraint on eval_id (if exists)
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'fk_rec_eval')
BEGIN
    ALTER TABLE training_recommendations DROP CONSTRAINT fk_rec_eval;
    PRINT 'Dropped constraint fk_rec_eval';
END

-- Step 2: Allow NULL for program_id
ALTER TABLE training_recommendations
ALTER COLUMN program_id INT NULL;

-- Step 3: Allow NULL for eval_id (if not already)
ALTER TABLE training_recommendations
ALTER COLUMN eval_id INT NULL;

-- Verify the changes
SELECT 
    COLUMN_NAME,
    IS_NULLABLE,
    DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'training_recommendations'
  AND COLUMN_NAME IN ('program_id', 'eval_id')
ORDER BY COLUMN_NAME;
