CREATE TABLE IF NOT EXISTS "public"."profiles" (
    "id" "uuid" DEFAULT "auth"."uid"() NOT NULL PRIMARY KEY REFERENCES "auth"."users"("id") ON UPDATE CASCADE ON DELETE CASCADE,
    "created_at" timestamp with time zone DEFAULT "now"() NOT NULL,
    "name" "text" DEFAULT ''::"text" NOT NULL,
    "job" "text" DEFAULT ''::"text" NOT NULL,
    "pfp_url" "text"
);

CREATE TABLE IF NOT EXISTS "public"."connections" (
    "requested_by" "uuid" NOT NULL REFERENCES "public"."profiles"("id") ON UPDATE CASCADE ON DELETE CASCADE,
    "requested_for" "uuid" NOT NULL REFERENCES "public"."profiles"("id") ON UPDATE CASCADE ON DELETE CASCADE,
    "created_at" timestamp with time zone DEFAULT "now"(),
    "updated_at" timestamp with time zone DEFAULT "now"(),
    "status" "text",
    PRIMARY KEY ("requested_by", "requested_for")
);

CREATE TABLE IF NOT EXISTS "public"."socials" (
    "id" "uuid" DEFAULT "auth"."uid"() NOT NULL PRIMARY KEY REFERENCES "public"."profiles"("id") ON UPDATE CASCADE ON DELETE CASCADE,
    "linkedin_url" "text"
);

ALTER TABLE "public"."profiles" OWNER TO "postgres";
ALTER TABLE "public"."connections" OWNER TO "postgres";
ALTER TABLE "public"."socials" OWNER TO "postgres";

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

ALTER TABLE "public"."profiles" ENABLE ROW LEVEL SECURITY;

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

ALTER TABLE "public"."connections" ENABLE ROW LEVEL SECURITY;

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

ALTER TABLE "public"."socials" ENABLE ROW LEVEL SECURITY;

CREATE OR REPLACE FUNCTION "public"."handle_new_user"() RETURNS "trigger"
    LANGUAGE "plpgsql" SECURITY DEFINER
    SET "search_path" TO ''
    AS $$
BEGIN
  INSERT INTO public.profiles (id, name)
  VALUES (new.id, new.raw_user_meta_data ->> 'name');

  INSERT INTO public.socials (id, linkedin_url)
  VALUES (new.id, NULL);

  RETURN new;
END;
$$;

ALTER FUNCTION "public"."handle_new_user"() OWNER TO "postgres";

CREATE OR REPLACE TRIGGER "on_auth_user_created" AFTER INSERT ON "auth"."users" FOR EACH ROW EXECUTE FUNCTION "public"."handle_new_user"();