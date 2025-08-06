begin;
select plan(2);

select tests.create_supabase_user('test', metadata => '{"name":"Test"}'::jsonb);
select tests.create_supabase_user('connected', metadata => '{"name":"Connected"}'::jsonb);
select tests.create_supabase_user('nosy_user', metadata => '{"name":"Nosy"}'::jsonb);

/*Nosy user tries to update test linkedin, should fail*/
select tests.authenticate_as('nosy_user');

update socials
set linkedin_url = 'https://www.linkedin.com'
where id = tests.get_supabase_uid('test');

select tests.authenticate_as('test');

select results_eq(
    'SELECT id, linkedin_url FROM public.socials where id = tests.get_supabase_uid(''test'')',
    $$VALUES (tests.get_supabase_uid('test'), NULL)$$,
    'Users should not be able to edit other users'' socials.'
);

/*Owner edits linkedin and sends connection request to connected*/
select tests.authenticate_as('test');

update socials
set linkedin_url = 'https://www.linkedin.com'
where id = tests.get_supabase_uid('test');

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

select tests.authenticate_as('nosy_user');

/*Nosy user tries to read socials of 2 connected users, fails*/
select is_empty(
    'SELECT id, linkedin_url FROM public.socials where id = tests.get_supabase_uid(''test'')',
    'Non connected users should not be able to select socials.'
);

select * from finish();
rollback;