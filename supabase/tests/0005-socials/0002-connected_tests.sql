begin;
select plan(3);

select tests.create_supabase_user('test', metadata => '{"name":"Test"}'::jsonb);
select tests.create_supabase_user('connected', metadata => '{"name":"Connected"}'::jsonb);

/*Owner edits linkedin*/
select tests.authenticate_as('test');

update socials
set linkedin_url = 'https://www.linkedin.com'
where id = tests.get_supabase_uid('test');

select results_eq(
    'SELECT id, linkedin_url FROM public.socials where id = tests.get_supabase_uid(''test'')',
    $$VALUES (tests.get_supabase_uid('test'), 'https://www.linkedin.com')$$,
    'Users should be able to edit their socials.'
);

/*Not connected tries to get socials, fails*/
select tests.authenticate_as('connected');
select is_empty(
    'SELECT id, linkedin_url FROM public.socials where id = tests.get_supabase_uid(''test'')',
    'A user that is not connected with another should not be able to see socials.'
);

select tests.authenticate_as('test');
insert into connections (requested_by, requested_for, status)
VALUES(
    tests.get_supabase_uid('test'),
    tests.get_supabase_uid('connected'),
    'pending'
);

select tests.authenticate_as('connected');

update connections
set status = 'accepted'
where requested_by = tests.get_supabase_uid('test')
AND requested_for = tests.get_supabase_uid('connected');

/*Once connected, should succeed*/
select results_eq(
    'SELECT id, linkedin_url FROM public.socials where id = tests.get_supabase_uid(''test'')',
    $$VALUES (tests.get_supabase_uid('test'), 'https://www.linkedin.com')$$,
    'Once the users are connected, they should be able to select socials.'
);

select * from finish();
rollback;