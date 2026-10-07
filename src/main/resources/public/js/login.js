const skjema = document.getElementById("login-skjema");
const melding = document.getElementById("melding");

skjema.addEventListener("submit", (event) => {
    event.preventDefault();

    const epost = document.getElementById("epost").value;
    const passord = document.getElementById("passord").value;
})