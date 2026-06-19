# Quick Start Guide

## ⚡ Requisitos previos

- Java 21
- Maven 3.9+

## 🛠️ Compilar el proyecto

```bash
mvn clean package
```

## 🧪 Ejecutar pruebas

```bash
mvn test
mvn verify
```

## ▶️ Ejecutar el ejemplo

```bash
java -jar target/limesurvey-clientV2-0.1.0-shaded.jar
```

## 📌 Notas adicionales

- Las pruebas de integración se ejecutan con Testcontainers.
- Si necesitas ajustar la configuración de Maven o las pruebas, revisa el archivo [pom.xml](pom.xml).
