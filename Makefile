# Makefile for the BankApp project

.PHONY: all build clean test run stop logs generate-clients help

# Default target
all: build

# Build the entire project and skip tests for a faster build if needed
build:
	gradlew build

# Clean the project
clean:
	gradlew clean

# Run all tests
test:
	gradlew test

# Start all services in detached mode using Docker Compose
run:
	docker-compose up -d

# Stop all services
stop:
	docker-compose down

# View logs of running services
logs:
	docker-compose logs -f

# Regenerate OpenAPI clients by compiling the 'common' module
generate-clients:
	gradlew :common:compileJava

# Display this help message
help:
	@echo "Makefile for the BankApp project"
	@echo ""
	@echo "Usage:"
	@echo "  make build           - Build the entire project"
	@echo "  make clean           - Clean all build artifacts"
	@echo "  make test            - Run all tests"
	@echo "  make run             - Start all services in detached mode"
	@echo "  make stop            - Stop all running services"
	@echo "  make logs            - Follow the logs of all services"
	@echo "  make generate-clients - Regenerate the OpenAPI client code in the 'common' module"
	@echo ""
