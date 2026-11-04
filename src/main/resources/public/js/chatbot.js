// Chatbot nederst til høyre. Lastes på hver side med:
// <script src="js/chatbot.js" defer></script>
document.addEventListener("DOMContentLoaded", () => {
    const knapp = document.createElement("button");
    knapp.className = "chat-knapp";
    knapp.setAttribute("aria-label", "Åpne chat");
    knapp.textContent = "💬";

    const vindu = document.createElement("section");
    vindu.className = "chat-vindu";
    vindu.hidden = true;
    vindu.innerHTML = `
        <header class="chat-topp">
            <span>Spør oss om husflid</span>
            <button class="chat-lukk" aria-label="Lukk chat">×</button>
        </header>
        <div class="chat-meldinger" aria-live="polite"></div>
        <form class="chat-skjema">
            <input type="text" maxlength="500" placeholder="Skriv et spørsmål …" aria-label="Melding" required>
            <button type="submit">Send</button>
        </form>
        <p class="chat-merknad">Eksempel – svarene kan inneholde feil.</p>
    `;

    document.body.append(vindu, knapp);

    const meldinger = vindu.querySelector(".chat-meldinger");
    const skjema = vindu.querySelector(".chat-skjema");
    const felt = skjema.querySelector("input");
    const sendKnapp = skjema.querySelector("button");

    function visMelding(tekst, hvem) {
        const boble = document.createElement("p");
        boble.className = "chat-melding chat-" + hvem;
        boble.textContent = tekst;   // textContent, så HTML i svaret ikke kjøres
        meldinger.appendChild(boble);
        meldinger.scrollTop = meldinger.scrollHeight;
        return boble;
    }

    function apneLukk() {
        vindu.hidden = !vindu.hidden;
        if (!vindu.hidden) {
            if (!meldinger.hasChildNodes()) {
                visMelding("Hei! Jeg kan svare på spørsmål om husflid og håndverk.", "bot");
            }
            felt.focus();
        }
    }

    knapp.addEventListener("click", apneLukk);
    vindu.querySelector(".chat-lukk").addEventListener("click", apneLukk);

    skjema.addEventListener("submit", async (event) => {
        event.preventDefault();
        const tekst = felt.value.trim();
        if (!tekst) {
            return;
        }

        visMelding(tekst, "bruker");
        felt.value = "";
        sendKnapp.disabled = true;

        const venter = visMelding("Skriver …", "bot");
        const treg = setTimeout(() => {
            venter.textContent = "Chatboten våkner, dette kan ta litt tid …";
        }, 15000);

        try {
            const svar = await fetch("/api/chat", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ melding: tekst })
            });
            const data = await svar.json();
            venter.textContent = svar.ok ? data.svar : data.feil;
        } catch {
            venter.textContent = "Fikk ikke kontakt med chatboten. Prøv igjen om litt.";
        } finally {
            clearTimeout(treg);
            sendKnapp.disabled = false;
            felt.focus();
        }
    });
});
