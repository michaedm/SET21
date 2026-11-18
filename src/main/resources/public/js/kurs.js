async function hentKurs() {
    const kursListe = document.getElementById("kurs-liste");

    try {
        const response = await fetch("/api/kurs");

        if (!response.ok) {
            throw new Error("Kunne ikke hente kurs");
        }

        const kurs = await response.json();

        kursListe.innerHTML = "";

        if (kurs.length === 0) {
            kursListe.textContent = "Ingen kurs tilgjengelig akkurat nå.";
            return;
        }

        kurs.forEach(k => {
            const kort = document.createElement("article");
            kort.className = "kurs-kort";
            kort.dataset.kat = (k.tema || "").toLowerCase();

            const dato = document.createElement("time");
            dato.className = "dato";

            if (k.startdato) {
                const startdato = new Date(k.startdato);

                const dag = document.createElement("span");
                dag.className = "dag";
                dag.textContent = startdato.getUTCDate();

                const maaned = document.createElement("span");
                maaned.className = "mnd";
                maaned.textContent = startdato.toLocaleDateString("nb-NO", {
                    month: "short",
                    timeZone: "UTC"
                });

                dato.dateTime = k.startdato;
                dato.appendChild(dag);
                dato.appendChild(maaned);
            } else {
                dato.textContent = "Dato kommer";
            }

            const kategori = document.createElement("span");
            kategori.className = "kat";
            kategori.textContent = k.tema || "Kurs";

            const overskrift = document.createElement("h3");
            overskrift.textContent = k.tittel || "Uten tittel";

            const beskrivelse = document.createElement("p");
            beskrivelse.textContent =
                k.beskrivelse || "Ingen beskrivelse tilgjengelig.";

            const fakta = document.createElement("ul");
            fakta.className = "fakta";

            const sted = document.createElement("li");
            sted.textContent = k.sted || "Sted kommer";

            const pris = document.createElement("li");

            if (k.pris == null) {
                pris.textContent = "Pris kommer";
            } else if (Number(k.pris) === 0) {
                pris.textContent = "Gratis";
            } else {
                pris.textContent = `Kr ${k.pris}`;
            }

            fakta.appendChild(sted);
            fakta.appendChild(pris);

            const meldPa = document.createElement("a");
            meldPa.className = "meld-pa";
            meldPa.textContent = "Meld på";
            // Går til påmeldingsskjemaet med kurset forhåndsvalgt
            meldPa.href = `pamelding.html?kurs=${encodeURIComponent(k.kurs_id)}`;

            kort.appendChild(dato);
            kort.appendChild(kategori);
            kort.appendChild(overskrift);
            kort.appendChild(beskrivelse);
            kort.appendChild(fakta);
            kort.appendChild(meldPa);

            kursListe.appendChild(kort);
        });

    } catch (error) {
        console.error("Feil ved henting av kurs:", error);
        kursListe.textContent = "Kunne ikke laste kurs";
    }
}

function filtrerKurs() {
    const valgtFilter = document.querySelector('input[name="kat"]:checked').id;

    const kortene = document.querySelectorAll(".kurs-kort");

    kortene.forEach(kort => {
        const kategori = kort.dataset.kat;

        if (valgtFilter === "f-alle") {
            kort.style.display = "";
        } else if (valgtFilter === "f-strikk" && kategori === "strikking") {
            kort.style.display = "";
        } else if (valgtFilter === "f-hekling" && kategori === "hekling") {
            kort.style.display = "";
        } else if (valgtFilter === "f-sy" &&
                   (kategori === "søm" || kategori === "vev" || kategori === "søm og vev")) {
            kort.style.display = "";
        } else {
            kort.style.display = "none";
        }
    });
}
document.querySelectorAll('input[name="kat"]').forEach(radio => {
    radio.addEventListener("change", filtrerKurs);
});
hentKurs();
