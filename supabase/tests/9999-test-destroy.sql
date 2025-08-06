DROP SCHEMA tests CASCADE;
DROP SCHEMA test_overrides CASCADE;

begin;
select plan(1);

select pass('Clear test functions.');

select * from finish();
rollback;