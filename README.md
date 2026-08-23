# Digital Banking System

A production-style **Digital Banking System** built with a microservices architecture. The project demonstrates real-time money transfers, fraud detection, distributed transaction management using the **Saga pattern**, asynchronous communication with **Apache Kafka**, API gateway rate limiting, notifications, OTP verification, and payment integration.

> This README is based on the accompanying tutorial transcript. Some implementation details such as exact credentials, secrets, and environment-specific configuration are intentionally left as placeholders.

## 🚀 Overview

The system is designed around a common banking problem:

> What happens when money is deducted from a sender's account, but a later step in a distributed transaction fails?

With a traditional single database transaction, the database can use `BEGIN`, `COMMIT`, and `ROLLBACK`. In a microservices architecture, however, each service can have its own database, so one service cannot simply roll back work already committed by another service.

This project addresses that problem using a **Saga-based transaction flow** with compensating actions such as refunds.

It also introduces a **real-time fraud detection service** that consumes transaction events through Kafka before the transfer is completed.

## ✨ Features

- Bank account creation
- Account and balance management
- Sender → receiver money transfers
- Balance validation before transfer
- Distributed transaction processing
- Saga orchestration
- Compensating/refund operations
- Real-time fraud detection
- Suspicious transaction verification
- OTP generation and verification
- Temporary account blocking
- Transaction status tracking
- Kafka-based asynchronous event communication
- Notification service
- Debit/credit alerts
- Suspicious-activity alerts
- Refund and payment-failure notifications
- API Gateway as the single entry point
- Redis-based request rate limiting
- Payment gateway integration
- Docker-based infrastructure
- MySQL persistence
- Spring Boot Actuator health endpoints

## 🏗️ Architecture

The system contains the following major components:

```text
                         ┌───────────────────┐
                         │       Client      │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │    API Gateway    │
                         │ Rate Limiting     │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │ Transaction       │
                         │ Service           │
                         │ Saga Orchestrator │
                         └─────────┬─────────┘
                                   │
                  ┌────────────────┼────────────────┐
                  │                │                │
                  ▼                ▼                ▼
          ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
          │   Account    │  │    Kafka     │  │   Payment    │
          │   Service    │  │              │  │   Service    │
          └──────────────┘  └──────┬───────┘  └──────────────┘
                                   │
                     ┌─────────────┼─────────────┐
                     │             │             │
                     ▼             ▼             ▼
              ┌────────────┐ ┌────────────┐ ┌──────────────┐
              │   Fraud    │ │Notification│ │   Account    │
              │ Detection  │ │  Service   │ │   Service    │
              │  Service   │ │            │ │ (credit/refund)
              └────────────┘ └────────────┘ └──────────────┘
```

The API Gateway acts as the single entry point. The Transaction Service acts as the decision-maker and Saga orchestrator. Kafka connects the services through events.

The tutorial specifically places Kafka between the services so that the fraud detection service can consume every initiated transaction and evaluate it before the transfer is completed. fileciteturn2file5L790-L838

## 🧩 Microservices

### 1. Account Service

Responsible for banking/account operations:

- Create account
- Get account
- Get balance
- Deduct balance
- Credit receiver
- Credit sender for refund
- Block account
- Maintain account status

The account service uses MySQL and Spring Data JPA.

### 2. Transaction Service

This is the central decision-making service and acts as the **Saga orchestrator**.

Responsibilities include:

- Start a transaction
- Coordinate Saga steps
- Track transaction status
- Publish transaction events
- Consume fraud results
- Decide whether the transaction should continue
- Trigger completion/refund flows
- Verify OTP

The tutorial describes this service as the backbone/heart of the banking workflow because it decides what should happen at each stage. fileciteturn1file5L718-L736

### 3. Fraud Detection Service

Responsible for detecting suspicious transactions in real time.

It consumes transaction events from Kafka and evaluates whether a transaction appears suspicious.

Possible outcomes include:

```text
Transaction
    │
    ▼
Fraud Check
    │
    ├── Clean ────────► Continue transfer
    │
    └── Suspicious ───► Verification / Block / Refund
```

The tutorial also covers temporarily blocking an account after failed verification or repeated suspicious activity. fileciteturn1file0L19-L61

### 4. Notification Service

Responsible for sending user notifications such as:

- Debit alerts
- Credit alerts
- Suspicious activity alerts
- OTP notifications
- Refund notifications
- Payment success notifications
- Payment failure notifications

The tutorial describes notifications through channels such as email, phone/SMS, and WhatsApp, while the implementation begins with Java Mail and can be extended with external SMS resources. fileciteturn2file5L842-L868

### 5. Payment Service

Responsible for external payment gateway integration.

The tutorial discusses payment gateway integration and uses Razorpay in the project setup, while also mentioning Stripe as another possible gateway. fileciteturn1file5L742-L766

### 6. API Gateway

The API Gateway is the external entry point for the system.

Responsibilities:

- Route requests to microservices
- Protect services from excessive requests
- Apply rate limiting
- Expose health/gateway information

Redis is used with the gateway for reactive request rate limiting.

## 💰 Money Transfer Flow

A simplified successful transaction looks like this:

```text
Client
  │
  ▼
API Gateway
  │
  ▼
Transaction Service
  │
  ▼
Saga starts
  │
  ▼
Account Service
  │
  ├── Validate balance
  │
  └── Deduct sender balance
  │
  ▼
Transaction Service
  │
  ├── Save transaction as PROCESSING
  │
  └── Publish Transaction Initiated event
  │
  ▼
Kafka
  │
  ▼
Fraud Detection Service
  │
  ├── No fraud
  │      │
  │      ▼
  │   Transaction Service
  │      │
  │      ▼
  │   Credit Receiver
  │      │
  │      ▼
  │   Notification Service
  │
  └── Fraud detected
         │
         ▼
     Verification / OTP
         │
         ├── Verified ─────► Continue
         │
         └── Failed ───────► Block / Refund
```

The tutorial's flow first deducts the sender's balance, records the transaction as processing, publishes a `transaction initiated` event, and lets the fraud service inspect it. fileciteturn2file9L1324-L1446

## 🔄 Saga Pattern

The Saga pattern is one of the core concepts of this project.

### The problem

Suppose:

1. Sender balance is deducted.
2. Transaction service processes the next step.
3. A later service fails.

If every service has a separate database, the original database transaction cannot automatically roll back all previously committed work.

### The solution

Instead of relying on one global rollback, the system performs **compensating actions**.

Example:

```text
Step 1: Deduct sender balance
          │
          ▼
Step 2: Process transaction
          │
          ▼
Step 3: Fraud verification
          │
          ▼
Step 4: Credit receiver
```

If a later step cannot complete:

```text
Failure
  │
  ▼
Compensating action
  │
  ▼
Credit sender / Refund
```

The tutorial explains Saga using a cash-on-delivery example: a refund is an explicit new operation rather than an invisible database rollback. fileciteturn1file3L432-L528

## 🛡️ Fraud Detection

Fraud detection is treated as a critical part of the banking system.

A fraudster could automate many transactions in a short period of time, potentially draining an account before the user notices.

The system therefore sends transaction events to the fraud detection service before the transfer is finalized.

### Suspicious transaction flow

```text
Transaction Initiated
        │
        ▼
Fraud Detection
        │
   ┌────┴────┐
   │         │
 Clean    Suspicious
   │         │
   ▼         ▼
Continue   Verify User
             │
        ┌────┴────┐
        │         │
     Verified   Failed
        │         │
        ▼         ▼
     Continue   Block / Refund
```

For suspicious transactions, the system can request user verification and OTP confirmation. Failed verification can result in temporarily blocking the sender account and sending an alert. fileciteturn2file1L164-L200

## 🔐 OTP Verification

The transaction flow supports OTP-based verification for suspicious or unusual transactions.

The flow includes:

1. Detect suspicious transaction.
2. Generate OTP.
3. Notify the user.
4. User provides OTP.
5. Verify OTP.
6. Continue transaction if valid.
7. Handle invalid/expired OTP appropriately.
8. Refund or block when verification fails.

The tutorial explicitly includes OTP generation, OTP verification, and handling expiration as part of the banking system. fileciteturn2file1L176-L188

## 📡 Kafka Event Flow

Apache Kafka is used as the event backbone.

Examples of events discussed in the tutorial include:

```text
transaction.initiated
transaction.completed
fraud.detected
otp.generated
refund
payment.completed
payment.failed
debit.alert
credit.alert
```

The exact topic naming can be adapted to the implementation.

A simplified event flow:

```text
Transaction Service
       │
       │ transaction.initiated
       ▼
     Kafka
       │
       ├──────────────► Fraud Detection Service
       │
       └──────────────► Other Consumers
```

After processing, services publish events that other services consume. The account service, for example, consumes transaction-completed and fraud-detected events for balance completion and account blocking. fileciteturn1file6L919-L975

## 🧰 Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Application development |
| Spring Boot | Microservices framework |
| Spring Web | REST APIs |
| Spring Data JPA | Database persistence |
| MySQL | Relational database |
| Apache Kafka | Event-driven communication |
| Redis | Rate limiting / fast data access |
| Spring Cloud Gateway | API Gateway |
| OpenFeign | Service-to-service communication |
| Lombok | Boilerplate reduction |
| Spring Boot Actuator | Health and monitoring endpoints |
| Java Mail | Email notifications |
| Razorpay | Payment gateway integration |
| Docker | Infrastructure/container setup |
| Postman | API testing |

The tutorial uses Java 17 and Maven-based Spring Boot projects. Account Service uses Spring Web, Kafka, JPA, MySQL, validation, Lombok, and Actuator; Transaction Service additionally uses OpenFeign; Fraud Detection uses Redis; and the Gateway uses Spring Cloud Gateway with reactive Redis support. fileciteturn2file4L607-L701

## 🐳 Infrastructure

The tutorial sets up the infrastructure using Docker Compose.

The main infrastructure components are:

```text
Docker Compose
├── Redis
├── Zookeeper
└── Kafka

Local Machine
└── MySQL
```

The tutorial starts Redis, Zookeeper, and Kafka with Docker Compose while MySQL is installed/running locally. fileciteturn2file6L901-L929

Start the Docker infrastructure with:

```bash
docker compose up -d
```

If your `docker-compose.yml` defines all services, you can start everything with:

```bash
docker compose up -d
```

Check running containers:

```bash
docker ps
```

Stop the containers:

```bash
docker compose down
```

## 🔌 Service Ports

The tutorial configures the API Gateway as the main entry point on port `8080`.

The transaction service uses port `8082` and the payment service uses port `8083`. fileciteturn2file2L343-L403

A simplified setup is:

| Service | Port |
|---|---:|
| API Gateway | 8080 |
| Account Service | 8081* |
| Transaction Service | 8082 |
| Payment Service | 8083 |
| Fraud Detection Service | 8084* |
| Notification Service | 8085* |

`*` Ports marked with an asterisk should be checked against the project's actual configuration because the transcript does not establish every service port consistently.

## 🗄️ Database Strategy

The architecture follows the microservices principle of giving services their own persistence boundaries.

For example:

```text
Account Service
      │
      ▼
  Account DB

Transaction Service
      │
      ▼
Transaction DB
```

This separation is the reason distributed transaction consistency becomes an important problem and motivates the use of Saga orchestration.

## 📁 Suggested Project Structure

```text
digital-banking-system/
│
├── account-service/
│   ├── src/main/java/
│   │   └── com/banking/account/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── enums/
│   │       ├── repository/
│   │       ├── service/
│   │       └── consumer/
│   └── src/main/resources/
│       └── application.yml
│
├── transaction-service/
│   ├── src/main/java/
│   │   └── com/banking/transaction/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── repository/
│   │       ├── service/
│   │       └── consumer/
│   └── src/main/resources/
│       └── application.yml
│
├── fraud-detection-service/
│   ├── src/main/java/
│   └── src/main/resources/
│
├── notification-service/
│   ├── src/main/java/
│   └── src/main/resources/
│
├── payment-service/
│   ├── src/main/java/
│   └── src/main/resources/
│
├── api-gateway/
│   ├── src/main/java/
│   └── src/main/resources/
│
└── docker-compose.yml
```

The tutorial creates six Spring Boot services and organizes the Account Service around controllers, DTOs, entities/enums, repositories, services, event consumers, and configuration. fileciteturn1file4L647-L703

## 🔗 Main Account Operations

The Account Service implements operations including:

```text
POST   /api/v1/accounts
GET    /api/v1/accounts/{accountNumber}
GET    /api/v1/accounts/{accountNumber}/balance
POST   /api/v1/accounts/{accountNumber}/block

POST   /api/v1/accounts/deduct
POST   /api/v1/accounts/credit
POST   /api/v1/accounts/refund
```

The transcript explicitly identifies create account, get account, get balance, block account, deduct balance, credit receiver, and credit sender/refund as the required Account Service operations. fileciteturn1file2L293-L347

> Verify exact paths against the source code before using these endpoints in production documentation.

## 🧪 Testing

The tutorial uses Postman for end-to-end API testing.

The first testing step is to create the sender and receiver accounts through the API Gateway. The gateway is the public entry point, so requests are sent to port `8080` rather than directly to individual internal services. fileciteturn2file8L1221-L1263

Recommended test scenarios:

### Account

- Create sender account
- Create receiver account
- Get account
- Get balance
- Block account

### Successful transfer

- Create sender and receiver
- Ensure sender has sufficient balance
- Initiate transfer
- Confirm transaction becomes `PROCESSING`
- Confirm fraud check passes
- Credit receiver
- Confirm debit/credit notifications

### Fraudulent transfer

- Initiate suspicious transaction
- Verify fraud event is generated
- Confirm transaction is stopped
- Verify sender account blocking behavior
- Verify notification is generated

### OTP

- Generate OTP
- Send OTP notification
- Verify valid OTP
- Test invalid OTP
- Test expired OTP

### Saga / Refund

- Deduct sender balance
- Force a downstream failure
- Confirm compensating action
- Confirm sender receives the refund
- Confirm transaction is marked appropriately

### Payment

- Test payment success
- Test payment failure
- Verify corresponding notification events

The tutorial concludes with end-to-end testing focused especially on transfer, real-time fraud detection, OTP, refund, Saga behavior, and notification events. fileciteturn1file9L1368-L1392

## ⚙️ Configuration

Each microservice should have its own configuration.

Example structure:

```yaml
spring:
  application:
    name: account-service

  datasource:
    url: jdbc:mysql://localhost:3306/account_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8081
```

Kafka and Redis configuration should also be supplied through environment-specific configuration.

**Do not commit real passwords, API keys, payment secrets, email credentials, or other sensitive values to GitHub.**

Use environment variables instead:

```bash
DB_USERNAME=your_username
DB_PASSWORD=your_password
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
REDIS_HOST=localhost
REDIS_PORT=6379
```

## ▶️ Running the Project

### 1. Clone the repository

```bash
git clone <your-repository-url>
cd digital-banking-system
```

### 2. Start infrastructure

```bash
docker compose up -d
```

Make sure Redis, Kafka, and Zookeeper are running.

### 3. Start MySQL

Ensure MySQL is running locally and create/configure the required databases.

### 4. Configure services

Update each service's `application.yml` or environment variables with:

- MySQL credentials
- Kafka bootstrap server
- Redis connection
- Service ports
- Payment gateway credentials
- Email configuration

### 5. Build services

From each Maven service:

```bash
./mvnw clean package
```

On Windows:

```bash
mvnw.cmd clean package
```

### 6. Start services

Start the services in an appropriate order:

```text
Infrastructure
   ↓
Account Service
   ↓
Transaction Service
   ↓
Fraud Detection Service
   ↓
Notification Service
   ↓
Payment Service
   ↓
API Gateway
```

### 7. Test through the Gateway

Use Postman and send requests through:

```text
http://localhost:8080
```

The tutorial uses the Gateway as the main entry point for account and transaction APIs. fileciteturn2file8L1233-L1263

## 📊 Transaction States

A transaction can be modeled around states such as:

```text
PROCESSING
     │
     ├── COMPLETED
     │
     ├── FAILED
     │
     ├── FLAGGED
     │
     └── REFUNDED
```

The tutorial explicitly stores an ongoing transaction as `PROCESSING` while the fraud and downstream steps are still being evaluated. fileciteturn2file9L1392-L1404

## 🎯 Key Learning Concepts

This project is particularly useful for learning:

- Microservices architecture
- Distributed transactions
- Saga pattern
- Saga orchestration
- Compensating transactions
- Event-driven architecture
- Apache Kafka
- Kafka consumers/producers
- Spring Cloud Gateway
- Redis rate limiting
- OpenFeign
- Spring Data JPA
- MySQL
- OTP verification
- Real-time fraud detection
- Payment gateway integration
- Notification systems
- Docker Compose
- End-to-end API testing

## 🔒 Important Production Considerations

This project is an educational implementation of a banking architecture. A real financial system would require substantially more controls.

Before considering a system production-ready, additional areas would need to be addressed, including:

- Strong authentication and authorization
- Encryption in transit and at rest
- Secrets management
- Idempotency for money-transfer requests
- Double-spending protection
- Concurrency control
- Audit trails
- Distributed tracing
- Dead-letter queues
- Kafka retry strategies
- Exactly-once/idempotent event handling where appropriate
- Database backups and disaster recovery
- Strong fraud models and risk scoring
- Compliance and regulatory requirements
- PCI/security requirements where applicable
- Monitoring and alerting
- High availability
- Load testing
- Formal security testing


## 🙌 Acknowledgements

This project follows the architecture and implementation concepts presented in the referenced digital banking system tutorial.

## 📄 License

Add the license appropriate for your repository.

For example:

```text
MIT License
```

If this repository contains your own implementation based on a tutorial, make sure your repository's license and attribution comply with the original tutorial/source's terms.
