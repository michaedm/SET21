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

    // Hent lokallag direkte fra Supabase
    try {
        const { data: lokallagListe, error } = await db
            .from("lokallag")
            .select("*")
            .order("navn", { ascending: true });

        if (error) {
            throw error;
        }

        lokallag.innerHTML = '<option value="">Velg lokallag</option>';

        lokallagListe.forEach((lag) => {
            const option = document.createElement("option");
            option.value = lag.lokallag_id;
            option.textContent = lag.navn;
            lokallag.appendChild(option);
        });

    } catch (error) {
        console.error("Feil ved henting av lokallag:", error);
        melding.textContent = "Kunne ikke hente lokallag fra Supabase.";
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

        if (
            !nyhet.tittel ||
            !nyhet.innhold ||
            !nyhet.publisert_dato ||
            !lokallag.value
        ) {
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
                const resultat = await svar.json();
                melding.textContent = resultat.feil || "Kunne ikke publisere nyheten.";
                return;
            }

            skjema.reset();
            melding.textContent = "Nyheten er publisert!";

        } catch (error) {
            console.error("Feil ved publisering:", error);
            melding.textContent = "Noe gikk galt: " + error.message;
        }
    });
});
