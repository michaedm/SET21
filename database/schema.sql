-- database/schema.sql
-- Husflidslag Østfold, sprint 1

create table lokallag (
  lokallag_id serial primary key,
  navn varchar not null
);

create table publish (
  nyhet_id serial primary key,
  tittel varchar not null,
  innhold text,
  bilde_url varchar,
  publisert_dato date not null,
  lokallag_id int4 not null references lokallag (lokallag_id)
);

create table kurs (
  kurs_id serial primary key,
  tittel varchar not null,
  tema varchar,
  beskrivelse text,
  sted varchar,
  startdato date not null,
  sluttdato date,
  maks_deltakere int4 not null,
  pris numeric not null,
  status varchar not null,
  lokallag_id int4 not null references lokallag (lokallag_id)
);

-- Row Level Security
alter table lokallag enable row level security;
alter table publish enable row level security;
alter table kurs enable row level security;

create policy "Alle kan lese lokallag"
  on public.lokallag for select to public using (true);

create policy "Alle kan lese nyheter"
  on public.publish for select to public using (true);

create policy "Innloggede kan endre nyheter"
  on public.publish for all to authenticated using (true) with check (true);

-- kurs har ingen policyer ennå (sprint 2)