document.addEventListener("DOMContentLoaded", () => {
    const skjema = document.querySelector("#admin-skjema");
    const tittel = document.querySelector("#tittel");
    const innhold = document.querySelector("#innhold");
    const publisertDato = document.querySelector("#publisert_dato");
    const melding = document.querySelector("#melding");

    skjema.addEventListener("submit", (event) => {
        event.preventDefault();

        const nyhet = {
            tittel: tittel.value.trim(),
            innhold: innhold.value.trim(),
            publisert_dato: publisertDato.value
        };

        if (
            !nyhet.tittel ||
            !nyhet.innhold ||
            !nyhet.publisert_dato
        ) {
            melding.textContent = "Fyll ut alle feltene.";
            
            return;
        }

        console.log("Nyhet:", nyhet);
        
         skjema.reset();
         melding.textContent = "";
    })
})