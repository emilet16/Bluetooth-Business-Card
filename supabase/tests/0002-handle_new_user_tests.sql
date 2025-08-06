/*Ensure a new user is handled properly*/

begin;
select plan(3);

select has_function('handle_new_user', 'There should be a function to handle new users.');
select has_trigger('auth', 'users', 'on_auth_user_created', 'create profile when user is created');

select tests.create_supabase_user('test', metadata => '{"name":"Test"}'::jsonb);

select results_eq(
    'SELECT id, name FROM public.profiles',
    $$VALUES (tests.get_supabase_uid('test'), 'Test')$$,
    'Function should create an entry into table profiles with the id and name of the user.'
);

select * from finish();
rollback;