/*Make sure all the components of the database are present*/

begin;
select plan(25);

select has_table('profiles', 'Table profiles should exist.');
select has_table('socials', 'Table socials should exist.');
select has_table('connections', 'Table connections should exist.');

select has_column('profiles', 'id', 'Table profiles should have a column named id.');
select col_is_pk('profiles', 'id', 'Column id of table profiles should be the primary key.');
select fk_ok('public', 'profiles', 'id', 'auth', 'users', 'id', 'Profile id must reference user id');
select has_column('profiles', 'name', 'Table profiles should have a column named name.');
select has_column('profiles', 'job', 'Table profiles should have a column named job.');
select has_column('profiles', 'pfp_url', 'Table profiles should have a column named pfp_url.');

select has_column('connections', 'requested_by', 'Table connections should have a column named requested_by.');
select has_column('connections', 'requested_for', 'Table connections should have a column named requested_for.');
select col_is_pk('connections', ARRAY['requested_by', 'requested_for'], 'The primary key for table connections should be the pair of requested_by and requested_for.');
select fk_ok('connections', 'requested_by', 'profiles', 'id', 'Requested_by must reference user id');
select fk_ok('connections', 'requested_for', 'profiles', 'id', 'Requested_for must reference user id');
select has_column('connections', 'status', 'Table connections should have a column named status.');

select has_column('socials', 'id', 'Table socials should have a column named id.');
select col_is_pk('socials', 'id', 'Column id of table socials should be a primary key.');
select fk_ok('socials', 'id', 'profiles', 'id', 'Socials id must reference user id');
select has_column('socials', 'linkedin_url', 'Table socials should have a column named linkedin_url.');

select tests.rls_enabled('public');

select policies_are(
    'public',
    'profiles',
    ARRAY [
        'Enable insert for users based on user_id',
        'Enable read access for auth users',
        'Enable update for users based on user_id'
    ],
    'Table profiles should have policies for select, insert and update.'
);

select policies_are(
    'public',
    'connections',
    ARRAY [
        'Create requested_by, status pending',
        'Delete connection requests',
        'Public read access',
        'Update requested_for, status ''accepted'' ''declined'''
    ],
    'Table connections should have policies for select, insert, update and delete.'
);

select policies_are(
    'public',
    'socials',
    ARRAY [
        'Users can create their own rows',
        'Users can select their row and connections',
        'Users can update their own rows'
    ],
    'Table socials should have policies for select, insert and update.'
);

select tests.rls_enabled('storage');

select policies_are(
    'storage',
    'objects',
    ARRAY [
        'Upload a new pfp only when auth 2dkq_0'
    ],
    'Storage objects should have a policy for insert.'
);

select * from finish();
rollback;