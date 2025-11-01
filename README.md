# Shipment Tracker System

Event-driven microservices system for real-time shipment tracking and automated customer notifications. Implements Domain-Driven Design (DDD) with Hexagonal Architecture and Event-Driven Architecture (EDA) using Apache Kafka.

## Table of Contents

- [Overview](#overview)
  - [Features](#features)
  - [Technology Stack](#technology-stack)
  - [System Architecture](#system-architecture)
- [Getting Started](#getting-started)
  - [System Requirements](#system-requirements)
  - [Installation](#installation)
  - [Running the Application](#running-the-application)
  - [Running Kafka](#running-kafka)
  - [Stopping the Application](#stopping-the-application)
- [API Documentation](#api-documentation)
  - [Swagger UI](#swagger-ui)
  - [Endpoint Overview](#endpoint-overview)
  - [Example Requests](#example-requests)
- [Testing](#testing)
  - [Running Unit Tests](#running-unit-tests)
  - [Running Integration Tests](#running-integration-tests)
  - [Test Coverage](#test-coverage)
  - [Test Structure](#test-structure)

---

## Overview

The **Shipment Tracker System** is a distributed microservices application that tracks shipment status changes in real-time and automatically notifies customers of delivery updates. The system integrates with external shipping APIs, processes status changes through an event-driven architecture using Apache Kafka, and maintains a complete audit trail of all shipment events.

This implementation demonstrates enterprise-grade software architecture patterns including:
- **Domain-Driven Design (DDD)** with tactical patterns
- **Hexagonal Architecture** (Ports & Adapters)
- **Event-Driven Architecture (EDA)** with Kafka
- **CQRS principles** for read/write separation
- **Test-Driven Development (TDD)** with comprehensive test coverage

### Features

#### TrackingService (Command Side)
- ✅ **Create shipment tracking** with unique tracking IDs
- ✅ **Fetch real-time status** from external shipping APIs
- ✅ **Update tracking information** with status transitions
- ✅ **Publish domain events** to Kafka when status changes
- ✅ **Business rule enforcement** (no invalid status transitions)
- ✅ **External API integration** with retry logic and circuit breaker patterns
- ✅ **RESTful API** with OpenAPI/Swagger documentation

#### NotificationService (Event Consumer)
- ✅ **Consume status change events** from Kafka topics
- ✅ **Generate customer notifications** based on status updates
- ✅ **Multi-channel support** (EMAIL, SMS, PUSH, IN_APP)
- ✅ **Automatic retry logic** for failed notifications (up to 3 attempts)
- ✅ **Notification history tracking** with audit timestamps
- ✅ **Manual acknowledgment** for reliable message processing

#### Monitoring & Observability
- ✅ **Spring Boot Actuator** for health checks and metrics
- ✅ **Prometheus metrics** export for monitoring
- ✅ **H2 Console** for database inspection during development
- ✅ **Comprehensive logging** with structured output

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming language with modern features (records, pattern matching) |
| **Spring Boot** | 3.5.7 | Application framework and dependency injection |
| **Spring Data JPA** | 3.5.7 | Data access layer with repository pattern |
| **Hibernate** | 6.x | ORM implementation for persistence |
| **H2 Database** | 2.x | In-memory database (production: PostgreSQL/MySQL) |
| **Apache Kafka** | 3.x | Distributed event streaming platform |
| **Spring Kafka** | 3.x | Kafka integration for Spring Boot |
| **Spring WebFlux** | 3.5.7 | Reactive WebClient for external API calls |
| **SpringDoc OpenAPI** | 2.8.4 | API documentation (Swagger UI) |
| **Lombok** | 1.18.x | Boilerplate code reduction |
| **Maven** | 3.9+ | Build and dependency management |
| **JUnit 5** | 5.10+ | Testing framework |
| **Mockito** | 5.x | Mocking framework for unit tests |
| **MockWebServer** | 4.12.0 | HTTP server mocking for WebClient tests |

**Development Tools:**
- **IntelliJ IDEA** - Primary IDE
- **Git** - Version control
- **Docker** - Kafka container orchestration (optional)
- **Postman** - API testing (recommended)

### System Architecture

The system consists of two microservices communicating via Kafka events:

```
┌─────────────────────────────────────────────────────────────────┐
│                     External Shipping APIs                       │
│                  (DHL, FedEx, UPS, Correos, etc.)               │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP/REST
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      TrackingService                             │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  REST API (TrackingController)                             │ │
│  │  • POST   /api/trackings                                   │ │
│  │  • GET    /api/trackings/{trackingId}                      │ │
│  │  • PUT    /api/trackings/{trackingId}                      │ │
│  │  • DELETE /api/trackings/{trackingId}                      │ │
│  │  • GET    /api/trackings                                   │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │  Application Layer (TrackingApplicationService)            │ │
│  │  • Orchestrates use cases                                  │ │
│  │  • Coordinates domain and infrastructure                   │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │  Domain Layer (DDD Tactical Patterns)                      │ │
│  │  • Tracking (Aggregate Root)                               │ │
│  │  • TrackingId (Value Object)                               │ │
│  │  • ShipmentStatus (Enum)                                   │ │
│  │  • StatusChange (Domain Event)                             │ │
│  │  • Business rules & invariants                             │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │  Infrastructure Layer                                       │ │
│  │  • TrackingRepository (JPA)                                │ │
│  │  • ExternalShipmentApiAdapter (WebClient)                  │ │
│  │  • KafkaEventPublisher                                     │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
└─────────────────────────┼─────────────────────────────────────┘
                          │ Kafka Event
                          │ (ShipmentStatusChangedEvent)
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Apache Kafka Message Broker                    │
│         Topic: shipment-status-changes                           │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Event Stream
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    NotificationService                           │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  Kafka Consumer (ShipmentStatusEventListener)              │ │
│  │  • Consumes ShipmentStatusChangedEvent                     │ │
│  │  • Manual acknowledgment for reliability                   │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │  Application Layer (NotificationService)                   │ │
│  │  • Process status change events                            │ │
│  │  • Create and send notifications                           │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │  Domain Layer                                               │ │
│  │  • Notification (Aggregate Root)                           │ │
│  │  • NotificationType (EMAIL, SMS, PUSH, IN_APP)             │ │
│  │  • NotificationStatus (PENDING, SENT, FAILED, RETRYING)    │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                         │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │  Infrastructure Layer                                       │ │
│  │  • NotificationRepository (JPA)                            │ │
│  │  • LoggingNotificationSender (Output Port)                 │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

**Event Flow:**
1. Client creates a shipment tracking via REST API
2. TrackingService fetches status from external API
3. When status changes, domain event is published to Kafka
4. NotificationService consumes the event
5. Notification is generated and sent to customer
6. Notification status is persisted with audit trail

---

## Getting Started

### System Requirements

Before running the application, ensure you have the following installed:

| Requirement | Minimum Version | Recommended Version |
|-------------|----------------|---------------------|
| **JDK** | 21 | 21 (LTS) |
| **Maven** | 3.9.0 | 3.9.5+ |
| **Apache Kafka** | 2.8.0 | 3.x |
| **Memory** | 1 GB RAM | 2 GB RAM |

**Verify Installation:**

```bash
# Check Java version
java -version
# Expected: openjdk version "21" or higher

# Check Maven version
mvn -version
# Expected: Apache Maven 3.9.0 or higher
```

### Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd shipment-tracker-system
   ```

2. **Build the project:**
   ```bash
   mvn clean install
   ```

   This will:
   - Download all dependencies
   - Compile the source code
   - Run all tests (75+ unit tests)
   - Package the application as a JAR file

### Running the Application

#### Step 1: Start Kafka

The application requires Apache Kafka to be running for event-driven communication between services.

**Option A: Using Docker (Recommended)**

```bash
# Create docker-compose.yml with Kafka and Zookeeper
docker-compose up -d

# Verify Kafka is running
docker ps
```

**Option B: Local Kafka Installation**

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka (in a new terminal)
bin/kafka-server-start.sh config/server.properties

# Create the required topic
bin/kafka-topics.sh --create \
  --topic shipment-status-changes \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

#### Step 2: Start the Application

**Option 1: Using Maven Spring Boot Plugin (Recommended for Development)**

```bash
mvn spring-boot:run
```

**Option 2: Using the JAR file (Production-like)**

```bash
# Build the JAR (if not already built)
mvn clean package -DskipTests

# Run the JAR
java -jar target/shipment-tracker-system-0.0.1-SNAPSHOT.jar
```

**Expected Console Output:**

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.5.7)

INFO ... : Starting ShipmentTrackerSystemApplication
INFO ... : The following 1 profile is active: "default"
INFO ... : Started ShipmentTrackerSystemApplication in 4.523 seconds
INFO ... : Kafka consumer started for topic: shipment-status-changes
```

The application will start on **http://localhost:8080/api**

### Running Kafka

If you need to monitor Kafka messages or troubleshoot event flow:

**List Topics:**
```bash
kafka-topics.sh --list --bootstrap-server localhost:9092
```

**Consume Messages (Monitor Events):**
```bash
kafka-console-consumer.sh \
  --topic shipment-status-changes \
  --from-beginning \
  --bootstrap-server localhost:9092
```

**Produce Test Message:**
```bash
kafka-console-producer.sh \
  --topic shipment-status-changes \
  --bootstrap-server localhost:9092
```

### Stopping the Application

Press `Ctrl + C` in the terminal to stop the application.

To stop Kafka:
```bash
# If using Docker
docker-compose down

# If using local installation
# Stop Kafka server (Ctrl + C)
# Stop Zookeeper (Ctrl + C)
```

---

## API Documentation

### Swagger UI

The API is fully documented using **OpenAPI 3.0** specification with interactive Swagger UI.

**Access Swagger UI:**
```
http://localhost:8080/api/swagger-ui.html
```

**OpenAPI JSON Specification:**
```
http://localhost:8080/api/api-docs
```

From Swagger UI, you can:
- View all available endpoints
- See request/response schemas
- Test endpoints interactively
- Download the OpenAPI specification
- View actuator endpoints

### Endpoint Overview

#### TrackingService REST API

Base path: `/api/trackings`

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| **POST** | `/api/trackings` | Create new shipment tracking | TrackingRequest | TrackingResponse (201) |
| **GET** | `/api/trackings/{trackingId}` | Get tracking by ID | - | TrackingResponse (200) |
| **PUT** | `/api/trackings/{trackingId}` | Update tracking status | TrackingRequest | TrackingResponse (200) |
| **DELETE** | `/api/trackings/{trackingId}` | Delete tracking | - | 204 No Content |
| **GET** | `/api/trackings` | List all trackings | - | List<TrackingResponse> (200) |

**Request Body Example (TrackingRequest):**

```json
{
  "trackingId": "TRK123456789",
  "currentStatus": "IN_TRANSIT",
  "lastLocation": "Distribution Center - Madrid",
  "carrier": "DHL Express",
  "estimatedDelivery": "2025-11-05T14:00:00"
}
```

**Response Example (TrackingResponse):**

```json
{
  "trackingId": "TRK123456789",
  "currentStatus": "IN_TRANSIT",
  "previousStatus": "PENDING",
  "lastLocation": "Distribution Center - Madrid",
  "carrier": "DHL Express",
  "estimatedDelivery": "2025-11-05T14:00:00",
  "createdAt": "2025-11-01T10:30:00",
  "updatedAt": "2025-11-01T12:45:00",
  "lastCheckedAt": "2025-11-01T12:45:00"
}
```

**Response Codes:**

| Code | Description |
|------|-------------|
| 200 OK | Request successful |
| 201 Created | Resource created successfully |
| 204 No Content | Deletion successful |
| 400 Bad Request | Invalid request (validation error) |
| 404 Not Found | Tracking not found |
| 409 Conflict | Tracking ID already exists |
| 500 Internal Server Error | Unexpected server error |

**Error Response Example (400 Bad Request):**

```json
{
  "timestamp": "2025-11-01T10:30:45",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: trackingId must not be blank",
  "path": "/api/trackings"
}
```

**Shipment Status Values:**

| Status | Description |
|--------|-------------|
| `PENDING` | Shipment created, awaiting pickup |
| `IN_TRANSIT` | Package in transit to destination |
| `OUT_FOR_DELIVERY` | Out for delivery to customer |
| `DELIVERED` | Successfully delivered (final state) |
| `FAILED_DELIVERY` | Delivery attempt failed |
| `RETURNED` | Package returned to sender |
| `CANCELLED` | Shipment cancelled (final state) |

**Business Rules:**
- Status transitions must be valid (e.g., cannot go from DELIVERED back to PENDING)
- DELIVERED and CANCELLED are final states
- TrackingId must be unique and max 50 characters
- TrackingId must match pattern: `^[A-Z0-9]+$` (alphanumeric uppercase)

### Example Requests

**Using cURL:**

```bash
# Create a new tracking
curl -X POST http://localhost:8080/api/trackings \
  -H "Content-Type: application/json" \
  -d '{
    "trackingId": "TRK123456789",
    "currentStatus": "PENDING",
    "lastLocation": "Origin Warehouse",
    "carrier": "DHL Express",
    "estimatedDelivery": "2025-11-05T14:00:00"
  }'

# Get tracking by ID
curl http://localhost:8080/api/trackings/TRK123456789

# Update tracking status
curl -X PUT http://localhost:8080/api/trackings/TRK123456789 \
  -H "Content-Type: application/json" \
  -d '{
    "trackingId": "TRK123456789",
    "currentStatus": "IN_TRANSIT",
    "lastLocation": "Distribution Center - Madrid",
    "carrier": "DHL Express",
    "estimatedDelivery": "2025-11-05T14:00:00"
  }'

# Get all trackings
curl http://localhost:8080/api/trackings

# Delete tracking
curl -X DELETE http://localhost:8080/api/trackings/TRK123456789
```

**Using Browser:**

Simply paste the URLs in your browser for GET requests:
```
http://localhost:8080/api/trackings
http://localhost:8080/api/trackings/TRK001234567890
```

---

## Testing

### Running Unit Tests

Unit tests verify individual components in isolation using mocks.

```bash
# Run all unit tests
mvn test

# Run tests for a specific service
mvn test -Dtest="*TrackingService*"

# Run tests for domain layer only
mvn test -Dtest="com.mpowerplus.shipmenttrackersystem.*.domain.**"

# Run a specific test class
mvn test -Dtest=TrackingTest
```

### Running Integration Tests

Integration tests verify the complete flow from HTTP request to database and Kafka.

```bash
# Run all tests (unit + integration)
mvn verify

# Run only integration tests
mvn test -Dtest="**/*IntegrationTest"
```

**Note:** Integration tests are currently limited due to external API mocking complexities. The test suite focuses primarily on comprehensive unit testing with 75+ tests covering all layers.

### Test Coverage

**Current Test Coverage:**

| Layer | Test Count | Coverage |
|-------|-----------|----------|
| **Domain Layer** | 43 tests | ~95% |
| **Application Layer** | 11 tests | ~90% |
| **Infrastructure Layer** | 21 tests | ~85% |
| **Total** | **75+ tests** | **~90%** |

**Test Categories:**
- ✅ **Value Objects**: TrackingId validation, equality, immutability
- ✅ **Entities**: Tracking aggregate behavior, status transitions
- ✅ **Domain Events**: StatusChange, ShipmentStatusChangedEvent
- ✅ **Domain Enums**: ShipmentStatus transitions and validations
- ✅ **Application Services**: Use case orchestration with mocked dependencies
- ✅ **Repository Adapters**: JPA mapping and data access
- ✅ **Mappers**: Bidirectional entity-domain conversions
- ✅ **External API Adapters**: WebClient with MockWebServer
- ✅ **Kafka Publishers**: Event publishing with Spring Kafka Test

### Test Structure

**TrackingService Tests:**

```
src/test/java/com/mpowerplus/shipmenttrackersystem/
├── trackingservice/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── TrackingTest.java              (10 tests)
│   │   │   ├── TrackingIdTest.java            (8 tests)
│   │   │   ├── StatusChangeTest.java          (6 tests)
│   │   │   └── ShipmentStatusTest.java        (12 tests)
│   │   └── repository/
│   │       └── TrackingRepositoryTest.java    (7 tests)
│   ├── application/
│   │   └── service/
│   │       └── TrackingApplicationServiceTest.java  (11 tests)
│   └── infrastructure/
│       ├── adapter/
│       │   ├── output/
│       │   │   ├── persistence/
│       │   │   │   ├── TrackingJpaRepositoryTest.java       (5 tests)
│       │   │   │   ├── TrackingRepositoryAdapterTest.java   (8 tests)
│       │   │   │   └── TrackingMapperTest.java              (8 tests)
│       │   │   ├── external/
│       │   │   │   └── ExternalShipmentApiAdapterTest.java  (4 tests)
│       │   │   └── messaging/
│       │   │       └── KafkaEventPublisherTest.java         (3 tests)
│       │   └── input/
│       │       └── rest/
│       │           └── TrackingControllerTest.java          (5 tests)
│       └── ...
```

**NotificationService Tests:**

```
├── notificationservice/
│   ├── domain/
│   │   └── model/
│   │       └── NotificationTest.java          (8 tests)
│   └── application/
│       └── service/
│           └── NotificationServiceTest.java   (5 tests)
```

**Shared Kernel Tests:**

```
├── shared/
│   └── domain/
│       └── event/
│           └── ShipmentStatusChangedEventTest.java  (4 tests)
```

**Example Test Cases:**

```java
// Domain Test - Business Rules
@Test
void updateStatus_FromDeliveredToInTransit_ThrowsException() {
    Tracking tracking = createTracking(ShipmentStatus.DELIVERED);

    assertThrows(IllegalStateException.class,
        () -> tracking.updateStatus(ShipmentStatus.IN_TRANSIT, "Madrid", "DHL"));
}

// Application Test - Use Case Orchestration
@Test
void createTracking_ValidRequest_PublishesEvent() {
    when(externalApi.fetchShipmentStatus(any()))
        .thenReturn(new ExternalShipmentData(...));

    service.createTracking(request);

    verify(eventPublisher).publish(any(ShipmentStatusChangedEvent.class));
}

// Infrastructure Test - WebClient Integration
@Test
void fetchShipmentStatus_SuccessfulResponse_ReturnsData() {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setBody("{\"status\":\"IN_TRANSIT\"}"));

    ExternalShipmentData result = adapter.fetchShipmentStatus("TRK123");

    assertThat(result.getStatus()).isEqualTo("IN_TRANSIT");
}
```

---

## Database Access

### H2 Console

The application uses an **H2 in-memory database** with a web console for inspection during development.

**Access H2 Console:**
```
http://localhost:8080/api/h2-console
```

**Login Credentials:**

| Field | Value |
|-------|-------|
| **JDBC URL** | `jdbc:h2:mem:shipment-tracker-db` |
| **Username** | `admin` |
| **Password** | *(leave empty)* |
| **Driver Class** | `org.h2.Driver` |

**Note:** These credentials are defined in `src/main/resources/application.yml`

### Test Data

The application automatically loads test data on startup through SQL scripts:

1. **Schema Creation** (`src/main/resources/schema.sql`)
   - Creates the `trackings` table with all necessary columns, constraints, and indexes
   - Creates the `notifications` table for notification history
   - Defines CHECK constraints for status values
   - Adds indexes for performance optimization

2. **Data Loading** (`src/main/resources/data.sql`)
   - Loads 6 sample shipment tracking records with realistic data
   - Includes various shipment statuses for testing different scenarios

**Configuration:**

This is achieved through Spring Boot's SQL initialization feature configured in `application.yml`:

```yaml
spring:
  sql:
    init:
      mode: always                          # Always execute scripts on startup
      schema-locations: classpath:schema.sql # DDL script
      data-locations: classpath:data.sql     # DML script
  jpa:
    defer-datasource-initialization: true    # Execute scripts before Hibernate validation
```

**Loaded Test Data:**

| Tracking ID | Status | Location | Carrier | Estimated Delivery |
|-------------|--------|----------|---------|-------------------|
| TRK001234567890 | IN_TRANSIT | Distribution Center - Madrid | DHL Express | 2025-10-29 14:00:00 |
| TRK987654321000 | OUT_FOR_DELIVERY | Local Hub - Barcelona | Correos | 2025-10-27 18:00:00 |
| TRK555666777888 | DELIVERED | Customer Address - Valencia | SEUR | 2025-10-26 16:00:00 |
| TRK111222333444 | PENDING | Origin Warehouse - Sevilla | MRW | 2025-10-30 12:00:00 |
| TRK999888777666 | FAILED_DELIVERY | Customer Address - Bilbao | UPS | 2025-10-28 14:00:00 |
| TRK444555666777 | IN_TRANSIT | International Hub - Frankfurt | FedEx | 2025-11-02 10:00:00 |

**Query Test Data:**

You can query this data directly in the H2 console:

```sql
-- View all trackings
SELECT * FROM trackings ORDER BY created_at DESC;

-- View trackings by status
SELECT * FROM trackings WHERE current_status = 'IN_TRANSIT';

-- View all notifications
SELECT * FROM notifications ORDER BY created_at DESC;

-- View notifications for a specific tracking
SELECT * FROM notifications WHERE tracking_id = 'TRK001234567890';
```

**Database Schema:**

**trackings table:**
- `id` (BIGSERIAL) - Primary key
- `tracking_id` (VARCHAR(50)) - Unique tracking identifier
- `current_status` (VARCHAR(30)) - Current shipment status
- `previous_status` (VARCHAR(30)) - Previous status for audit
- `last_location` (VARCHAR(200)) - Last known location
- `carrier` (VARCHAR(100)) - Shipping carrier name
- `estimated_delivery` (TIMESTAMP) - Estimated delivery date/time
- `created_at` (TIMESTAMP) - Record creation timestamp
- `updated_at` (TIMESTAMP) - Last update timestamp
- `last_checked_at` (TIMESTAMP) - Last external API check timestamp

**notifications table:**
- `id` (BIGSERIAL) - Primary key
- `tracking_id` (VARCHAR(50)) - Reference to shipment
- `type` (VARCHAR(20)) - Notification channel (EMAIL, SMS, PUSH, IN_APP)
- `status` (VARCHAR(20)) - Notification status (PENDING, SENT, FAILED, RETRYING)
- `recipient` (VARCHAR(255)) - Recipient address
- `subject` (VARCHAR(500)) - Notification subject
- `message` (TEXT) - Notification message body
- `created_at` (TIMESTAMP) - Creation timestamp
- `sent_at` (TIMESTAMP) - Sent timestamp
- `retry_count` (INTEGER) - Number of retry attempts
- `error_message` (TEXT) - Error details if failed

---

## Monitoring and Observability

### Spring Boot Actuator

The application includes **Spring Boot Actuator** for production-ready monitoring and management endpoints.

**Base URL:**
```
http://localhost:8080/api/actuator
```

### Available Endpoints

#### Core Endpoints

| Endpoint | Description | Example |
|----------|-------------|---------|
| `/actuator` | List all available endpoints | Shows discovery document |
| `/actuator/health` | Application health status | Database connectivity, disk space, Kafka |
| `/actuator/info` | Application information | Version, description, metadata |

**Health Check Response:**

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760,
        "path": "C:\\dev-projects\\shipment-tracker-system",
        "exists": true
      }
    },
    "kafka": {
      "status": "UP",
      "details": {
        "clusterId": "kafka-cluster-1"
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

#### Metrics and Monitoring

| Endpoint | Description |
|----------|-------------|
| `/actuator/metrics` | List of available metrics (JVM, HTTP, Kafka, custom) |
| `/actuator/metrics/jvm.memory.used` | Current JVM memory usage |
| `/actuator/metrics/jvm.memory.max` | Maximum JVM memory available |
| `/actuator/metrics/http.server.requests` | HTTP request statistics (count, duration) |
| `/actuator/metrics/system.cpu.usage` | CPU usage percentage |
| `/actuator/metrics/kafka.producer.request.total` | Total Kafka producer requests |
| `/actuator/metrics/kafka.consumer.fetch.total` | Total Kafka consumer fetches |

**Metrics Example:**

```bash
# View JVM memory usage
curl http://localhost:8080/api/actuator/metrics/jvm.memory.used

# Response:
{
  "name": "jvm.memory.used",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 234567890
    }
  ],
  "availableTags": [
    {
      "tag": "area",
      "values": ["heap", "nonheap"]
    },
    {
      "tag": "id",
      "values": ["G1 Eden Space", "G1 Old Gen", "G1 Survivor Space"]
    }
  ]
}
```

#### Application Introspection

| Endpoint | Description |
|----------|-------------|
| `/actuator/beans` | List of all Spring beans in the application context |
| `/actuator/mappings` | All REST endpoint mappings (controllers) |
| `/actuator/env` | Environment properties and configuration |
| `/actuator/configprops` | All `@ConfigurationProperties` beans |
| `/actuator/loggers` | Logger configuration (can be changed at runtime) |
| `/actuator/conditions` | Auto-configuration report |

**View All Endpoints:**

```bash
# List all REST endpoints
curl http://localhost:8080/api/actuator/mappings

# View application configuration
curl http://localhost:8080/api/actuator/env

# View logger levels
curl http://localhost:8080/api/actuator/loggers
```

**Change Logger Level at Runtime:**

```bash
# Change logger level to DEBUG for tracking service
curl -X POST http://localhost:8080/api/actuator/loggers/com.mpowerplus.shipmenttrackersystem.trackingservice \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

#### Diagnostics

| Endpoint | Description | Content-Type |
|----------|-------------|--------------|
| `/actuator/threaddump` | JVM thread dump for debugging | `text/plain` |
| `/actuator/heapdump` | JVM heap dump (downloads .hprof file) | `application/octet-stream` |

#### Prometheus Metrics

The application exposes **Prometheus-compatible metrics** for monitoring and alerting.

**Prometheus Endpoint:**
```
http://localhost:8080/api/actuator/prometheus
```

**Sample Metrics:**

```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space",} 2.34567890E8

# HELP http_server_requests_seconds Duration of HTTP server requests
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="GET",uri="/api/trackings",status="200",} 42
http_server_requests_seconds_sum{method="GET",uri="/api/trackings",status="200",} 1.234567

# HELP kafka_producer_request_total Total number of producer requests
# TYPE kafka_producer_request_total counter
kafka_producer_request_total{client_id="producer-1",} 156
```

**Configuration:**

All actuator endpoints are exposed via configuration in `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"              # Expose all endpoints
  endpoint:
    health:
      show-details: always        # Show detailed health information
  prometheus:
    metrics:
      export:
        enabled: true             # Enable Prometheus metrics
```

**Security Note:** In production, consider securing actuator endpoints with Spring Security and exposing only necessary endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus  # Only expose specific endpoints
```

---

## Development Tools

### Recommended Tools

**API Testing:**
- **Postman** - Feature-rich API testing tool
- **cURL** - Command-line HTTP client
- **Swagger UI** - Built-in interactive API documentation
- **HTTPie** - Modern command-line HTTP client

**Database Inspection:**
- **H2 Console** - Built-in web console (http://localhost:8080/api/h2-console)
- **IntelliJ Database Tools** - Built-in database client
- **DBeaver** - Universal database tool

**Kafka Monitoring:**
- **Kafka Console Tools** - Command-line tools included with Kafka
- **Kafdrop** - Web UI for viewing Kafka topics and browsing consumer groups
- **Confluent Control Center** - Enterprise Kafka monitoring (if using Confluent)

**Code Quality:**
- **SonarLint** - IDE plugin for code quality analysis
- **SpotBugs** - Static analysis for Java bytecode
- **Checkstyle** - Code style checker

**Observability:**
- **Prometheus** - Metrics collection and monitoring
- **Grafana** - Metrics visualization and dashboards
- **Spring Boot Admin** - Web UI for managing Spring Boot applications

### Docker Compose for Development

For a complete development environment with Kafka and monitoring tools, create a `docker-compose.yml`:

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  kafdrop:
    image: obsidiandynamics/kafdrop:latest
    depends_on:
      - kafka
    ports:
      - "9000:9000"
    environment:
      KAFKA_BROKERCONNECT: kafka:9092
```

**Start the environment:**

```bash
docker-compose up -d
```

**Access services:**
- **Kafka**: localhost:9092
- **Kafdrop**: http://localhost:9000

---

## Architecture

### Domain-Driven Design (DDD)

This project implements **Domain-Driven Design (DDD)** tactical patterns to achieve a clean, maintainable, and testable architecture. The codebase is organized into distinct layers, each with specific responsibilities, following the principles of **Hexagonal Architecture** (Ports & Adapters).

**Key DDD Concepts Applied:**
- **Value Objects** - Immutable, self-validating domain primitives
- **Entities** - Objects with identity and lifecycle
- **Aggregates** - Cluster of entities and value objects with a root
- **Aggregate Root** - Entry point for all operations on the aggregate
- **Repositories** - Abstraction for data access (collection-like interface)
- **Domain Events** - Events that represent something that happened in the domain
- **Domain Services** - Business logic that doesn't naturally fit in entities
- **Application Services** - Orchestration of domain logic (use cases)
- **Shared Kernel** - Code shared between bounded contexts (ShipmentStatusChangedEvent)

**DDD Strategic Patterns:**
- **Bounded Contexts** - TrackingService and NotificationService are separate bounded contexts
- **Context Mapping** - Services communicate via domain events (Publisher-Subscriber pattern)
- **Ubiquitous Language** - Domain terminology consistently used throughout the codebase

### Project Structure

```
src/main/java/com/mpowerplus/shipmenttrackersystem/
├── trackingservice/                    # TrackingService Bounded Context
│   ├── domain/                         # Domain Layer (Core Business Logic)
│   │   ├── model/                      # Entities and Aggregates
│   │   │   ├── Tracking.java          # Aggregate root
│   │   │   ├── TrackingId.java        # Value object (identifier)
│   │   │   ├── ShipmentStatus.java    # Enum (domain concept)
│   │   │   └── StatusChange.java      # Domain event
│   │   └── repository/                # Repository interfaces (domain contracts)
│   │       └── TrackingRepository.java
│   │
│   ├── application/                   # Application Layer (Use Cases)
│   │   ├── dto/                       # Data Transfer Objects
│   │   │   ├── TrackingRequest.java  # Java 21 record
│   │   │   ├── TrackingResponse.java # Java 21 record
│   │   │   └── ExternalShipmentData.java
│   │   ├── port/                      # Ports (interfaces)
│   │   │   ├── input/
│   │   │   │   └── TrackingUseCase.java
│   │   │   └── output/
│   │   │       ├── ExternalShipmentApiPort.java
│   │   │       ├── EventPublisherPort.java
│   │   │       └── ExternalApiException.java
│   │   └── service/                   # Application services
│   │       ├── TrackingApplicationService.java
│   │       ├── TrackingNotFoundException.java
│   │       └── TrackingAlreadyExistsException.java
│   │
│   └── infrastructure/                # Infrastructure Layer (Adapters)
│       ├── adapter/
│       │   ├── input/                 # Input adapters (primary/driving)
│       │   │   └── rest/
│       │   │       └── TrackingController.java
│       │   └── output/                # Output adapters (secondary/driven)
│       │       ├── persistence/
│       │       │   ├── TrackingEntity.java
│       │       │   ├── TrackingJpaRepository.java
│       │       │   ├── TrackingRepositoryAdapter.java
│       │       │   └── TrackingMapper.java
│       │       ├── external/
│       │       │   └── ExternalShipmentApiAdapter.java
│       │       └── messaging/
│       │           └── KafkaEventPublisher.java
│       └── config/
│           ├── WebClientConfig.java
│           └── KafkaProducerConfig.java
│
├── notificationservice/               # NotificationService Bounded Context
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Notification.java     # Aggregate root
│   │   │   ├── NotificationType.java # Enum
│   │   │   └── NotificationStatus.java
│   │   └── repository/
│   │       └── NotificationRepository.java
│   │
│   ├── application/
│   │   ├── port/
│   │   │   └── output/
│   │   │       ├── NotificationSender.java
│   │   │       └── NotificationSendException.java
│   │   └── service/
│   │       └── NotificationService.java
│   │
│   └── infrastructure/
│       ├── adapter/
│       │   ├── input/
│       │   │   └── messaging/
│       │   │       └── ShipmentStatusEventListener.java
│       │   └── output/
│       │       ├── persistence/
│       │       │   ├── NotificationEntity.java
│       │       │   ├── NotificationJpaRepository.java
│       │       │   ├── NotificationRepositoryAdapter.java
│       │       │   └── NotificationMapper.java
│       │       └── notification/
│       │           └── LoggingNotificationSender.java
│       └── config/
│           └── KafkaConsumerConfig.java
│
└── shared/                            # Shared Kernel
    └── domain/
        └── event/
            └── ShipmentStatusChangedEvent.java
```

### Layer Responsibilities

#### Domain Layer

The **heart of the application** containing business rules and domain logic. This layer is:
- **Framework-agnostic** - No Spring, JPA, or infrastructure annotations
- **Fully testable** - Pure Java with business logic only
- **Isolated** - No dependencies on other layers
- **Technology-independent** - Can be used with any framework

**Key Components:**

**1. Value Objects (TrackingId)**
- Immutable objects that represent domain concepts
- Self-validating with business rules enforcement
- Equality based on value, not identity

```java
public class TrackingId {
    private static final int MAX_LENGTH = 50;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Z0-9]+$");

    private final String value;

    private TrackingId(String value) {
        validate(value);
        this.value = value;
    }

    public static TrackingId of(String value) {
        return new TrackingId(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TrackingId cannot be null or blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "TrackingId cannot exceed " + MAX_LENGTH + " characters");
        }
        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "TrackingId must contain only uppercase alphanumeric characters");
        }
    }
}
```

**2. Aggregate Root (Tracking)**
- Entry point for all operations on the aggregate
- Enforces invariants and business rules
- Generates domain events
- Maintains consistency boundaries

**Business Rules Implemented:**
- ✅ DELIVERED and CANCELLED are final states (no further transitions allowed)
- ✅ Cannot transition from DELIVERED back to any other status
- ✅ Cannot transition from CANCELLED back to any other status
- ✅ Status changes generate domain events
- ✅ Tracks previous status for audit trail

```java
public class Tracking {
    private final TrackingId trackingId;
    private ShipmentStatus currentStatus;
    private ShipmentStatus previousStatus;

    // Business logic: Update status with validation
    public Optional<StatusChange> updateStatus(ShipmentStatus newStatus, String location) {
        if (!isValidTransition(this.currentStatus, newStatus)) {
            throw new IllegalStateException(
                "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        StatusChange event = StatusChange.of(trackingId, currentStatus, newStatus, ...);

        this.previousStatus = this.currentStatus;
        this.currentStatus = newStatus;
        this.updatedAt = LocalDateTime.now();

        return Optional.of(event);
    }

    // Business rule: Check if transition is valid
    private boolean isValidTransition(ShipmentStatus from, ShipmentStatus to) {
        return from != ShipmentStatus.DELIVERED && from != ShipmentStatus.CANCELLED;
    }

    // Business query methods
    public boolean isOverdue() {
        return estimatedDelivery != null
            && LocalDateTime.now().isAfter(estimatedDelivery)
            && currentStatus != ShipmentStatus.DELIVERED;
    }

    public boolean wasRecentlyChecked() {
        return lastCheckedAt != null
            && Duration.between(lastCheckedAt, LocalDateTime.now()).toMinutes() < 60;
    }
}
```

**3. Domain Events (StatusChange, ShipmentStatusChangedEvent)**
- Represent something that happened in the domain
- Immutable records of past events
- Used for inter-service communication via Kafka

**4. Repository Interface (TrackingRepository)**
- Domain contract for data access
- Collection-like interface (no implementation details)
- Dependency Inversion Principle applied

```java
public interface TrackingRepository {
    Tracking save(Tracking tracking);
    Optional<Tracking> findByTrackingId(TrackingId trackingId);
    List<Tracking> findAll();
    void delete(TrackingId trackingId);
}
```

**Benefits:**
- Business logic is isolated and easy to test
- Domain model is independent of persistence technology
- Can be reused across different applications
- Changes to infrastructure don't affect domain logic

#### Application Layer

Orchestrates the flow of data between the domain and infrastructure layers. Implements **Use Case pattern** and **Hexagonal Architecture ports**.

**Responsibilities:**
- Convert DTOs to domain objects and vice versa
- Execute domain services and coordinate workflows
- Handle transaction boundaries (via `@Transactional`)
- Implement application-level error handling
- Define ports (interfaces) for infrastructure dependencies

**Key Components:**

**1. Application Services (TrackingApplicationService)**
- Orchestrates use cases
- Coordinates domain logic with infrastructure
- No business rules (delegated to domain layer)

```java
@Service
@Transactional
public class TrackingApplicationService implements TrackingUseCase {

    private final TrackingRepository repository;
    private final ExternalShipmentApiPort externalApi;
    private final EventPublisherPort eventPublisher;

    @Override
    public TrackingResponse createTracking(TrackingRequest request) {
        // 1. Validate tracking doesn't already exist
        TrackingId trackingId = TrackingId.of(request.trackingId());
        if (repository.findByTrackingId(trackingId).isPresent()) {
            throw new TrackingAlreadyExistsException(trackingId);
        }

        // 2. Fetch data from external API
        ExternalShipmentData externalData =
            externalApi.fetchShipmentStatus(request.trackingId());

        // 3. Create domain aggregate
        Tracking tracking = Tracking.create(
            trackingId,
            ShipmentStatus.valueOf(externalData.status()),
            externalData.location(),
            externalData.carrier(),
            externalData.estimatedDelivery()
        );

        // 4. Save to repository
        tracking = repository.save(tracking);

        // 5. Publish domain event
        eventPublisher.publish(new ShipmentStatusChangedEvent(
            tracking.getTrackingId().getValue(),
            null,
            tracking.getCurrentStatus().name(),
            tracking.getLastLocation(),
            LocalDateTime.now()
        ));

        // 6. Return response DTO
        return TrackingResponse.fromDomain(tracking);
    }
}
```

**2. DTOs (Data Transfer Objects)**
- Java 21 records for immutability
- Used for API communication
- Separate from domain model

```java
public record TrackingRequest(
    @NotBlank String trackingId,
    @NotNull String currentStatus,
    String lastLocation,
    String carrier,
    LocalDateTime estimatedDelivery
) {}

public record TrackingResponse(
    String trackingId,
    String currentStatus,
    String previousStatus,
    String lastLocation,
    String carrier,
    LocalDateTime estimatedDelivery,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastCheckedAt
) {
    public static TrackingResponse fromDomain(Tracking tracking) {
        return new TrackingResponse(
            tracking.getTrackingId().getValue(),
            tracking.getCurrentStatus().name(),
            tracking.getPreviousStatus() != null
                ? tracking.getPreviousStatus().name() : null,
            tracking.getLastLocation(),
            tracking.getCarrier(),
            tracking.getEstimatedDelivery(),
            tracking.getCreatedAt(),
            tracking.getUpdatedAt(),
            tracking.getLastCheckedAt()
        );
    }
}
```

**3. Ports (Interfaces)**

**Input Ports (Use Cases):**
```java
public interface TrackingUseCase {
    TrackingResponse createTracking(TrackingRequest request);
    TrackingResponse getTracking(String trackingId);
    TrackingResponse updateTracking(String trackingId, TrackingRequest request);
    void deleteTracking(String trackingId);
    List<TrackingResponse> getAllTrackings();
}
```

**Output Ports (Dependencies):**
```java
public interface ExternalShipmentApiPort {
    ExternalShipmentData fetchShipmentStatus(String trackingId);
}

public interface EventPublisherPort {
    void publish(ShipmentStatusChangedEvent event);
}
```

**Benefits:**
- Clear separation between API and domain
- Use cases are explicit and testable
- Easy to mock dependencies in tests
- Supports multiple presentation layers (REST, GraphQL, gRPC)

#### Infrastructure Layer

Provides technical implementations for domain contracts and framework configurations. This is where **all framework-specific code** lives.

**Responsibilities:**
- Database access (JPA/Hibernate)
- External service integrations (WebClient)
- Message broker integration (Kafka)
- REST API implementation (Spring MVC)
- Framework configuration (Spring Boot)

**Key Components:**

**1. Input Adapters (Primary/Driving Adapters)**

**REST Controller:**
```java
@RestController
@RequestMapping("/api/trackings")
@Tag(name = "Tracking API", description = "Shipment tracking management")
public class TrackingController {

    private final TrackingUseCase trackingUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new tracking")
    public TrackingResponse createTracking(
            @Valid @RequestBody TrackingRequest request) {
        return trackingUseCase.createTracking(request);
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Get tracking by ID")
    public TrackingResponse getTracking(
            @PathVariable String trackingId) {
        return trackingUseCase.getTracking(trackingId);
    }
}
```

**2. Output Adapters (Secondary/Driven Adapters)**

**Persistence Adapter:**
```java
@Component
public class TrackingRepositoryAdapter implements TrackingRepository {

    private final TrackingJpaRepository jpaRepository;
    private final TrackingMapper mapper;

    @Override
    public Tracking save(Tracking tracking) {
        TrackingEntity entity = mapper.toEntity(tracking);
        TrackingEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Tracking> findByTrackingId(TrackingId trackingId) {
        return jpaRepository.findByTrackingId(trackingId.getValue())
            .map(mapper::toDomain);
    }
}
```

**External API Adapter:**
```java
@Component
public class ExternalShipmentApiAdapter implements ExternalShipmentApiPort {

    private final WebClient webClient;

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ExternalShipmentData fetchShipmentStatus(String trackingId) {
        return webClient.get()
            .uri("/shipments/{id}", trackingId)
            .retrieve()
            .bodyToMono(ExternalShipmentData.class)
            .timeout(Duration.ofSeconds(5))
            .block();
    }
}
```

**Kafka Event Publisher:**
```java
@Component
public class KafkaEventPublisher implements EventPublisherPort {

    private final KafkaTemplate<String, ShipmentStatusChangedEvent> kafkaTemplate;

    @Value("${kafka.topics.shipment-status-changes}")
    private String topic;

    @Override
    public void publish(ShipmentStatusChangedEvent event) {
        kafkaTemplate.send(topic, event.trackingId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event", ex);
                } else {
                    log.info("Event published for tracking {}", event.trackingId());
                }
            });
    }
}
```

**Kafka Event Consumer (NotificationService):**
```java
@Component
public class ShipmentStatusEventListener {

    private final NotificationService notificationService;

    @KafkaListener(
        topics = "${kafka.topics.shipment-status-changes}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStatusChangeEvent(
            @Payload ShipmentStatusChangedEvent event,
            Acknowledgment acknowledgment) {
        try {
            log.info("Received event for tracking: {}", event.trackingId());
            notificationService.processStatusChangeEvent(event);
            acknowledgment.acknowledge(); // Manual commit
            log.info("Successfully processed event");
        } catch (Exception e) {
            log.error("Error processing event", e);
            // Don't acknowledge - message will be redelivered
        }
    }
}
```

**3. JPA Entities and Mappers**

**JPA Entity:**
```java
@Entity
@Table(name = "trackings")
public class TrackingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_id", unique = true, nullable = false)
    private String trackingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private ShipmentStatus currentStatus;
}
```

**Mapper (Entity ↔ Domain):**
```java
@Component
public class TrackingMapper {
    public TrackingEntity toEntity(Tracking domain) {
        TrackingEntity entity = new TrackingEntity();
        entity.setTrackingId(domain.getTrackingId().getValue());
        entity.setCurrentStatus(domain.getCurrentStatus());
        return entity;
    }

    public Tracking toDomain(TrackingEntity entity) {
        return Tracking.builder()
            .trackingId(TrackingId.of(entity.getTrackingId()))
            .currentStatus(entity.getCurrentStatus())
            .build();
    }
}
```

**Benefits:**
- Domain layer is completely isolated from infrastructure
- Easy to swap implementations (e.g., PostgreSQL instead of H2)
- Can test domain logic without infrastructure
- Supports multiple adapters (REST, gRPC, CLI)

### SOLID Principles

This project strictly adheres to **SOLID principles** throughout the codebase:

#### 1. Single Responsibility Principle (SRP)

**"A class should have one, and only one, reason to change."**

Each class in the system has a single, well-defined responsibility:

- **TrackingId** - Only validates and encapsulates tracking identifiers
- **Tracking** - Only manages tracking state and business rules
- **TrackingApplicationService** - Only orchestrates tracking use cases
- **TrackingController** - Only handles HTTP requests/responses
- **TrackingRepositoryAdapter** - Only bridges domain and persistence
- **KafkaEventPublisher** - Only publishes events to Kafka

**Example:**
```java
// ✅ GOOD: Single responsibility - only validates tracking IDs
public class TrackingId {
    private final String value;

    private TrackingId(String value) {
        validate(value);  // Only validation logic
        this.value = value;
    }

    private void validate(String value) {
        // Validation rules only
    }
}

// ❌ BAD: Multiple responsibilities
public class TrackingId {
    private String value;

    public void saveToDatabase() { }  // Persistence responsibility
    public void sendEmail() { }       // Notification responsibility
    public void validate() { }        // Validation responsibility
}
```

#### 2. Open/Closed Principle (OCP)

**"Software entities should be open for extension but closed for modification."**

The system uses interfaces and abstractions to allow extension without modifying existing code:

- **Repository interfaces** - New implementations can be added without changing domain code
- **Port interfaces** - New adapters can be plugged in without changing application layer
- **Notification types** - New notification channels can be added by implementing `NotificationSender`

**Example:**
```java
// ✅ GOOD: Open for extension via interface
public interface NotificationSender {
    void send(String recipient, String subject, String message);
}

// Can add new implementations without changing existing code
@Component
public class EmailNotificationSender implements NotificationSender { }

@Component
public class SmsNotificationSender implements NotificationSender { }

@Component
public class PushNotificationSender implements NotificationSender { }

// ❌ BAD: Closed for extension
public class NotificationSender {
    public void send(String type, String recipient, String message) {
        if (type.equals("EMAIL")) {
            // Email logic - need to modify this class to add SMS
        }
    }
}
```

#### 3. Liskov Substitution Principle (LSP)

**"Derived classes must be substitutable for their base classes."**

All implementations honor their interface contracts without surprising behavior:

- **TrackingRepository implementations** - All implementations preserve domain semantics
- **NotificationSender implementations** - All implementations follow the same contract
- **ExternalShipmentApiPort implementations** - Real and test implementations are interchangeable

**Example:**
```java
// ✅ GOOD: All implementations are substitutable
public interface TrackingRepository {
    Tracking save(Tracking tracking);
    Optional<Tracking> findByTrackingId(TrackingId id);
}

// Production implementation
@Component
public class TrackingRepositoryAdapter implements TrackingRepository {
    // Uses JPA - honors contract
}

// Test implementation
public class InMemoryTrackingRepository implements TrackingRepository {
    // Uses HashMap - honors same contract
}

// Both can be used interchangeably without breaking code
```

#### 4. Interface Segregation Principle (ISP)

**"Clients should not be forced to depend on methods they do not use."**

The system uses small, focused interfaces rather than large, monolithic ones:

- **TrackingUseCase** - Only defines tracking operations
- **ExternalShipmentApiPort** - Only defines external API operations
- **EventPublisherPort** - Only defines event publishing operations
- **NotificationSender** - Only defines notification sending operations

**Example:**
```java
// ✅ GOOD: Focused interfaces
public interface ExternalShipmentApiPort {
    ExternalShipmentData fetchShipmentStatus(String trackingId);
}

public interface EventPublisherPort {
    void publish(ShipmentStatusChangedEvent event);
}

// Clients only depend on what they need
public class TrackingApplicationService {
    private final ExternalShipmentApiPort externalApi;  // Only needs API access
    private final EventPublisherPort eventPublisher;    // Only needs event publishing
}

// ❌ BAD: Fat interface
public interface TrackingService {
    Tracking save(Tracking tracking);
    ExternalShipmentData fetchFromApi(String id);
    void publishEvent(Event event);
    void sendEmail(String to, String subject);
    void generateReport();
    void backup();
    // Forces clients to depend on methods they don't use
}
```

#### 5. Dependency Inversion Principle (DIP)

**"High-level modules should not depend on low-level modules. Both should depend on abstractions."**

The architecture is built around this principle:

- **Domain layer** defines repository interfaces (abstractions)
- **Infrastructure layer** implements these interfaces (concrete implementations)
- **Application layer** depends on ports (abstractions), not adapters (implementations)
- **All dependencies point inward** toward the domain layer

**Dependency Flow:**
```
Infrastructure → Application → Domain
    (adapters)   → (use cases) → (business rules)
```

**Example:**
```java
// ✅ GOOD: High-level depends on abstraction
// Domain layer defines the contract
public interface TrackingRepository {
    Tracking save(Tracking tracking);
}

// Application layer depends on abstraction
@Service
public class TrackingApplicationService {
    private final TrackingRepository repository;  // Depends on interface

    public TrackingApplicationService(TrackingRepository repository) {
        this.repository = repository;
    }
}

// Infrastructure layer provides implementation
@Component
public class TrackingRepositoryAdapter implements TrackingRepository {
    private final TrackingJpaRepository jpaRepository;  // Spring Data JPA
    // Implementation details
}

// Spring wires everything automatically via dependency injection

// ❌ BAD: High-level depends on low-level
@Service
public class TrackingApplicationService {
    private final TrackingRepositoryAdapter repository;  // Depends on concrete class
    private final JpaRepository jpaRepository;           // Depends on JPA directly
}
```

**Benefits of SOLID Principles:**
- ✅ Code is easier to understand and maintain
- ✅ Changes are localized and predictable
- ✅ Testing is straightforward with mocks
- ✅ Components can be reused across projects
- ✅ System is flexible and extensible

---

## Technical Details

### Event-Driven Architecture (EDA)

The system implements **Event-Driven Architecture** using Apache Kafka as the message broker.

**Event Flow:**

```
TrackingService                    Kafka                    NotificationService
      │                              │                              │
      │ 1. Status changes            │                              │
      ├─────────────────────────────>│                              │
      │    (ShipmentStatusChanged)   │                              │
      │                              │                              │
      │                              │ 2. Event is persisted        │
      │                              │    and replicated            │
      │                              │                              │
      │                              │ 3. Consumer polls            │
      │                              │<─────────────────────────────┤
      │                              │                              │
      │                              │ 4. Event delivered           │
      │                              ├─────────────────────────────>│
      │                              │                              │
      │                              │                              │ 5. Process event
      │                              │                              │    Create notification
      │                              │                              │
      │                              │ 6. Manual acknowledge        │
      │                              │<─────────────────────────────┤
      │                              │    (offset committed)        │
```

**Key Patterns:**

**1. Domain Events (Shared Kernel)**

Events represent facts that happened in the domain:

```java
public record ShipmentStatusChangedEvent(
    String trackingId,
    String previousStatus,
    String newStatus,
    String location,
    LocalDateTime occurredAt
) implements Serializable {
    public static ShipmentStatusChangedEvent from(
            TrackingId trackingId,
            ShipmentStatus previousStatus,
            ShipmentStatus newStatus,
            String location) {
        return new ShipmentStatusChangedEvent(
            trackingId.getValue(),
            previousStatus != null ? previousStatus.name() : null,
            newStatus.name(),
            location,
            LocalDateTime.now()
        );
    }
}
```

**2. Event Publishing (Producer)**

Events are published asynchronously to Kafka:

```java
@Component
public class KafkaEventPublisher implements EventPublisherPort {

    private final KafkaTemplate<String, ShipmentStatusChangedEvent> kafkaTemplate;

    @Override
    public void publish(ShipmentStatusChangedEvent event) {
        kafkaTemplate.send(topic, event.trackingId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event for tracking {}: {}",
                        event.trackingId(), ex.getMessage());
                    // Could implement retry or dead letter queue
                } else {
                    RecordMetadata metadata = result.getRecordMetadata();
                    log.info("Event published successfully - topic: {}, partition: {}, offset: {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
    }
}
```

**3. Event Consumption (Consumer)**

Events are consumed with manual acknowledgment for reliability:

```java
@KafkaListener(
    topics = "${kafka.topics.shipment-status-changes}",
    groupId = "${spring.kafka.consumer.group-id}",
    containerFactory = "kafkaListenerContainerFactory"
)
public void handleStatusChangeEvent(
        @Payload ShipmentStatusChangedEvent event,
        Acknowledgment acknowledgment) {
    try {
        // Process event
        notificationService.processStatusChangeEvent(event);

        // Manual acknowledgment - commit offset only if processing succeeds
        acknowledgment.acknowledge();

        log.info("Successfully processed event for tracking: {}", event.trackingId());
    } catch (Exception e) {
        log.error("Error processing event for tracking: {}", event.trackingId(), e);
        // Don't acknowledge - Kafka will redeliver the message
        // Configure retry and dead letter queue for failed messages
    }
}
```

**4. Kafka Configuration**

**Producer Configuration:**
```java
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, ShipmentStatusChangedEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");           // Wait for all replicas
        config.put(ProducerConfig.RETRIES_CONFIG, 3);            // Retry on failure
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");  // Compress messages
        return new DefaultKafkaProducerFactory<>(config);
    }
}
```

**Consumer Configuration:**
```java
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, ShipmentStatusChangedEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");    // Start from beginning
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);        // Manual commit
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");                 // Trust all packages
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ShipmentStatusChangedEvent>
            kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ShipmentStatusChangedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);  // Manual acknowledgment
        return factory;
    }
}
```

**Benefits of EDA:**
- ✅ **Loose coupling** - Services don't know about each other
- ✅ **Scalability** - Services can scale independently
- ✅ **Reliability** - Messages are persisted and replicated
- ✅ **Resilience** - Failed messages can be retried
- ✅ **Auditability** - All events are logged and traceable

### Value Objects

Value Objects are **immutable, self-validating** domain primitives that encapsulate business rules.

**Characteristics:**
- Immutable (all fields are `final`)
- Private constructors
- Factory methods (`.of()`) for creation
- Built-in validation
- No identity (equality by value)
- No setters

**Example: TrackingId**

```java
public class TrackingId {
    private static final int MAX_LENGTH = 50;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Z0-9]+$");

    private final String value;  // Immutable

    private TrackingId(String value) {  // Private constructor
        validate(value);
        this.value = value;
    }

    public static TrackingId of(String value) {  // Factory method
        return new TrackingId(value);
    }

    private void validate(String value) {  // Self-validating
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TrackingId cannot be null or blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "TrackingId cannot exceed " + MAX_LENGTH + " characters");
        }
        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "TrackingId must contain only uppercase alphanumeric characters");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {  // Equality by value
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackingId that = (TrackingId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
```

**Benefits:**
- ✅ **Type safety** - Cannot pass a `ProductId` where `TrackingId` is expected
- ✅ **Encapsulation** - Validation logic is centralized
- ✅ **Explicit domain language** - Code reads like business language
- ✅ **Prevents primitive obsession** - No raw strings floating around
- ✅ **Immutability** - Thread-safe and predictable

### Repository Pattern

The Repository pattern abstracts data access and enables **Dependency Inversion Principle**.

**Domain Repository Interface:**
```java
// Domain layer - defines the contract
public interface TrackingRepository {
    Tracking save(Tracking tracking);
    Optional<Tracking> findByTrackingId(TrackingId trackingId);
    List<Tracking> findAll();
    void delete(TrackingId trackingId);
    boolean existsByTrackingId(TrackingId trackingId);
}
```

**Infrastructure Implementation:**
```java
// Infrastructure layer - implements the contract
@Component
public class TrackingRepositoryAdapter implements TrackingRepository {

    private final TrackingJpaRepository jpaRepository;
    private final TrackingMapper mapper;

    @Override
    public Tracking save(Tracking tracking) {
        TrackingEntity entity = mapper.toEntity(tracking);
        TrackingEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Tracking> findByTrackingId(TrackingId trackingId) {
        return jpaRepository.findByTrackingId(trackingId.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public List<Tracking> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(TrackingId trackingId) {
        jpaRepository.deleteByTrackingId(trackingId.getValue());
    }

    @Override
    public boolean existsByTrackingId(TrackingId trackingId) {
        return jpaRepository.existsByTrackingId(trackingId.getValue());
    }
}
```

**Spring Data JPA Repository:**
```java
@Repository
public interface TrackingJpaRepository extends JpaRepository<TrackingEntity, Long> {
    Optional<TrackingEntity> findByTrackingId(String trackingId);
    void deleteByTrackingId(String trackingId);
    boolean existsByTrackingId(String trackingId);

    @Query("SELECT t FROM TrackingEntity t WHERE t.currentStatus = :status")
    List<TrackingEntity> findByStatus(@Param("status") ShipmentStatus status);
}
```

**Benefits:**
- ✅ Domain layer independent of persistence technology
- ✅ Easy to test with in-memory implementations
- ✅ Can swap database without changing domain code
- ✅ Queries are centralized and reusable

### Data Initialization

Data is automatically loaded on application startup through SQL scripts.

**Configuration (application.yml):**
```yaml
spring:
  sql:
    init:
      mode: always                          # Always execute scripts
      schema-locations: classpath:schema.sql # DDL script
      data-locations: classpath:data.sql     # DML script
  jpa:
    hibernate:
      ddl-auto: none                        # Disable Hibernate auto-DDL
    defer-datasource-initialization: true    # Execute scripts before validation
```

**Startup Process:**

1. **Spring Boot starts** and initializes the H2 database
2. **schema.sql executes** creating tables, constraints, and indexes
3. **data.sql executes** inserting test data
4. **Hibernate validates** entity mappings against existing schema
5. **Application ready** with pre-loaded data

**Why this approach?**
- ✅ **Explicit control** over database schema (no auto-DDL surprises)
- ✅ **Version control** of database structure via SQL files
- ✅ **Production-like environment** simulation
- ✅ **Easy migration** to real databases (PostgreSQL, MySQL)
- ✅ **Predictable** and **repeatable** initialization

---

## Contributing

This is a demonstration project showcasing enterprise-grade software architecture patterns including Domain-Driven Design (DDD), Hexagonal Architecture, and Event-Driven Architecture (EDA).

**Key Learning Objectives:**
- ✅ DDD tactical patterns (Value Objects, Aggregates, Domain Events)
- ✅ Hexagonal Architecture (Ports & Adapters)
- ✅ Event-Driven Architecture with Apache Kafka
- ✅ SOLID principles in practice
- ✅ Test-Driven Development (TDD)
- ✅ Clean Architecture with clear layer separation

For questions or feedback, please open an issue on the project repository.

---

## License

This project is provided as-is for educational and demonstration purposes.

---

## Acknowledgments

**Architecture Patterns:**
- Domain-Driven Design (Eric Evans)
- Hexagonal Architecture (Alistair Cockburn)
- Clean Architecture (Robert C. Martin)

**Technologies:**
- Spring Boot Team
- Apache Kafka Community
- JetBrains IntelliJ IDEA

---

**Thank you for exploring this project!**

*Built with ❤️ using Domain-Driven Design, Hexagonal Architecture, and Event-Driven Architecture.*
