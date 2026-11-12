// Påmeldingsskjema.
// Henter kursene fra /api/kurs til nedtrekkslisten og sender skjemaet som JSON til PAMELDING_URL.
// NB: /api/pamelding finnes ikke i backend ennå (Michael lager den). Feltnavnene under
// (kurs_id, fornavn, etternavn, epost, telefon) kan justeres etter tabellen i Supabase.
const PAMELDING_URL = "/api/pamelding";

const skjema = document.getElementById("pamelding-skjema");
const kursValg = document.getElementById("kurs");
const melding = document.getElementById("pamelding-melding");

function visMelding(tekst, feil) {
    melding.textContent = tekst;
    melding.classList.toggle("feil", feil);
    melding.hidden = false;
}

async function lastKurs() {
    const valgtId = new URLSearchParams(window.location.search).get("kurs");
    try {
        const svar = await fetch("/api/kurs");
        if (!svar.ok) throw new Error();
        const kurs = await svar.json();

        kursValg.innerHTML = '<option value="">Velg kurs</option>';
        kurs.forEach(k => {
            const valg = document.createElement("option");
            valg.value = k.kurs_id;
            valg.textContent = k.tittel;
            if (String(k.kurs_id) === valgtId) valg.selected = true;
            kursValg.appendChild(valg);
        });
    } catch (e) {
        kursValg.innerHTML = '<option value="">Kunne ikke hente kurs</option>';
    }
}

skjema.addEventListener("submit", async (hendelse) => {
    hendelse.preventDefault();
    const knapp = skjema.querySelector("button[type=submit]");
    knapp.disabled = true;
    melding.hidden = true;

    const data = Object.fromEntries(new FormData(skjema));
    data.kurs_id = Number(data.kurs_id);

    try {
        const svar = await fetch(PAMELDING_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });
        if (!svar.ok) throw new Error(await svar.text());

        visMelding("Takk! Påmeldingen din er registrert.", false);
        skjema.reset();
    } catch (e) {
        visMelding("Kunne ikke sende påmeldingen. Prøv igjen senere.", true);
    } finally {
        knapp.disabled = false;
    }
});

lastKurs();
