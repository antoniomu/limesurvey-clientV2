# Docker Setup para LimeSurvey Testing

Este proyecto incluye una configuración Docker completa para ejecutar **LimeSurvey** v6 (con fácil cambio a v7) y ejecutar tests contra él.

## 🚀 Requisitos

- **Docker**: [Instalar Docker](https://docs.docker.com/get-docker/)
- **Docker Compose**: [Instalar Docker Compose](https://docs.docker.com/compose/install/)
- **Maven 3.9+** y **Java 21** (para ejecutar tests localmente)

## 📁 Archivos de Configuración

```
docker-compose.yml           # Stack principal (LimeSurvey + MySQL)
docker-compose.test.yml      # Composición adicional para tests
Dockerfile.test              # Imagen para ejecutar tests
limesurvey.sh / limesurvey.bat  # Scripts de automatización
.env.example                 # Variables de configuración
```

## 🎯 Inicio Rápido

### En Windows:
```bash
limesurvey.bat start
```

### En Linux/Mac:
```bash
chmod +x limesurvey.sh
./limesurvey.sh start
```

Esto iniciará:
- **MySQL** en el puerto 3306
- **LimeSurvey** en el puerto 80 (http://localhost)

Credenciales por defecto:
- Usuario: `admin`
- Contraseña: `admin123`

## 📋 Comandos Disponibles

### Gestión de Servicios

| Comando | Descripción |
|---------|-------------|
| `start` | Inicia la stack completa |
| `stop` | Detiene todos los contenedores |
| `restart` | Reinicia los servicios |
| `clean` | Elimina contenedores y volúmenes (⚠️ pierde datos) |
| `status` | Muestra el estado actual |
| `logs [service]` | Muestra logs en tiempo real |

**Ejemplo:**
```bash
./limesurvey.sh logs limesurvey-app
```

### Testing

| Comando | Descripción |
|---------|-------------|
| `test` | Ejecuta tests unitarios |
| `test:integration` | Ejecuta tests de integración contra LimeSurvey |
| `test:all` | Ejecuta todos los tests |

**Ejemplo:**
```bash
./limesurvey.sh start
./limesurvey.sh test:all
```

### Control de Versión

| Comando | Descripción |
|---------|-------------|
| `version 6` | Cambia a LimeSurvey v6 |
| `version 7` | Cambia a LimeSurvey v7 |

**Ejemplo:**
```bash
./limesurvey.sh version 7
./limesurvey.sh stop
./limesurvey.sh start  # Inicia con v7
```

## 🔧 Configuración Detallada

### Variables de Entorno

Edita `.env.example` (o crea `.env`) para personalizar:

```env
# Versión de LimeSurvey (6-apache o 7-apache)
LIMESURVEY_VERSION=6-apache

# Credenciales de base de datos
DB_USERNAME=limesurvey
DB_PASSWORD=limesurvey123
DB_NAME=limesurvey

# Credenciales de administrador
ADMIN_USER=admin
ADMIN_PASSWORD=admin123
ADMIN_EMAIL=admin@limesurvey.local

# URL base de LimeSurvey
LIMESURVEY_URL_FORMAT=http://localhost

# Configuración de tests
LIMESURVEY_TEST_URL=http://localhost
LIMESURVEY_TEST_USER=admin
LIMESURVEY_TEST_PASSWORD=admin123
```

### Cambiar a LimeSurvey v7

Opción 1: Usar el script
```bash
./limesurvey.sh version 7
./limesurvey.sh stop
./limesurvey.sh start
```

Opción 2: Manual - edita `docker-compose.yml`:
```yaml
services:
  limesurvey:
    image: martialblog/limesurvey:7-apache  # Cambia aquí
```

Luego reinicia:
```bash
docker-compose down
docker-compose up -d
```

## 🧪 Ejecutar Tests

### Tests Unitarios (sin LimeSurvey)
```bash
./limesurvey.sh test
```

### Tests de Integración (requiere LimeSurvey)
```bash
# 1. Inicia el stack
./limesurvey.sh start

# 2. Ejecuta los tests
./limesurvey.sh test:integration

# 3. Detén cuando termines
./limesurvey.sh stop
```

### Todos los Tests
```bash
./limesurvey.sh start
./limesurvey.sh test:all
./limesurvey.sh stop
```

### Ejecutar Maven directamente
```bash
# Solo tests unitarios
mvn -DskipTests=false -Dtest=*Test test

# Tests de integración
mvn verify

# Con output detallado
mvn verify -X
```

## 🐛 Troubleshooting

### LimeSurvey no inicia
```bash
# Verifica logs
./limesurvey.sh logs limesurvey-app

# Reinicia todo
./limesurvey.sh stop
./limesurvey.sh start
```

### Base de datos no responde
```bash
# Verifica MySQL
./limesurvey.sh logs limesurvey-db

# Limpia y reinicia
./limesurvey.sh clean
./limesurvey.sh start
```

### Tests fallan con "Connection refused"
- Asegúrate que LimeSurvey está corriendo: `./limesurvey.sh status`
- Espera un poco a que LimeSurvey se inicialice
- Verifica que el puerto 80 está disponible

### Puerto 80 ya está en uso
Edita `docker-compose.yml` y cambia:
```yaml
ports:
  - "8080:80"  # Ahora en puerto 8080
```

## 📊 Monitoreo

Ver todos los servicios:
```bash
./limesurvey.sh status
```

Ver logs en tiempo real:
```bash
./limesurvey.sh logs

# O de un servicio específico
./limesurvey.sh logs limesurvey-app
./limesurvey.sh logs limesurvey-db
```

Conectar a la base de datos directamente:
```bash
docker exec -it limesurvey-db mysql -ulimesurvey -plimesurvey123 limesurvey
```

## 🔐 Seguridad

⚠️ **IMPORTANTE**: Esta configuración es para **desarrollo y testing**. No uses en producción.

Para producción:
- Cambia todas las contraseñas en `docker-compose.yml`
- Usa volúmenes en ubicaciones seguras
- Configura HTTPS
- Limita acceso a la red
- Usa variables de entorno seguras

## 📝 Estructura del Proyecto

```
.
├── docker-compose.yml          # Stack principal
├── docker-compose.test.yml     # Configuración de tests
├── Dockerfile.test             # Builder para tests
├── limesurvey.sh               # Script Linux/Mac
├── limesurvey.bat              # Script Windows
├── .env.example                # Plantilla de variables
├── pom.xml                     # Configuración Maven
├── src/
│   ├── main/                   # Código fuente
│   └── test/                   # Tests
└── DOCKER.md                   # Este archivo
```

## 🚀 Workflow Típico

```bash
# 1. Primer inicio
./limesurvey.sh start

# 2. Desarrollar y escribir tests
# (editar src/test/...)

# 3. Ejecutar tests
./limesurvey.sh test:all

# 4. Probar contra LimeSurvey v7
./limesurvey.sh version 7
./limesurvey.sh stop
./limesurvey.sh start
./limesurvey.sh test:integration

# 5. Limpiar
./limesurvey.sh stop
./limesurvey.sh clean
```

## 💡 Tips

### Reutilizar el contenedor para desarrollo
```bash
# Terminal 1: Inicia stack
./limesurvey.sh start

# Terminal 2: Edita código y ejecuta tests
mvn test

# O con watch
mvn test -Dgroups=!integration
```

### Ejecutar tests en paralelo
```bash
mvn verify -T 1C  # 1 thread por core
```

### Depuración
```bash
# Con debug de Maven
mvn -X verify

# Ver output de contenedores
docker logs -f limesurvey-app
docker logs -f limesurvey-db
```

## 🤝 Contribución

Si encuentras problemas con esta configuración, abre un issue en el repositorio.

---

**Última actualización**: 2024
**Compatible con**: LimeSurvey v6, v7 | Docker 20+ | Docker Compose 2+
