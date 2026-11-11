// Eksempeltekst som vises i "Les mer". Velges ut fra ord i overskriften.
const eksempelTekster = [
    {
        nokkelord: ["julemarked", "husflidsstua"],
        avsnitt: [
            "Det nærmer seg jul, og husflidsstua åpner dørene for et koselig julemarked. Her kan du møte medlemmene våre og se håndverket de har jobbet med gjennom hele året.",
            "På salg finner du blant annet strikkevarer, vevde tekstiler, tresløyd, keramikk og håndlagde julepynt. Alle produktene er laget av lokale håndverkere, og mange av dem er unike.",
            "Det blir servering av kaffe, vafler og gløgg, og barna kan være med på enkel julepynt-verksted. Inngangen er gratis, og alle er velkomne.",
            "Overskuddet fra salg av kaffe og vafler går til å drifte husflidsstua og til nye kurs for medlemmene."
        ]
    },
    {
        nokkelord: ["vevkurs", "vev"],
        avsnitt: [
            "Vi har fortsatt noen få ledige plasser på vevkurset i Sarpsborg. Kurset passer for både nybegynnere og dere som har vevd før.",
            "Du lærer å sette opp en vev, velge garn og mønster, og lage din egen løper eller pute. Kursleder hjelper deg underveis, og alt av utstyr er tilgjengelig på kurset.",
            "Kurset går over flere kvelder, og du betaler kun for garnet du bruker. Plassene fordeles etter først til mølla-prinsippet.",
            "Ta kontakt med oss så snart som mulig hvis du vil være med."
        ]
    },
    {
        nokkelord: ["bunadskurs", "bunad"],
        avsnitt: [
            "Bunadskurset ble raskt fullt, og interessen har vært så stor at vi har bestemt oss for å sette opp et ekstra kurs.",
            "På kurset får du hjelp til å sy, brodere og tilpasse din egen bunad, med veiledning fra erfarne kursledere. Du lærer også om bunadens historie og hvordan den skal stelles.",
            "Det ekstra kurset starter i løpet av høsten, og det er plass til færre deltakere, slik at alle får god oppfølging.",
            "Meld deg på i dag, og sikre deg en plass på ventelisten hvis kurset skulle bli fullt."
        ]
    },
    {
        nokkelord: ["strikkekurs", "strikk"],
        avsnitt: [
            "I oktober starter vi et nytt strikkekurs for nybegynnere. Du trenger ingen forkunnskaper, bare lyst til å lære.",
            "Du lærer grunnleggende teknikker som å legge opp, strikke rett og vrang, øke, felle og felle av. Målet er at du skal sitte igjen med din egen lue eller hals ved kursets slutt.",
            "Kurset holdes i en hyggelig og avslappet atmosfære, og vi tar pauser med kaffe og en prat underveis.",
            "Påmeldingen åpner snart. Følg med her på siden for å få med deg datoen."
        ]
    }
];

const standardTekst = [
    "Dette er en nyhet fra Østfold husflid. Her får du høre om det som skjer i lokallagene våre.",
    "Vi arrangerer jevnlig kurs, markeder og treff for deg som er glad i håndverk. Følg med for å få med deg nye arrangementer.",
    "Har du spørsmål, er du velkommen til å ta kontakt med oss."
];

function hentAvsnitt(tittel) {
    const t = (tittel || "").toLowerCase();
    const treff = eksempelTekster.find(e => e.nokkelord.some(ord => t.includes(ord)));
    return treff ? treff.avsnitt : standardTekst;
}

function escapeHtml(tekst) {
    const div = document.createElement("div");
    div.textContent = tekst == null ? "" : String(tekst);
    return div.innerHTML;
}

// ---------- Les mer-dialog ----------
const lesMerDialog = document.createElement("div");
lesMerDialog.className = "modal-bakgrunn";
lesMerDialog.hidden = true;
lesMerDialog.innerHTML = `
    <div class="modal-boks" role="dialog" aria-modal="true" aria-labelledby="modal-tittel">
        <button type="button" class="modal-lukk" aria-label="Lukk">&times;</button>
        <h2 id="modal-tittel"></h2>
        <time class="modal-dato"></time>
        <img class="modal-bilde klikkbart-bilde" alt="Nyhetsbilde">
        <div class="modal-tekst"></div>
    </div>
`;
document.body.appendChild(lesMerDialog);

function lukkLesMer() {
    lesMerDialog.hidden = true;
}

function visLesMer(nyhet) {
    lesMerDialog.querySelector("#modal-tittel").textContent = nyhet.tittel;
    lesMerDialog.querySelector(".modal-dato").textContent = nyhet.publisert_dato || "";

    const bilde = lesMerDialog.querySelector(".modal-bilde");
    bilde.hidden = !nyhet.bilde_url;
    if (nyhet.bilde_url) bilde.src = nyhet.bilde_url;

    const tekst = lesMerDialog.querySelector(".modal-tekst");
    tekst.innerHTML = "";
    const intro = document.createElement("p");
    intro.className = "modal-ingress";
    intro.textContent = nyhet.innhold || "";
    tekst.appendChild(intro);
    hentAvsnitt(nyhet.tittel).forEach(a => {
        const p = document.createElement("p");
        p.textContent = a;
        tekst.appendChild(p);
    });

    lesMerDialog.hidden = false;
}

lesMerDialog.addEventListener("click", e => {
    if (e.target === lesMerDialog || e.target.classList.contains("modal-lukk")) lukkLesMer();
});

// ---------- Bilde-forstørrelse ----------
const bildeVisning = document.createElement("div");
bildeVisning.className = "lightbox";
bildeVisning.hidden = true;
bildeVisning.innerHTML = `<img alt="Forstørret bilde">`;
document.body.appendChild(bildeVisning);

bildeVisning.addEventListener("click", () => { bildeVisning.hidden = true; });

document.addEventListener("click", e => {
    const img = e.target.closest(".klikkbart-bilde");
    if (!img) return;
    bildeVisning.querySelector("img").src = img.src;
    bildeVisning.hidden = false;
});

document.addEventListener("keydown", e => {
    if (e.key !== "Escape") return;
    if (!bildeVisning.hidden) bildeVisning.hidden = true;
    else if (!lesMerDialog.hidden) lukkLesMer();
});

// ---------- Hent nyheter ----------
fetch("/api/nyheter")
    .then(response => response.json())
    .then(data => {

        let nyheter = document.getElementById("nyheter");

        data.forEach(nyhet => {

            let article = document.createElement("article");

            article.innerHTML = `
                <h3>${escapeHtml(nyhet.tittel)}</h3>
                <p>${escapeHtml(nyhet.innhold)}</p>
                <time>${escapeHtml(nyhet.publisert_dato)}</time>
                <a href="#">Les mer</a>
                <img class="article_img klikkbart-bilde" src="${escapeHtml(nyhet.bilde_url)}" alt="Nyhetsbilde">
            `;

            article.querySelector("a").addEventListener("click", e => {
                e.preventDefault();
                visLesMer(nyhet);
            });

            nyheter.appendChild(article);
        });
    });
