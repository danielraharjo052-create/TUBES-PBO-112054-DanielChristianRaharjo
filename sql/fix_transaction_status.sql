-- Jalankan sekali pada database ConcertHub yang sudah ada.
-- Menyamakan constraint dengan alur PENDING -> DIBAYAR -> SUKSES / CANCELLED.
DO $$
DECLARE c RECORD;
BEGIN
    FOR c IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'transactions'::regclass
          AND contype = 'c'
          AND pg_get_constraintdef(oid) ILIKE '%status%'
    LOOP
        EXECUTE format('ALTER TABLE transactions DROP CONSTRAINT %I', c.conname);
    END LOOP;
END $$;

ALTER TABLE transactions
    ADD CONSTRAINT transactions_status_check
    CHECK (status IN ('PENDING', 'DIBAYAR', 'SUKSES', 'CANCELLED'));

CREATE INDEX IF NOT EXISTS idx_transactions_pending_created
    ON transactions (created_at) WHERE status = 'PENDING';
