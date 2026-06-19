# Advanced Docker Configuration Examples

## 📚 Ejemplos de Uso Avanzado

### 1. PostgreSQL en lugar de MySQL

Crea `docker-compose.postgres.yml`:

```yaml
version: '3.8'

services:
  limesurvey-db:
    image: postgres:15
    environment:
      POSTGRES_DB: limesurvey
      POSTGRES_USER: limesurvey
      POSTGRES_PASSWORD: limesurvey123
    ports:
      - "5432:5432"
    volumes:
      - limesurvey-db-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U limesurvey"]
      timeout: 20s
      retries: 10

  limesurvey:
    environment:
      DB_TYPE: pgsql
      DB_HOST: limesurvey-db
      DB_PORT: 5432
      DB_NAME: limesurvey
      DB_USERNAME: limesurvey
      DB_PASSWORD: limesurvey123

volumes:
  limesurvey-db-data:
```

Usar:
```bash
docker-compose -f docker-compose.yml -f docker-compose.postgres.yml up -d
```

### 2. HTTPS con certificados autofirmados

Crea `docker-compose.ssl.yml`:

```yaml
version: '3.8'

services:
  limesurvey:
    ports:
      - "443:443"
    volumes:
      - ./certs:/app/certs:ro
    environment:
      LIMESURVEY_URL_FORMAT: "https://localhost"
```

Genera certificados:
```bash
mkdir -p certs
openssl req -x509 -newkey rsa:4096 -keyout certs/key.pem -out certs/cert.pem -days 365 -nodes
```

### 3. Redis para caching

```yaml
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
```

### 4. Desarrollo con volumen local

Edita `docker-compose.yml` para el desarrollo:

```yaml
  limesurvey:
    volumes:
      - ./limesurvey-upload:/app/upload
      - ./limesurvey-config:/app/application/config
      - ./custom-plugins:/app/plugins/custom  # Volumen adicional para plugins
```

### 5. Diferentes perfiles para dev, test, prod

Crea `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  limesurvey-db:
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD_FILE: /run/secrets/db_root_password
      MYSQL_PASSWORD_FILE: /run/secrets/db_password
    secrets:
      - db_root_password
      - db_password

  limesurvey:
    restart: always
    environment:
      LOG_LEVEL: warning

secrets:
  db_root_password:
    file: ./secrets/db_root_password.txt
  db_password:
    file: ./secrets/db_password.txt
```

### 6. Monitoreo con Prometheus y Grafana

```yaml
services:
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
```

### 7. Ejecutar tests en Docker (sin Maven local)

```bash
docker-compose -f docker-compose.yml -f docker-compose.test.yml up --abort-on-container-exit
```

### 8. Backup y Restore de BD

**Backup:**
```bash
docker exec limesurvey-db mysqldump -ulimesurvey -plimesurvey123 limesurvey > backup.sql
```

**Restore:**
```bash
docker exec -i limesurvey-db mysql -ulimesurvey -plimesurvey123 limesurvey < backup.sql
```

### 9. Logs centralizados (ELK Stack)

```yaml
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.0.0
    environment:
      - discovery.type=single-node
    ports:
      - "9200:9200"

  logstash:
    image: docker.elastic.co/logstash/logstash:8.0.0
    depends_on:
      - elasticsearch

  kibana:
    image: docker.elastic.co/kibana/kibana:8.0.0
    ports:
      - "5601:5601"
```

### 10. Limitar recursos

```yaml
services:
  limesurvey:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 2G
        reservations:
          cpus: '0.5'
          memory: 1G

  limesurvey-db:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
```

## 🔄 Workflows Comunes

### Workflow: Testing contra v6 y v7

```bash
#!/bin/bash

echo "Testing against LimeSurvey 6..."
./limesurvey.sh version 6
./limesurvey.sh stop
./limesurvey.sh start
sleep 30
./limesurvey.sh test:all
RESULT_V6=$?

echo "Testing against LimeSurvey 7..."
./limesurvey.sh version 7
./limesurvey.sh stop
./limesurvey.sh start
sleep 30
./limesurvey.sh test:all
RESULT_V7=$?

./limesurvey.sh stop

if [ $RESULT_V6 -eq 0 ] && [ $RESULT_V7 -eq 0 ]; then
    echo "✓ All tests passed on both versions"
    exit 0
else
    echo "✗ Some tests failed"
    exit 1
fi
```

### Workflow: CI/CD Integration

```yaml
# .github/workflows/test.yml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up Java
        uses: actions/setup-java@v2
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Start LimeSurvey
        run: docker-compose up -d
      
      - name: Wait for services
        run: sleep 30
      
      - name: Run tests
        run: mvn verify
      
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v2
        with:
          name: test-results
          path: target/failsafe-reports/
      
      - name: Cleanup
        if: always()
        run: docker-compose down
```

## 🛠️ Troubleshooting Avanzado

### Ver volúmenes en uso
```bash
docker volume ls
docker volume inspect limesurvey-clientv2_limesurvey-db-data
```

### Ejecutar comandos dentro del contenedor
```bash
docker exec -it limesurvey-app bash
docker exec limesurvey-db mysql -uroot -prootpass -e "SHOW DATABASES;"
```

### Rebuild sin caché
```bash
docker-compose build --no-cache
```

### Inspeccionar red Docker
```bash
docker network ls
docker network inspect limesurvey-clientv2_default
```

## 📊 Performance Tuning

### Aumentar memoria de MySQL
```yaml
services:
  limesurvey-db:
    command: --max_connections=1000 --max_allowed_packet=512M
```

### Cachés y optimizaciones
```yaml
services:
  limesurvey:
    environment:
      - LIMESURVEY_DB_CACHE_TYPE=redis
      - LIMESURVEY_REDIS_HOST=redis
```

---

Para más información, ver: **DOCKER.md**
