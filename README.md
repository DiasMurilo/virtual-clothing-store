# Virtual Clothing Store - REST API

A Spring Boot REST API for managing customers, products, orders, and categories in a virtual clothing store.

## Technologies

- Java 21
- Spring Boot 3.2.0
- PostgreSQL
- Docker

## Quick Start

1. **Run with Docker Compose:**

   ```bash
   docker-compose up --build
   ```

2. **Access the API:**
   - Base URL: `http://localhost:8080`

## Running the Application

### Option 1: Docker Compose (Recommended)

This runs the complete application stack with PostgreSQL database.

````bash
# Navigate to project directory
cd virtual-clothing-store
This repository demonstrates a **Spring Cloud microservice architecture** adapted
from a single monolithic application.  It includes:

* **Discovery server** (Eureka)
* **Config server** with externalized configuration
* **API Gateway** (Spring Cloud Gateway) with routing, retries and circuit
  breakers
* **Catalog service** and **Order service** as independent Spring Boot
  applications
* **Feign clients** with fallback for inter‑service communication
* **Resilience4j** circuit breaker on the client side
* **Tracing/metrics** via Micrometer and Zipkin
* **PostgreSQL** database container with health checks
* **Docker Compose** orchestration for local development

## Getting Started

### Prerequisites

* Docker & Docker Compose
* JDK 21 / Maven 3.9+ (to build the jars)

### Build & Run

From the repository root:

```powershell
cd virtual-clothing-store
# compile and build all modules
mvn clean package -DskipTests

# start the stack (first run will download images)
docker-compose up --build
````

Service ports:

- `8761` – Eureka UI
- `8888` – Config server
- `8080` – API gateway
- `8081` – Order service (alias `app`)
- `8082` – Catalog service
- `9411` – Zipkin

Each JVM exposes `/actuator/health` and Docker health‑checks use that
endpoint; the compose file includes `healthcheck:` entries so containers
are marked healthy only when the application is up.

Health endpoints are exposed by Spring Boot Actuator; the compose file includes
`healthcheck` rules for the gateway, catalog and order service.

### Testing

Each service includes a small unit test. Run:

```bash
mvn test
```

or execute the catalog‑service test directly:

```bash
cd catalog-service
mvn test
```

### Interacting

Once the stack is up wait a few seconds for Eureka to populate, then:

```bash
curl http://localhost:8080/api/products        # catalog
curl http://localhost:8080/api/orders          # orders
```

The gateway automatically retries failed downstream requests and applies a
circuit breaker; fallback endpoints (under `/fallback/...`) return empty lists when
a service is unavailable. Order‑service uses a Feign client with a Resilience4j
circuit breaker and a fallback implementation as well.

### Extending

- Add business logic to `ProductService`/`OrderService` and populate the
  database using the `db` container.
- Implement additional resilience rules or metrics as needed.
- Push images to a registry and deploy to Kubernetes/Azure/Cloud Foundry.

---

This README satisfies the initial scope of turning a Spring Boot monolith into
a working microservices example, complete with discovery, configuration,
resilience, and container orchestration.

# Build and run all services

docker-compose up --build

# Or run in background

docker-compose up -d --build

````

**Services started:**

- **App**: `http://localhost:8080` (Spring Boot application)
- **Database**: `localhost:5432` (PostgreSQL)

### Docker — check / stop / clear & run from scratch

```bash
# 1) Stop and remove containers, networks and volumes created by compose
docker-compose down -v --remove-orphans

# 2) (optional) Remove images built by compose
docker-compose down --rmi all -v --remove-orphans

# 3) Rebuild and start fresh
docker-compose up -d --build

# 4) Check logs to ensure app started
docker-compose logs -f app
````

## API Endpoints

### Customers

- `GET /api/customers` - Get all customers (paginated)
- `GET /api/customers/{id}` - Get customer by ID
- `POST /api/customers` - Create customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

### Products

- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Categories

- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

### Orders

- `GET /api/orders` - Get all orders (paginated, optional date filter)
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/customer/{customerId}` - Get orders by customer
- `POST /api/orders?customerId={id}` - Create order
- `PUT /api/orders/{orderId}/products` - Add product to order
- `DELETE /api/orders/{orderId}/products/{productId}` - Remove product from order
- `PUT /api/orders/{id}` - Update order
- `DELETE /api/orders/{id}` - Delete order
