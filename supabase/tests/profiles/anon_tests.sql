begin;
select plan(1);

/*Test the behavior of the profile table depending on auth*/
select tests.create_supabase_user('test', metadata => '{"name":"Test"}'::jsonb);

/*Anon user tries editing and reading, nothing happens, but can read*/
select tests.clear_authentication();

update profiles
set job = 'President',
pfp_url = 'https://www.linkedin.com'
where id = tests.get_supabase_uid('test');

select results_eq(
    'SELECT id, name, job, pfp_url FROM public.profiles where id = tests.get_supabase_uid(''test'')',
    $$VALUES (tests.get_supabase_uid('test'), 'Test', '', NULL)$$
);

select * from finish();
rollback;