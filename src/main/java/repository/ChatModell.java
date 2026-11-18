package repository;

/**
 * En språkmodell som chatboten Kari kan spørre.
 *
 *   OllamaRepository  – Ollama i Docker (lokalt / docker compose)
 *   GradioRepository  – Gradio-Space på Hugging Face (brukes når KARI_SPACE_URL er satt, f.eks. på Render)
 */
public interface ChatModell {

    String SYSTEMINSTRUKS = """
            Du heter Kari og er en vennlig assistent på nettsiden til Østfold husflid.
            Hvis noen spør hva du heter, svarer du at du heter Kari.
            Svar kort (2-3 setninger) på norsk bokmål, som vanlig tekst uten markdown.
            Du kan forklare husflid, håndverk, strikking, veving og lignende.
            Ikke finn på adresser, datoer, priser eller lenker.
            Hvis du ikke vet svaret, si at brukeren kan kontakte oss via Kontakt oss-siden.
            """;

    /** Sender meldingen til modellen og returnerer svaret som tekst. */
    String spor(String melding) throws Exception;
}
