# Nyheter API

Base-adresse lokalt: `http://localhost:7070`

| Metode | Adresse | Hvem | Hva |
|---|---|---|---|
| GET | `/api/nyheter` | Alle | Alle nyheter, nyeste først |
| GET | `/api/nyheter/{id}` | Alle | Én nyhet (til «Les mer»-siden) |
| POST | `/api/nyheter` | Innlogget redaktør | Publiser ny nyhet |
| PUT | `/api/nyheter/{id}` | Innlogget redaktør | Endre en nyhet |
| DELETE | `/api/nyheter/{id}` | Innlogget redaktør | Slett en nyhet |

## Nyhet (det API-et svarer med)

```json
{
  "nyhet_id": 7,
  "tittel": "Julemarked på husflidsstua",
  "innhold": "Kom og se håndverk fra medlemmene våre.",
  "bilde_url": null,
  "publisert_dato": "2026-11-01",
  "lokallag_id": 1
}
```

## GET /api/nyheter

Valgfrie parametere:
- `lokallag_id` – bare nyheter fra ett lokallag, f.eks. `/api/nyheter?lokallag_id=2`
- `antall` – maks antall nyheter (høyst 100), f.eks. `/api/nyheter?antall=5` til forsiden

Sortert på `publisert_dato`, nyeste først.

## GET /api/nyheter/{id}

`200` med nyheten, eller `404` hvis den ikke finnes.

## POST og PUT (publisere og endre)

Send JSON i body:

```json
{
  "tittel": "Julemarked på husflidsstua",
  "innhold": "Kom og se håndverk fra medlemmene våre.",
  "bilde_url": "https://eksempel.no/bilde.jpg",
  "publisert_dato": "2026-11-01",
  "lokallag_id": 1
}
```

| Felt | Påkrevd | Regel |
|---|---|---|
| `tittel` | Ja | 1–200 tegn |
| `lokallag_id` | Ja | Et lokallag som finnes |
| `innhold` | Nei | Maks 50 000 tegn |
| `bilde_url` | Nei | Må starte med `http://` eller `https://` |
| `publisert_dato` | Nei | `ÅÅÅÅ-MM-DD`. Mangler den, brukes dagens dato |

PUT erstatter alle feltene over, så send hele nyheten (også felt som ikke er endret).

Svar: POST gir `201` med den lagrede nyheten (inkl. `nyhet_id`). PUT gir `200` med den endrede nyheten.

## DELETE

Gir `204` (ingen body) når nyheten er slettet.

## Innlogging

POST, PUT og DELETE krever at redaktøren er logget inn med Supabase Auth (som i `login.js`).
Send tokenet i headeren `Authorization`:

```js
const { data } = await db.auth.getSession();
if (!data.session) { window.location.href = "login.html"; }

const svar = await fetch("/api/nyheter", {
    method: "POST",
    headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + data.session.access_token
    },
    body: JSON.stringify({ tittel, innhold, lokallag_id: 1 })
});

if (!svar.ok) {
    const { feil } = await svar.json();
    alert(feil);
}
```

Backend sender tokenet videre til Supabase, så det er databasens regler (RLS) som avgjør om redaktøren har lov.

## Feil

Alle feil svarer med JSON: `{ "feil": "forklaring på norsk" }`

| Kode | Betyr |
|---|---|
| 400 | Ugyldige data (f.eks. mangler tittel, feil datoformat, lokallag finnes ikke) |
| 401 | Ikke logget inn, eller innloggingen har gått ut. Send brukeren til `login.html` |
| 403 | Logget inn, men har ikke lov |
| 404 | Nyheten finnes ikke |
| 500 | Noe gikk galt mot databasen |

## Til frontend: vis tekst trygt

Nyhetene skrives av redaktører, og teksten vises på nettsiden. Bruk `textContent`
(ikke `innerHTML`) når tittel og innhold settes inn, så HTML i en nyhet ikke kjøres som kode.
