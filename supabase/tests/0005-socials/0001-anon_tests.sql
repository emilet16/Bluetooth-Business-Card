begin;
select plan(2);

select tests.create_supabase_user('test', metadata => '{"name":"Test"}'::jsonb);

/*Anon user tries to update test linkedin, should fail*/
select tests.clear_authentication();

update socials
set linkedin_url = 'https://www.linkedin.com'
where id = tests.get_supabase_uid('test');

select tests.authenticate_as('test');

select results_eq(
    'SELECT id, linkedin_url FROM public.socials where id = tests.get_supabase_uid(''test'')',
    $$VALUES (tests.get_supabase_uid('test'), NULL)$$,
    'Anon user should not be able to edit socials.'
);

/*Owner edits linkedin*/
select tests.authenticate_as('test');

update socials
set linkedin_url = 'https://www.linkedin.com'
where id = tests.get_supabase_uid('test');

/*Anon user can't read table*/
select tests.clear_authentication();

select is_empty(
    'SELECT id, linkedin_url FROM public.socials where id = tests.get_supabase_uid(''test'')',
    'Anon users should not be able to select social rows.'
);

select * from finish();
rollback;