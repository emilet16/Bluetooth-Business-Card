begin;
select plan(3);

/*Test the behavior of the profile table depending on auth*/
select tests.create_supabase_user('test', metadata => '{"name":"Test"}'::jsonb);
select tests.create_supabase_user('connected', metadata => '{"name":"Connected"}'::jsonb);

/*Request connection*/
select tests.authenticate_as('test');

insert into connections (requested_by, requested_for, status)
VALUES(
    tests.get_supabase_uid('test'),
    tests.get_supabase_uid('connected'),
    'pending'
);

select results_eq(
    'SELECT requested_by, requested_for, status FROM public.connections where requested_by = tests.get_supabase_uid(''test'') AND requested_for = tests.get_supabase_uid(''connected'')',
    $$VALUES (tests.get_supabase_uid('test'), tests.get_supabase_uid('connected'), 'pending')$$,
    'Users should be able to send connections requests.'
);

/*Try accepting the connection instead of the other user*/
update connections
set status = 'accepted'
where requested_by = tests.get_supabase_uid('test')
AND requested_for = tests.get_supabase_uid('connected');

select results_eq(
    'SELECT requested_by, requested_for, status FROM public.connections where requested_by = tests.get_supabase_uid(''test'') AND requested_for = tests.get_supabase_uid(''connected'')',
    $$VALUES (tests.get_supabase_uid('test'), tests.get_supabase_uid('connected'), 'pending')$$,
    'Users should not be able to accept connections for other users.'
);

/*Accept connection*/
select tests.authenticate_as('connected');

update connections
set status = 'accepted'
where requested_by = tests.get_supabase_uid('test')
AND requested_for = tests.get_supabase_uid('connected');

select results_eq(
    'SELECT requested_by, requested_for, status FROM public.connections where requested_by = tests.get_supabase_uid(''test'') AND requested_for = tests.get_supabase_uid(''connected'')',
    $$VALUES (tests.get_supabase_uid('test'), tests.get_supabase_uid('connected'), 'accepted')$$,
    'Users should be able to accept connections sent to them.'
);

select * from finish();
rollback;