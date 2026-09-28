# Database (Supabase)

## Tilkobling
- Variabler i `.env`: `SUPABASE_URL` og `SUPABASE_KEY` (publishable key)
- Verdiene får du privat av databaseansvarlig. Ikke legg dem i Git.

## Tabeller
| Tabell | Kolonner |
|---|---|
| lokallag | lokallag_id, navn |
| publish (nyheter) | nyhet_id, tittel, innhold, bilde_url, publisert_dato, lokallag_id |
| kurs (sprint 2) | kurs_id, tittel, tema, beskrivelse, sted, startdato, sluttdato, maks_deltakere, pris, status, lokallag_id |

## Tilgang (RLS)
| Hvem | publish | lokallag |
|---|---|---|
| Besøkende (uinnlogget) | Kun lese | Kun lese |
| Innlogget redaktør | Lese, legge til, endre, slette | Kun lese |

## Eksempel: hent alle nyheter, nyeste først
GET {SUPABASE_URL}/rest/v1/publish?select=*&order=publisert_dato.desc
Header: apikey: {SUPABASE_KEY}

## Innlogging
Redaktør logger inn via Supabase Auth (e-post og passord).
Test-konto: redaktor@husflid-test.no (passord får du privat).

## Regler
- Kolonnenavn endres bare av databaseansvarlig, og gruppa får beskjed først.
- Skjemaet ligger i `database/schema.sql`.