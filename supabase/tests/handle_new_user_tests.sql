/*Ensure a new user is handled properly*/

begin;
CREATE EXTENSION "basejump-supabase_test_helpers";
select plan(3);

select has_function('handle_new_user');
select has_trigger('auth', 'users', 'on_auth_user_created', 'create profile when user is created');

select tests.create_supabase_user('test', metadata => '{"name":"Test"}'::jsonb);

select results_eq(
    'SELECT id, name FROM public.profiles',
    $$VALUES (tests.get_supabase_uid('test'), 'Test')$$
);

select * from finish();
rollback;