ALTER TABLE in_app_notifications DROP CONSTRAINT ck_notification_kind;
ALTER TABLE in_app_notifications DROP CONSTRAINT ck_notification_amount;
ALTER TABLE in_app_notifications DROP CONSTRAINT ck_notification_currency;
ALTER TABLE in_app_notifications DROP CONSTRAINT ck_notification_occurrence;
ALTER TABLE in_app_notifications DROP CONSTRAINT ck_notification_attention;

ALTER TABLE in_app_notifications ALTER COLUMN plan_id DROP NOT NULL;
ALTER TABLE in_app_notifications ALTER COLUMN amount DROP NOT NULL;
ALTER TABLE in_app_notifications ALTER COLUMN currency DROP NOT NULL;
ALTER TABLE in_app_notifications ALTER COLUMN occurrence_number DROP NOT NULL;
ALTER TABLE in_app_notifications ADD COLUMN calendar_entry_id UUID;
ALTER TABLE in_app_notifications ADD COLUMN completed_by_member_id UUID;

ALTER TABLE in_app_notifications ADD CONSTRAINT ck_notification_kind CHECK(kind IN (
  'EXPENSE_PLAN_OCCURRENCE_APPROACHING','EXPENSE_PLAN_LAST_INSTALLMENT_APPROACHING',
  'EXPENSE_PLAN_ATTENTION_REQUIRED','CALENDAR_TASK_COMPLETED'));
ALTER TABLE in_app_notifications ADD CONSTRAINT ck_notification_source CHECK(
  (kind = 'CALENDAR_TASK_COMPLETED' AND calendar_entry_id IS NOT NULL AND plan_id IS NULL)
  OR (kind <> 'CALENDAR_TASK_COMPLETED' AND plan_id IS NOT NULL AND calendar_entry_id IS NULL));
ALTER TABLE in_app_notifications ADD CONSTRAINT ck_notification_payload CHECK(
  (kind = 'CALENDAR_TASK_COMPLETED' AND amount IS NULL AND currency IS NULL
    AND occurrence_number IS NULL AND total_occurrences IS NULL AND completed_by_member_id IS NOT NULL)
  OR (kind <> 'CALENDAR_TASK_COMPLETED' AND amount > 0 AND currency IS NOT NULL
    AND LENGTH(currency) = 3 AND currency = UPPER(currency) AND occurrence_number > 0
    AND (total_occurrences IS NULL OR total_occurrences >= occurrence_number)
    AND completed_by_member_id IS NULL));
ALTER TABLE in_app_notifications ADD CONSTRAINT ck_notification_attention CHECK(
  (kind = 'EXPENSE_PLAN_ATTENTION_REQUIRED' AND attention_reason IS NOT NULL)
  OR (kind <> 'EXPENSE_PLAN_ATTENTION_REQUIRED' AND attention_reason IS NULL));
