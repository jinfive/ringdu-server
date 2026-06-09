ALTER TABLE IF EXISTS student_billing_invoices
    DROP CONSTRAINT IF EXISTS uk_student_billing_invoices_academy_student_month;

DO $$
DECLARE
    due_day_constraint text;
BEGIN
    FOR due_day_constraint IN
        SELECT constraint_info.conname
        FROM pg_constraint constraint_info
        JOIN pg_class table_info ON table_info.oid = constraint_info.conrelid
        WHERE table_info.relname = 'student_billing_settings'
          AND constraint_info.contype = 'c'
          AND pg_get_constraintdef(constraint_info.oid) ILIKE '%due_day%'
    LOOP
        EXECUTE format(
            'ALTER TABLE student_billing_settings DROP CONSTRAINT %I',
            due_day_constraint
        );
    END LOOP;

    IF to_regclass('student_billing_settings') IS NOT NULL THEN
        ALTER TABLE student_billing_settings
            ADD CONSTRAINT student_billing_settings_due_day_check
            CHECK (monthly_tuition >= 0 AND due_day BETWEEN 1 AND 31);
    END IF;
END $$;
