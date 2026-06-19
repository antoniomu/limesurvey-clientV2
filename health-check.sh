#!/bin/bash
# Health check - Verifica que Docker y dependencias estén disponibles

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "═══════════════════════════════════════════════════════════════"
echo "LimeSurvey Docker Setup - Health Check"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Check Docker
echo -n "Checking Docker... "
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | grep -oP '\d+\.\d+\.\d+' | head -1)
    echo -e "${GREEN}✓${NC} ($DOCKER_VERSION)"
else
    echo -e "${RED}✗ NOT FOUND${NC}"
    echo "  Install from: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check Docker Compose
echo -n "Checking Docker Compose... "
if command -v docker-compose &> /dev/null; then
    COMPOSE_VERSION=$(docker-compose --version | grep -oP '\d+\.\d+\.\d+' | head -1)
    echo -e "${GREEN}✓${NC} ($COMPOSE_VERSION)"
else
    echo -e "${RED}✗ NOT FOUND${NC}"
    echo "  Install from: https://docs.docker.com/compose/install/"
    exit 1
fi

# Check Maven (optional)
echo -n "Checking Maven... "
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn --version 2>/dev/null | grep "Apache Maven" | grep -oP '\d+\.\d+\.\d+')
    echo -e "${GREEN}✓${NC} ($MVN_VERSION)"
else
    echo -e "${YELLOW}○ OPTIONAL${NC} (needed for tests)"
    echo "  Install from: https://maven.apache.org/download.cgi"
fi

# Check Java (optional)
echo -n "Checking Java... "
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | grep version | grep -oP '\d+' | head -1)
    if [ "$JAVA_VERSION" -ge "21" ]; then
        echo -e "${GREEN}✓${NC} (Java $JAVA_VERSION)"
    else
        echo -e "${YELLOW}⚠${NC} (Java $JAVA_VERSION, need 21+)"
    fi
else
    echo -e "${YELLOW}○ OPTIONAL${NC} (needed for tests)"
    echo "  Install from: https://www.oracle.com/java/technologies/downloads/"
fi

echo ""
echo "Checking Docker files..."
echo ""

# Check required files
FILES=(
    "docker-compose.yml"
    "Dockerfile.test"
    "limesurvey.sh"
    "DOCKER.md"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        SIZE=$(du -h "$file" | cut -f1)
        echo -e "${GREEN}✓${NC} $file ($SIZE)"
    else
        echo -e "${RED}✗${NC} $file NOT FOUND"
    fi
done

echo ""
echo "Checking Docker daemon..."
echo -n "  "
if docker ps > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Docker daemon is running${NC}"
else
    echo -e "${RED}✗ Docker daemon is not running${NC}"
    echo "  Start Docker and try again"
    exit 1
fi

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo -e "${GREEN}✓ All checks passed!${NC}"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "Ready to start:"
echo "  ./limesurvey.sh start"
echo ""
echo "Then run tests:"
echo "  ./limesurvey.sh test:all"
echo ""
