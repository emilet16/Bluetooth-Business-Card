alter table "public"."connections" enable row level security;

alter table "public"."profiles" enable row level security;

alter table "public"."socials" enable row level security;

CREATE UNIQUE INDEX connections_pkey ON public.connections USING btree (requested_by, requested_for);

CREATE UNIQUE INDEX profiles_pkey ON public.profiles USING btree (id);

CREATE UNIQUE INDEX socials_pkey ON public.socials USING btree (id);

alter table "public"."connections" add constraint "connections_pkey" PRIMARY KEY using index "connections_pkey";

alter table "public"."profiles" add constraint "profiles_pkey" PRIMARY KEY using index "profiles_pkey";

alter table "public"."socials" add constraint "socials_pkey" PRIMARY KEY using index "socials_pkey";

alter table "public"."connections" add constraint "connections_requested_by_fkey" FOREIGN KEY (requested_by) REFERENCES profiles(id) ON UPDATE CASCADE ON DELETE CASCADE not valid;

alter table "public"."connections" validate constraint "connections_requested_by_fkey";

alter table "public"."connections" add constraint "connections_requested_for_fkey" FOREIGN KEY (requested_for) REFERENCES profiles(id) ON UPDATE CASCADE ON DELETE CASCADE not valid;

alter table "public"."connections" validate constraint "connections_requested_for_fkey";

alter table "public"."profiles" add constraint "profiles_id_fkey" FOREIGN KEY (id) REFERENCES auth.users(id) ON UPDATE CASCADE ON DELETE CASCADE not valid;

alter table "public"."profiles" validate constraint "profiles_id_fkey";

alter table "public"."socials" add constraint "socials_id_fkey" FOREIGN KEY (id) REFERENCES profiles(id) ON UPDATE CASCADE ON DELETE CASCADE not valid;

alter table "public"."socials" validate constraint "socials_id_fkey";

set check_function_bodies = off;

CREATE OR REPLACE FUNCTION public.handle_new_user()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
BEGIN
  INSERT INTO public.profiles (id, name)
  VALUES (new.id, new.raw_user_meta_data ->> 'name');

  INSERT INTO public.socials (id, linkedin_url)
  VALUES (new.id, NULL);

  RETURN new;
END;
$function$
;

create policy "Create requested_by, status pending"
on "public"."connections"
as permissive
for insert
to public
with check (((( SELECT auth.uid() AS uid) = requested_by) AND (status = 'pending'::text)));


create policy "Delete connection requests"
on "public"."connections"
as permissive
for delete
to public
using (((( SELECT auth.uid() AS uid) = requested_for) AND (status = 'pending'::text)));


create policy "Public read access"
on "public"."connections"
as permissive
for select
to public
using (true);


create policy "Update requested_for, status 'accepted' 'declined'"
on "public"."connections"
as permissive
for update
to public
using (((( SELECT auth.uid() AS uid) = requested_for) AND (status = 'pending'::text)))
with check (((( SELECT auth.uid() AS uid) = requested_for) AND (status = 'accepted'::text)));


create policy "Enable insert for users based on user_id"
on "public"."profiles"
as permissive
for insert
to authenticated
with check ((( SELECT auth.uid() AS uid) = id));


create policy "Enable read access for auth users"
on "public"."profiles"
as permissive
for select
to public
using (true);


create policy "Enable update for users based on user_id"
on "public"."profiles"
as permissive
for update
to authenticated
using ((( SELECT auth.uid() AS uid) = id))
with check ((( SELECT auth.uid() AS uid) = id));


create policy "Users can create their own rows"
on "public"."socials"
as permissive
for insert
to authenticated
with check ((( SELECT auth.uid() AS uid) = id));


create policy "Users can select their row and connections"
on "public"."socials"
as permissive
for select
to authenticated
using (((( SELECT auth.uid() AS uid) = id) OR (EXISTS ( SELECT 1
   FROM connections
  WHERE (((connections.requested_by = auth.uid()) AND (connections.requested_for = socials.id)) OR ((connections.requested_for = auth.uid()) AND (connections.requested_by = socials.id)))))));


create policy "Users can update their own rows"
on "public"."socials"
as permissive
for update
to authenticated
using ((( SELECT auth.uid() AS uid) = id))
with check ((( SELECT auth.uid() AS uid) = id));



