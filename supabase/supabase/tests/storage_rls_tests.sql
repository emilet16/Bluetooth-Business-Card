begin;
select plan(1);

select policies_are(
    'storage',
    'objects',
    ARRAY [
        'Upload a new pfp only when auth 2dkq_0',
        'Users can only delete their own pfps 2dkq_0',
        'Users can only delete their own pfps 2dkq_1'
    ]
);

select * from finish();
rollback;