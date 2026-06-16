ALTER TABLE IF EXISTS student_billing_invoices
    DROP CONSTRAINT IF EXISTS uk_student_billing_invoices_academy_student_month;

ALTER TABLE IF EXISTS student_billing_settings
    DROP CONSTRAINT IF EXISTS student_billing_settings_check;

ALTER TABLE IF EXISTS student_billing_settings
    DROP CONSTRAINT IF EXISTS student_billing_settings_due_day_check;

ALTER TABLE IF EXISTS student_billing_settings
    DROP CONSTRAINT IF EXISTS chk_student_billing_settings_due_day;

ALTER TABLE IF EXISTS student_billing_settings
    ADD CONSTRAINT chk_student_billing_settings_due_day
    CHECK (monthly_tuition >= 0 AND due_day BETWEEN 1 AND 31);
