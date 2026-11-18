---
title: SET21 Ollama
emoji: 🧶
colorFrom: red
colorTo: yellow
sdk: docker
app_port: 7860
pinned: false
---

# SET21 chatbot (Ollama)

Språkmodellen bak chatboten på nettsiden til Østfold husflid.
Kjører [Ollama](https://ollama.com) med modellen `gemma3:1b`.
Toppen av denne fila er innstillinger for Hugging Face Spaces.

## Kjør lokalt

```bash
docker build -t set21-ollama chatbot
docker run --rm -p 7860:7860 set21-ollama
```

Test at den svarer:

```bash
curl http://localhost:7860/api/chat -d '{"model":"gemma3:1b","stream":false,"messages":[{"role":"user","content":"Hva er husflid?"}]}'
```

## På Hugging Face

Lastes opp som et privat Docker-Space. SET21-appen når det via `OLLAMA_URL`
og sender med `HF_TOKEN`, så ingen andre kan bruke det.
