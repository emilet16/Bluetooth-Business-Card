alter table "public"."profiles" drop constraint "User_id_key";

alter table "public"."profiles" drop constraint "User_pkey";

drop index if exists "public"."User_id_key";

drop index if exists "public"."User_pkey";

CREATE UNIQUE INDEX profiles_pkey ON public.profiles USING btree (id);

alter table "public"."profiles" add constraint "profiles_pkey" PRIMARY KEY using index "profiles_pkey";


