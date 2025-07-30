begin;
select plan(19);

select has_table('profiles');
select has_table('socials');
select has_table('connections');

select has_column('profiles', 'id');
select col_is_pk('profiles', 'id');
select fk_ok('public', 'profiles', 'id', 'auth', 'users', 'id', 'Profile id must reference user id');
select has_column('profiles', 'name');
select has_column('profiles', 'job');
select has_column('profiles', 'pfp_url');

select has_column('connections', 'requested_by');
select has_column('connections', 'requested_for');
select col_is_pk('connections', ARRAY['requested_by', 'requested_for']);
select fk_ok('connections', 'requested_by', 'profiles', 'id', 'Requested_by must reference user id');
select fk_ok('connections', 'requested_for', 'profiles', 'id', 'Requested_for must reference user id');
select has_column('connections', 'status');

select has_column('socials', 'id');
select col_is_pk('socials', 'id');
select fk_ok('socials', 'id', 'profiles', 'id', 'Socials id must reference user id');
select has_column('socials', 'linkedin_url');

select * from finish();
rollback;