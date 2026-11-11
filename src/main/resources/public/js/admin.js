const SUPABASE_URL = "https://mujammhifcrqhjvejdmv.supabase.co";
const SUPABASE_KEY = "sb_publishable_4xM-nqgSmCBPCWvXp4I11g_hDnQ-W3q";

const db = supabase.createClient(SUPABASE_URL, SUPABASE_KEY);

document.addEventListener("DOMContentLoaded", async () => {
    const skjema = document.querySelector("#admin-skjema");
    const tittel = document.querySelector("#tittel");
    const innhold = document.querySelector("#innhold");
    const publisertDato = document.querySelector("#publisert_dato");
    const lokallag = document.querySelector("#lokallag_id");
    const melding = document.querySelector("#melding");

    // Sjekk om brukeren er logget inn
    const { data } = await db.auth.getSession();

    if (!data.session) {
        window.location.href = "login.html";
        return;
    }

    // Hent lokallag fra backend
    try {
        const svar = await fetch("/api/lokallag");

        if (!svar.ok) {
            throw new Error("Kunne ikke hente lokallag.");
        }

        const lokallagListe = await svar.json();

        lokallag.innerHTML = '<option value="">Velg lokallag</option>';

        lokallagListe.forEach((lag) => {
            const option = document.createElement("option");
            option.value = lag.lokallag_id;
            option.textContent = lag.navn;
            lokallag.appendChild(option);
        });

    } catch (error) {
        melding.textContent = error.message;
        return;
    }

    // Publiser nyhet
    skjema.addEventListener("submit", async (event) => {
        event.preventDefault();

        const nyhet = {
            tittel: tittel.value.trim(),
            innhold: innhold.value.trim(),
            publisert_dato: publisertDato.value,
            lokallag_id: Number(lokallag.value)
        };

        if (!nyhet.tittel || !nyhet.innhold || !nyhet.publisert_dato || !lokallag.value) {
            melding.textContent = "Fyll ut alle feltene.";
            return;
        }

        try {
            const { data } = await db.auth.getSession();

            if (!data.session) {
                window.location.href = "login.html";
                return;
            }

            const svar = await fetch("/api/nyheter", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + data.session.access_token
                },
                body: JSON.stringify(nyhet)
            });

            if (svar.status === 401) {
                window.location.href = "login.html";
                return;
            }

            if (!svar.ok) {
                const { feil } = await svar.json();
                melding.textContent = feil;
                return;
            }

            skjema.reset();
            melding.textContent = "Nyheten er publisert!";

        } catch (error) {
            melding.textContent = "Noe gikk galt: " + error.message;
        }
    });
});