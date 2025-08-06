begin;
select plan(1);

/*Test the behavior of the profile table depending on auth*/
select tests.create_supabase_user('test', metadata => '{"name":"Test"}'::jsonb);

/*Owner edits job*/
select tests.authenticate_as('test');

update profiles
set job = 'CEO',
pfp_url = 'https://www.google.com'
where id = tests.get_supabase_uid('test');

select results_eq(
    'SELECT id, name, job, pfp_url FROM public.profiles where id = tests.get_supabase_uid(''test'')',
    $$VALUES (tests.get_supabase_uid('test'), 'Test', 'CEO', 'https://www.google.com')$$
);

select * from finish();
rollback;