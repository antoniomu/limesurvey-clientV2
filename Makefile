.PHONY: help docker-start docker-stop docker-restart docker-clean docker-status docker-logs \
        test test-unit test-integration test-all \
        version-6 version-7 \
        build run-example clean-all

LIMESURVEY_SHELL := $(if $(OS),limesurvey.bat,./limesurvey.sh)

help:
	@echo "═══════════════════════════════════════════════════════════════"
	@echo "LimeSurvey Client Testing - Makefile"
	@echo "═══════════════════════════════════════════════════════════════"
	@echo ""
	@echo "Docker Management:"
	@echo "  make docker-start           Start LimeSurvey stack"
	@echo "  make docker-stop            Stop LimeSurvey stack"
	@echo "  make docker-restart         Restart services"
	@echo "  make docker-clean           Remove containers and volumes"
	@echo "  make docker-status          Show services status"
	@echo "  make docker-logs            Show logs (tail -f)"
	@echo ""
	@echo "Testing:"
	@echo "  make test                   Run unit tests"
	@echo "  make test-integration       Run integration tests"
	@echo "  make test-all               Run all tests (requires running docker)"
	@echo ""
	@echo "LimeSurvey Version:"
	@echo "  make version-6              Change to LimeSurvey v6"
	@echo "  make version-7              Change to LimeSurvey v7"
	@echo ""
	@echo "Build & Run:"
	@echo "  make build                  Build JAR without tests"
	@echo "  make run-example            Run example JAR"
	@echo ""
	@echo "Cleaning:"
	@echo "  make clean-all              Full cleanup (maven + docker)"
	@echo ""
	@echo "═══════════════════════════════════════════════════════════════"

# Docker targets
docker-start:
	@echo "Starting LimeSurvey stack..."
	@$(LIMESURVEY_SHELL) start

docker-stop:
	@echo "Stopping LimeSurvey stack..."
	@$(LIMESURVEY_SHELL) stop

docker-restart:
	@echo "Restarting LimeSurvey stack..."
	@$(LIMESURVEY_SHELL) restart

docker-clean:
	@echo "Cleaning Docker resources..."
	@$(LIMESURVEY_SHELL) clean

docker-status:
	@$(LIMESURVEY_SHELL) status

docker-logs:
	@$(LIMESURVEY_SHELL) logs

# Testing targets
test: test-unit

test-unit:
	@echo "Running unit tests..."
	mvn -DskipTests=false -Dtest=*Test test

test-integration:
	@echo "Running integration tests..."
	mvn verify

test-all: docker-start
	@sleep 15
	@echo "Running all tests..."
	mvn verify
	@$(LIMESURVEY_SHELL) stop

# Version switching
version-6:
	@$(LIMESURVEY_SHELL) version 6
	@echo "Run: make docker-restart (to apply changes)"

version-7:
	@$(LIMESURVEY_SHELL) version 7
	@echo "Run: make docker-restart (to apply changes)"

# Build targets
build:
	mvn -DskipTests package

run-example: build
	java -jar target/limesurvey-clientV2-0.1.0-shaded.jar

# Cleaning targets
clean-all: docker-clean
	mvn clean
	@echo "Full cleanup complete"

# Quick start workflow
quick-test: docker-start test-all

# Development workflow (docker stays running)
dev: docker-start
	@echo "Development mode - Docker running"
	@echo "Run 'make test' in another terminal"
	@echo "Press Ctrl+C when done"
	@$(LIMESURVEY_SHELL) logs limesurvey-app
