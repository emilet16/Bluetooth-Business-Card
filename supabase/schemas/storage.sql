/*RLS policies to secure write access to the pfp storage bucket*/

create policy "Upload a new pfp only when auth 2dkq_0"
on "storage"."objects"
as permissive
for insert
to authenticated
with check ((bucket_id = 'pfp'::text));