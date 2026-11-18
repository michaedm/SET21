#!/bin/sh
# Startskript for alt-i-ett-imaget: Ollama i bakgrunnen, appen i forgrunnen
echo "Starter chatbot-modellen ..."
/bin/ollama serve &
until /bin/ollama list >/dev/null 2>&1; do sleep 1; done
echo "Modellen er klar. Starter appen på http://localhost:7070"
exec java -jar /app/app.jar
