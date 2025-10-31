# Logging Service (`logging-svc`)

A Spring Boot microservice that publishes and consumes structured log messages via **Spring Cloud Stream**, supporting **Kafka** and **RabbitMQ** as interchangeable messaging binders.

---

## Overview

`logging-svc` is a lightweight log-processing service that:

- Publishes structured logs (`LogMessage`) to a message broker (Kafka or RabbitMQ)
- Consumes messages from the same topic/queue for downstream storage or analytics
- Is fully binder-agnostic — the same code works with either Kafka or RabbitMQ by switching profiles

Use cases:

- Centralized logging and monitoring
- Distributed microservice audit trails
- Asynchronous event-driven processing

---

## Architecture

```
+------------------+           +---------------------+           +------------------+
|    REST Client   |  --->     |   logging-svc       |  --->     |  Kafka / Rabbit  |
| (e.g., Postman)  |           | (Spring Boot App)   |           |   Broker Queue   |
+------------------+           +---------------------+           +------------------+
          |                               |                                |
          |  POST /logs/send              |                                |
          |------------------------------>|                                |
          |                               | streamBridge.send("logProducer-out-0")
          |                               |---------------------------------------> topic/queue
                                          |<--------------------------------------- consumer triggers
                                          |        logConsumer(LogMessage)
```

---

## Tech Stack

| Layer | Technology |
|--------|-------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| Messaging | Spring Cloud Stream 4.3.0 |
| Binders | Kafka, RabbitMQ |
| Build | Maven |
| Observability | Actuator + Prometheus |
| Lombok | DTO / Builder |

---

## Project Structure

```
logging-svc/
 ├── controller/LogController.java       # REST endpoint to send logs
 ├── consumer/LogConsumerConfig.java     # Consumer function
 ├── publisher/LogPublisher.java         # Publishes messages
 ├── model/LogMessage.java               # Log DTO
 ├── resources/application.yml           # Shared + profile configs
 ├── LoggingSvcApplication.java          # Main class
 └── pom.xml
```

---

## Configuration (`application.yml`)

The app supports two profiles:
- `kafka` (default)
- `rabbit`

### Base configuration
```yaml
spring:
  application:
    name: logging-svc
  profiles:
    active: kafka

server:
  port: 8088
```

### Kafka profile
```yaml
spring:
  config:
    activate:
      on-profile: kafka

  kafka:
    bootstrap-servers: localhost:9092

  cloud:
    stream:
      default-binder: kafka
      function:
        definition: logConsumer;logProducer
      bindings:
        logProducer-out-0:
          destination: log-topic
        logConsumer-in-0:
          destination: log-topic
          group: logging-svc
```

### RabbitMQ profile
```yaml
spring:
  config:
    activate:
      on-profile: rabbit

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

  cloud:
    stream:
      default-binder: rabbit
      function:
        definition: logConsumer;logProducer
      bindings:
        logProducer-out-0:
          destination: log-queue
        logConsumer-in-0:
          destination: log-queue
          group: logging-svc
```

---

## Message Model

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    private String level;
    private String message;
    private String serviceName;
    private String timestamp;
}
```

---

## Publishing Logs

**Endpoint**

```
POST /logs/send?level=INFO&message=Something%20happened
```

Example curl:
```bash
curl -X POST "http://localhost:8088/logs/send?level=INFO&message=Service%20started"
```

Internally:
```java
streamBridge.send("logProducer-out-0", message);
```

✅ Message goes to:
- Kafka → `log-topic` (when profile `kafka`)
- RabbitMQ → `log-queue` (when profile `rabbit`)

---

## Consuming Logs

`LogConsumerConfig.java`
```java
@Bean
public Consumer<LogMessage> logConsumer() {
    return log -> {
        System.out.println("📥 Received log: " + log);
        // future: save to DB or Elasticsearch
    };
}
```

Triggered automatically when new messages arrive.

---

## Maven Dependencies (excerpt)

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-stream-binder-kafka</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

## Docker Setup

### Kafka + Zookeeper
```yaml
version: "3.9"
services:
  zookeeper:
    image: bitnami/zookeeper:3.9
    ports: ["2181:2181"]
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes

  kafka:
    image: bitnami/kafka:3.8
    ports: ["9092:9092"]
    environment:
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
    depends_on:
      - zookeeper
```

### RabbitMQ
```yaml
rabbitmq:
  image: rabbitmq:3.13-management
  ports:
    - "5672:5672"
    - "15672:15672"
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
```

---

## Run & Test

1️⃣ Start Kafka or RabbitMQ containers
```bash
docker compose up -d
```

2️⃣ Run the app
```bash
# Kafka mode
mvn spring-boot:run -Dspring-boot.run.profiles=kafka

# Rabbit mode
mvn spring-boot:run -Dspring-boot.run.profiles=rabbit
```

3️⃣ Send a test log
```bash
curl -X POST "http://localhost:8088/logs/send?level=INFO&message=Kafka%20works"
```

Console output:
```
✅ Published log: LogMessage(level=INFO, ...)
📥 Received log: LogMessage(level=INFO, ...)
```

---

## Troubleshooting

| Error | Cause | Fix |
|-------|--------|-----|
| Failed to start bean 'inputBindingLifecycle' | Both binders active | Use one profile |
| Failed to locate function 'logConsumer' | Missing consumer bean | Add `@Bean Consumer<LogMessage>` |
| Broker not reachable | Kafka/Rabbit not running | Start Docker containers |
| A default binder has been requested... | Multiple binders, no default | Add `spring.cloud.stream.default-binder` |

---

## Monitoring Endpoints

| Endpoint | Description |
|-----------|-------------|
| `/actuator/health` | Service health |
| `/actuator/metrics` | Runtime metrics |
| `/actuator/prometheus` | Prometheus scrape |

---

## Future Enhancements

- Persist logs to DB / Elasticsearch
- Add retry + DLQ handling
- Grafana dashboards
- Log filtering & query API
- Support cloud brokers (AWS MSK / RabbitMQ Cloud)

---

## Author

**Nasruddin Khan**  
Software Engineer – Alinma Bank  
_Enterprise Logging and Monitoring Platform_
