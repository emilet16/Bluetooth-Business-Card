create table "public"."connections" (
    "requested_by" uuid not null,
    "requested_for" uuid not null,
    "created_at" timestamp with time zone default now(),
    "updated_at" timestamp with time zone default now(),
    "status" text
);


create table "public"."profiles" (
    "id" uuid not null default auth.uid(),
    "created_at" timestamp with time zone not null default now(),
    "name" text not null default ''::text,
    "job" text not null default ''::text,
    "pfp_url" text
);


create table "public"."socials" (
    "id" uuid not null default auth.uid(),
    "linkedin_url" text
);


grant delete on table "public"."connections" to "anon";

grant insert on table "public"."connections" to "anon";

grant references on table "public"."connections" to "anon";

grant select on table "public"."connections" to "anon";

grant trigger on table "public"."connections" to "anon";

grant truncate on table "public"."connections" to "anon";

grant update on table "public"."connections" to "anon";

grant delete on table "public"."connections" to "authenticated";

grant insert on table "public"."connections" to "authenticated";

grant references on table "public"."connections" to "authenticated";

grant select on table "public"."connections" to "authenticated";

grant trigger on table "public"."connections" to "authenticated";

grant truncate on table "public"."connections" to "authenticated";

grant update on table "public"."connections" to "authenticated";

grant delete on table "public"."connections" to "service_role";

grant insert on table "public"."connections" to "service_role";

grant references on table "public"."connections" to "service_role";

grant select on table "public"."connections" to "service_role";

grant trigger on table "public"."connections" to "service_role";

grant truncate on table "public"."connections" to "service_role";

grant update on table "public"."connections" to "service_role";

grant delete on table "public"."profiles" to "anon";

grant insert on table "public"."profiles" to "anon";

grant references on table "public"."profiles" to "anon";

grant select on table "public"."profiles" to "anon";

grant trigger on table "public"."profiles" to "anon";

grant truncate on table "public"."profiles" to "anon";

grant update on table "public"."profiles" to "anon";

grant delete on table "public"."profiles" to "authenticated";

grant insert on table "public"."profiles" to "authenticated";

grant references on table "public"."profiles" to "authenticated";

grant select on table "public"."profiles" to "authenticated";

grant trigger on table "public"."profiles" to "authenticated";

grant truncate on table "public"."profiles" to "authenticated";

grant update on table "public"."profiles" to "authenticated";

grant delete on table "public"."profiles" to "service_role";

grant insert on table "public"."profiles" to "service_role";

grant references on table "public"."profiles" to "service_role";

grant select on table "public"."profiles" to "service_role";

grant trigger on table "public"."profiles" to "service_role";

grant truncate on table "public"."profiles" to "service_role";

grant update on table "public"."profiles" to "service_role";

grant delete on table "public"."socials" to "anon";

grant insert on table "public"."socials" to "anon";

grant references on table "public"."socials" to "anon";

grant select on table "public"."socials" to "anon";

grant trigger on table "public"."socials" to "anon";

grant truncate on table "public"."socials" to "anon";

grant update on table "public"."socials" to "anon";

grant delete on table "public"."socials" to "authenticated";

grant insert on table "public"."socials" to "authenticated";

grant references on table "public"."socials" to "authenticated";

grant select on table "public"."socials" to "authenticated";

grant trigger on table "public"."socials" to "authenticated";

grant truncate on table "public"."socials" to "authenticated";

grant update on table "public"."socials" to "authenticated";

grant delete on table "public"."socials" to "service_role";

grant insert on table "public"."socials" to "service_role";

grant references on table "public"."socials" to "service_role";

grant select on table "public"."socials" to "service_role";

grant trigger on table "public"."socials" to "service_role";

grant truncate on table "public"."socials" to "service_role";

grant update on table "public"."socials" to "service_role";


