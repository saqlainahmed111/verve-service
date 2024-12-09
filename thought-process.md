# thought-process.md

## **High-Level Overview**

The implementation aims to build a RESTful microservice with robust support for request handling, deduplication, optional endpoint communication, Kafka streaming, and scheduled logging. The service leverages **Redis for deduplication**, **Kafka for distributed logging**, and **Spring WebFlux** for efficient non-blocking request handling.

This document provides insights into the design decisions, architecture, and approach to meet all the functional requirements and scalability considerations.

---

## **1. Implementation Approach**

The core implementation approach follows the principles of microservices, with a focus on separation of concerns and scalability:

1. **REST Service with WebFlux**:
    - Exposed an endpoint `/api/verve/accept`.
    - Handled incoming request parameters (`id` as mandatory and `endpoint` as optional).

2. **Deduplication with Redis**:
    - Ensures that repeated requests are only processed once.
    - Redis is used as the distributed state store for request uniqueness tracking.

3. **Kafka for Message Streaming**:
    - Kafka topic streaming is implemented to handle counts of unique requests for distributed logging and monitoring.

4. **Scheduled Task for Periodic Logging**:
    - Logs the unique requests count every minute to support auditing and analysis.

5. **Optional HTTP Communication**:
    - If an endpoint URL is specified, it can dynamically send GET/POST requests using `WebClient`.

6. **Redis Cluster for Distributed Scalability**:
    - While basic Redis deduplication is in place, a Redis cluster will support high availability across distributed systems.

---

## **2. Design Considerations**

### **2.1 Scalability**
- **Redis for Deduplication**:
  Redis is chosen for deduplication because:
    - It provides atomic operations (`SET` commands).
    - Scales well for distributed caching and can handle millions of requests.

- **Kafka for Asynchronous Message Handling**:
  Kafka decouples unique event counts from processing logic, allowing distributed log streaming even during heavy traffic loads.

---

### **2.2 Fault Tolerance**
1. **Redis Failover**:
    - A Redis cluster can be used in production deployments to ensure data persistence and high availability.

2. **Kafka Resilience**:
    - Kafka handles retries and data consistency through topic partitioning and replication mechanisms.

3. **Scheduled Logging**:
    - Logging is scheduled every minute to balance latency and performance.

---

### **2.3 Data Integrity**
1. Deduplication logic ensures that repeated requests within a specific interval are ignored.
2. Kafka topics are written with unique counts only to avoid data duplication.

---

### **2.4 Ease of Testing & Deployment**
1. Simplified dependency injection and modular design with clear separation between services, controllers, and repositories.
2. Tested Kafka streaming logic with local Kafka servers.
3. Redis is configured locally for development environments but will scale with clustering in production.

---

## **3. Implementation Design**

The microservice consists of the following core components:

### **3.1 REST Controller**

Exposed the `/api/verve/accept` endpoint. This listens for:
- Mandatory query parameter: `id`.
- Optional query parameter: `endpoint`.

The controller handles request validation and routes logic to the service layer.

---

### **3.2 Service Layer**

The service layer performs:
1. **Deduplication Logic**:
    - Redis-based sets are used to determine if an incoming request is unique.
2. **Kafka Publishing**:
    - Unique counts are sent to a Kafka topic (`unique-request-topic`) for monitoring.
3. **Optional External Requests**:
    - Handles sending HTTP GET/POST calls when the endpoint is specified.

---

### **3.3 Redis**

Redis is leveraged for:
- Request deduplication logic.
- Avoiding repeated processing by storing unique request IDs temporarily.

Redis operations involve:
```java
redisTemplate.opsForSet().add("unique-requests", id)
