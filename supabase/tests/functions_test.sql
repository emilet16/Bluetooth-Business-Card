begin;
select plan(2);

select has_function('handle_new_user');
select has_trigger('auth', 'users', 'on_auth_user_created', 'create profile when user is created');

select * from finish();
rollback;