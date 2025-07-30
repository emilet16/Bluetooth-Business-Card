begin;
select plan(3);

select policies_are(
    'public',
    'profiles',
    ARRAY [
        'Enable insert for users based on user_id',
        'Enable read access for auth users',
        'Enable update for users based on user_id'
    ]
);

select policies_are(
    'public',
    'connections',
    ARRAY [
        'Create requested_by, status pending',
        'Delete connection requests',
        'Public read access',
        'Update requested_for, status ''accepted'' ''declined'''
    ]
);

select policies_are(
    'public',
    'socials',
    ARRAY [
        'Users can create their own rows',
        'Users can select their row and connections',
        'Users can update their own rows'
    ]
);

select * from finish();
rollback;