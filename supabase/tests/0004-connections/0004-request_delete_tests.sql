begin;
select plan(2);

/*Test the behavior of the profile table depending on auth*/
select tests.create_supabase_user('test', metadata => '{"name":"Test"}'::jsonb);
select tests.create_supabase_user('deleted', metadata => '{"name":"Deleted"}'::jsonb);

/*Delete connection*/
select tests.authenticate_as('test');

insert into connections (requested_by, requested_for, status)
VALUES(
    tests.get_supabase_uid('test'),
    tests.get_supabase_uid('deleted'),
    'pending'
);

select results_eq(
    'SELECT requested_by, requested_for, status FROM public.connections where requested_by = tests.get_supabase_uid(''test'') AND requested_for = tests.get_supabase_uid(''deleted'')',
    $$VALUES (tests.get_supabase_uid('test'), tests.get_supabase_uid('deleted'), 'pending')$$,
    'Users should be able to send connection requests.'
);

select tests.authenticate_as('deleted');

delete from connections
where requested_by = tests.get_supabase_uid('test')
AND requested_for = tests.get_supabase_uid('deleted');

select is_empty(
    'SELECT requested_by, requested_for, status FROM public.connections where requested_by = tests.get_supabase_uid(''test'') AND requested_for = tests.get_supabase_uid(''deleted'')',
    'Ensure the connection request was deleted properly.'
);

select * from finish();
rollback;