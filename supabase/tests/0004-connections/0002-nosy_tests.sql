begin;
select plan(4);

/*Test the behavior of the profile table depending on auth*/
select tests.create_supabase_user('test', metadata => '{"name":"Test"}'::jsonb);
select tests.create_supabase_user('connected', metadata => '{"name":"Connected"}'::jsonb);
select tests.create_supabase_user('nosy_user', metadata => '{"name":"Nosy"}'::jsonb);


/*Nosy user tries to connect 2 random users, should fail*/
select tests.authenticate_as('nosy_user');

select throws_ok(
    'insert into connections (requested_by, requested_for, status)
    VALUES(
        tests.get_supabase_uid(''test''),
        tests.get_supabase_uid(''connected''),
        ''pending''
    )',
    NULL,
    'A user should not be able to mess with the connection of the other users.'
);

select is_empty(
    'SELECT requested_by, requested_for, status FROM public.connections where requested_by = tests.get_supabase_uid(''test'') AND requested_for = tests.get_supabase_uid(''connected'')',
    'Make sure the user did not impact their connections.'
);

/*Nosy user tries to accept/delete for another user*/
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
    'A user should be able to send a connection request where requested_by = their id.'
);

select tests.authenticate_as('nosy_user');

update connections
set status = 'accepted'
where requested_by = tests.get_supabase_uid('test')
AND requested_for = tests.get_supabase_uid('connected');

delete from connections
where requested_by = tests.get_supabase_uid('test')
AND requested_for = tests.get_supabase_uid('connected');

select results_eq(
    'SELECT requested_by, requested_for, status FROM public.connections where requested_by = tests.get_supabase_uid(''test'') AND requested_for = tests.get_supabase_uid(''connected'')',
    $$VALUES (tests.get_supabase_uid('test'), tests.get_supabase_uid('connected'), 'pending')$$,
    'A user should not be able to update othert users'' connections or delete them.'
);

select * from finish();
rollback;