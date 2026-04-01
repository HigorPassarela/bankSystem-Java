# Stage 1: Build com imagem oficial do Gradle (sem precisar de gradlew local)
FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /app

# Configurar JVM para desabilitar completamente SSL (apenas para desenvolvimento)
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false \
    -Dcom.sun.net.useSystemProxies=true \
    -Dtrust_all_cert=true \
    -Dcom.sun.net.ssl.checkRevocation=false \
    -Djdk.tls.trustNameService=true \
    -Djavax.net.ssl.trustStore= \
    -Djavax.net.ssl.trustStoreType=JKS"

# Copia apenas arquivos de build primeiro para cache de dependências
COPY build.gradle settings.gradle ./

# Criar arquivo gradle.properties para desabilitar SSL completamente
RUN echo "org.gradle.daemon=false" >> gradle.properties && \
    echo "systemProp.com.sun.net.ssl.checkRevocation=false" >> gradle.properties && \
    echo "systemProp.trust_all_cert=true" >> gradle.properties && \
    echo "org.gradle.jvmargs=-Dcom.sun.net.ssl.checkRevocation=false -Dtrust_all_cert=true -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStoreType=JKS" >> gradle.properties

RUN gradle dependencies --no-daemon --no-configuration-cache --insecure || true

# Copia o código fonte e compila
COPY src ./src
RUN gradle bootJar --no-daemon -x test --no-configuration-cache --insecure

# Stage 2: Runtime leve
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S banksystem && adduser -S banksystem -G banksystem
COPY build/libs/*.jar app.jar
USER banksystem
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]