# Quick Start Guide - LimeSurvey Testing

## ⚡ Comando Rápido (30 segundos)

**Windows:**
```
limesurvey.bat start
```

**Linux/Mac:**
```
./limesurvey.sh start
```

Luego accede a: **http://localhost**
- Usuario: `admin`
- Contraseña: `admin123`

---

## 🧪 Ejecutar Tests (60 segundos)

```bash
# Terminal 1: Inicia LimeSurvey
./limesurvey.sh start

# Terminal 2: Ejecuta tests
./limesurvey.sh test:all

# Detener
./limesurvey.sh stop
```

---

## 📌 Cambiar a LimeSurvey v7

```bash
./limesurvey.sh version 7
./limesurvey.sh stop
./limesurvey.sh start
```

---

## 📖 Documentación Completa

Ver: **DOCKER.md**

```bash
# Ver toda la ayuda
./limesurvey.sh help
```

---

## ⚠️ Requisitos Previos

✅ Docker instalado  
✅ Docker Compose instalado  
✅ Maven 3.9+ (para tests)  
✅ Java 21 (para tests)

[Instalar Docker](https://docs.docker.com/get-docker/)

---

**¿Problemas?** Ver sección de Troubleshooting en DOCKER.md
