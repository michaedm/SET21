# Hele appen + chatboten i ETT image, til lokal testing i Docker Desktop.
#
# Bygg (fra repo-mappa):
#   docker build -f alt-i-ett.Dockerfile -t set21-alt-i-ett .
#
# Supabase-nøkkelen er IKKE med i imaget. Gi den ved oppstart:
#   docker run -d --name set21 -p 7070:7070 --env-file .env set21-alt-i-ett
# (eller i Docker Desktop: Run -> Optional settings -> port 7070 + SUPABASE_URL og SUPABASE_KEY)

# --- Steg 1: bygg Java-appen (samme som Dockerfile) ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# --- Steg 2: hent Java-kjøremiljøet ---
FROM eclipse-temurin:17-jre AS jre

# --- Steg 3: Ollama med modellen (samme som chatbot/Dockerfile) + appen ---
FROM ollama/ollama:0.40.12

ENV OLLAMA_HOST=0.0.0.0:7860 \
    OLLAMA_MODELS=/models \
    HOME=/home/ollama

RUN mkdir -p /models /home/ollama \
    && (ollama serve & sleep 5 && ollama pull gemma3:1b) \
    && chown -R 1000:1000 /models /home/ollama

# Java fra steg 2
COPY --from=jre /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk \
    PATH=/opt/java/openjdk/bin:$PATH \
    OLLAMA_URL=http://localhost:7860

WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
COPY chatbot/start-alt.sh start-alt.sh

USER 1000
EXPOSE 7070

# Starter Ollama i bakgrunnen, venter til den er klar, og starter så appen
ENTRYPOINT ["/bin/sh", "/app/start-alt.sh"]
