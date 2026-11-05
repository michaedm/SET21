# Ollama med gemma3:1b ferdig nedlastet, klar for Hugging Face Spaces
FROM ollama/ollama:0.40.12

# Hugging Face krever port 7860 og kjører containeren som bruker 1000
ENV OLLAMA_HOST=0.0.0.0:7860 \
    OLLAMA_MODELS=/models \
    HOME=/home/ollama

# Last ned modellen mens imaget bygges, så den ikke må hentes ved hver oppstart
RUN mkdir -p /models /home/ollama \
    && (ollama serve & sleep 5 && ollama pull gemma3:1b) \
    && chown -R 1000:1000 /models /home/ollama

USER 1000
EXPOSE 7860

ENTRYPOINT ["/bin/ollama"]
CMD ["serve"]
