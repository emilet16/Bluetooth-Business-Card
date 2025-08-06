/*RLS policies to secure write access to the pfp storage bucket*/

create policy "Upload a new pfp only when auth 2dkq_0"
on "storage"."objects"
as permissive
for insert
to authenticated
with check ((bucket_id = 'pfp'::text));

create policy "Users can only delete their own pfps 2dkq_0"
on "storage"."objects"
as permissive
for delete
to authenticated
using (((bucket_id = 'pfp'::text) AND (owner_id = (auth.uid())::text)));

create policy "Users can only delete their own pfps 2dkq_1"
on "storage"."objects"
as permissive
for select
to authenticated
using (((bucket_id = 'pfp'::text) AND (owner_id = (auth.uid())::text)));