#!/bin/bash
# LimeSurvey Docker Automation Script
# Manejo sencillo de Docker para desarrollo y testing

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

# Check Docker installation
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    if ! command -v docker-compose &> /dev/null; then
        print_error "docker-compose is not installed. Please install docker-compose first."
        exit 1
    fi
    print_success "Docker and docker-compose are installed"
}

# Start LimeSurvey
start() {
    print_status "Starting LimeSurvey stack..."
    docker-compose up -d
    
    print_status "Waiting for services to be healthy..."
    sleep 10
    
    # Check database
    if docker exec limesurvey-db mysqladmin ping -hlocalhost > /dev/null 2>&1; then
        print_success "Database is ready"
    else
        print_error "Database failed to start"
        exit 1
    fi
    
    # Check LimeSurvey
    if curl -sf http://localhost > /dev/null 2>&1; then
        print_success "LimeSurvey is running"
    else
        print_warning "LimeSurvey may still be initializing..."
    fi
    
    echo ""
    print_success "LimeSurvey is accessible at http://localhost"
    echo -e "  ${YELLOW}Admin User:${NC} admin"
    echo -e "  ${YELLOW}Admin Password:${NC} admin123"
}

# Stop LimeSurvey
stop() {
    print_status "Stopping LimeSurvey stack..."
    docker-compose down
    print_success "Stack stopped"
}

# Restart services
restart() {
    print_status "Restarting LimeSurvey stack..."
    docker-compose restart
    sleep 5
    print_success "Stack restarted"
}

# Clean everything (remove volumes)
clean() {
    print_warning "This will remove all containers and volumes (including data)!"
    read -p "Are you sure? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_status "Cleaning up..."
        docker-compose down -v
        print_success "Clean complete"
    else
        print_status "Cleanup cancelled"
    fi
}

# Run unit tests
run_tests() {
    print_status "Running unit tests..."
    mvn -DskipTests=false -Dtest=*Test test
    print_success "Unit tests completed"
}

# Run integration tests (needs LimeSurvey running)
run_integration_tests() {
    if ! curl -sf http://localhost > /dev/null 2>&1; then
        print_error "LimeSurvey is not running. Please run: ./limesurvey.sh start"
        exit 1
    fi
    
    print_status "Running integration tests..."
    export LIMESURVEY_TEST_URL=http://localhost
    export LIMESURVEY_TEST_USER=admin
    export LIMESURVEY_TEST_PASSWORD=admin123
    mvn verify
    print_success "Integration tests completed"
}

# Change LimeSurvey version
change_version() {
    local version=$1
    if [[ ! "$version" =~ ^[67]$ ]]; then
        print_error "Invalid version. Use '6' or '7'"
        exit 1
    fi
    
    print_status "Changing LimeSurvey version to $version..."
    sed -i "s/martialblog\/limesurvey:[0-9]*-apache/martialblog\/limesurvey:${version}-apache/" docker-compose.yml
    sed -i "s/LIMESURVEY_VERSION=.*/LIMESURVEY_VERSION=${version}-apache/" .env.example
    print_success "Version updated to $version"
    echo -e "${YELLOW}Note:${NC} Stop and start the stack to apply changes:"
    echo "  ./limesurvey.sh stop"
    echo "  ./limesurvey.sh start"
}

# Show logs
show_logs() {
    local service=$1
    if [ -z "$service" ]; then
        docker-compose logs -f
    else
        docker-compose logs -f "$service"
    fi
}

# Show status
show_status() {
    print_status "Docker Services Status:"
    docker-compose ps
    echo ""
    
    if curl -sf http://localhost > /dev/null 2>&1; then
        print_success "LimeSurvey is accessible at http://localhost"
    else
        print_warning "LimeSurvey is not responding"
    fi
}

# Help message
show_help() {
    cat << EOF
${BLUE}LimeSurvey Docker Management${NC}

Usage: ./limesurvey.sh <command>

Commands:
  ${GREEN}start${NC}              Start LimeSurvey stack
  ${GREEN}stop${NC}               Stop LimeSurvey stack
  ${GREEN}restart${NC}            Restart services
  ${GREEN}clean${NC}              Remove all containers and volumes
  ${GREEN}status${NC}             Show services status
  ${GREEN}logs${NC} [service]     Show logs (limesurvey-app, limesurvey-db, etc)
  
  ${GREEN}test${NC}               Run unit tests
  ${GREEN}test:integration${NC}   Run integration tests (requires running LimeSurvey)
  ${GREEN}test:all${NC}           Run all tests
  
  ${GREEN}version${NC} <6|7>      Change LimeSurvey version
  
  ${GREEN}help${NC}               Show this message

Examples:
  ./limesurvey.sh start
  ./limesurvey.sh logs limesurvey-app
  ./limesurvey.sh test
  ./limesurvey.sh version 7
  ./limesurvey.sh test:integration

EOF
}

# Main command handling
case "${1:-help}" in
    start)
        check_docker
        start
        ;;
    stop)
        check_docker
        stop
        ;;
    restart)
        check_docker
        restart
        ;;
    clean)
        check_docker
        clean
        ;;
    status)
        check_docker
        show_status
        ;;
    logs)
        check_docker
        show_logs "$2"
        ;;
    test)
        run_tests
        ;;
    test:integration)
        run_integration_tests
        ;;
    test:all)
        run_tests
        echo ""
        run_integration_tests
        ;;
    version)
        change_version "$2"
        ;;
    help)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
