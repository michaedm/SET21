const SUPABASE_URL = "https://mujammhifcrqhjvejdmv.supabase.co";
const SUPABASE_KEY = "sb_publishable_4xM-nqgSmCBPCWvXp4I11g_hDnQ-W3q";

const db = supabase.createClient(SUPABASE_URL, SUPABASE_KEY);

document.getElementById("login-skjema").addEventListener("submit", async (event)=> {
    event.preventDefault();

    const epost = document.getElementById("epost").value;
    const passord = document.getElementById("passord").value;

    const {error} = await db.auth.signInWithPassword ({
        email: epost,
        password: passord
    });

    const melding = document.getElementById("melding");

    if (error) {
        melding.textContent = "Feil e-post eller passord"
    }  else{
        window.location.href = "index.html";
    }
});
