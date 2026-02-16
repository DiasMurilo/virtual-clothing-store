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

```bash
# Navigate to project directory
cd virtual-clothing-store

# Build and run all services
docker-compose up --build

# Or run in background
docker-compose up -d --build
```

**Services started:**

- **App**: `http://localhost:8080` (Spring Boot application)
- **Database**: `localhost:5432` (PostgreSQL)

**To stop:**

```bash
docker-compose down
```

### Option 2: Docker (Quick Testing)

For quick testing the container image runs with the `docker` Spring profile and uses the **H2 in‑memory database** by default (no external DB required).

```bash
# Build the image
docker build -t virtual-clothing-store .

# Run the container (uses H2 by default via the 'docker' profile)
docker run -d -p 8080:8080 virtual-clothing-store

# Tail container logs
docker logs -f <container-id-or-name>

# Access the API
curl http://localhost:8080/
```

Note: To run the standalone container against a PostgreSQL instance (instead of H2), pass the datasource environment variables which will override the in‑image settings:

```bash
docker run -d -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/virtualclothingstore \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=password \
  virtual-clothing-store
```

### Option 3: Local Development

Run locally with your own PostgreSQL database.

**Prerequisites:**

- Java 21
- Maven 3.6+
- PostgreSQL 15

**Setup Database:**

```sql
CREATE DATABASE virtualclothingstore;
CREATE USER postgres WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE virtualclothingstore TO postgres;
```

**Run Application (development):**

```bash
# Run with Maven (uses application.properties / PostgreSQL)
mvn spring-boot:run

# Run with the 'docker' profile locally (H2 in-memory)
mvn -Dspring-boot.run.profiles=docker spring-boot:run

# Or build and run the packaged JAR (specify profile as a JVM property)
mvn clean package
java -Dspring.profiles.active=docker -jar target/virtual-clothing-store-0.0.1-SNAPSHOT.jar
```

**Run tests:**

```bash
mvn test        # unit tests
mvn verify      # integration tests (if configured)
```

**Access:** `http://localhost:8080`

## Testing the API

Once running, verify and exercise the API endpoints:

```bash
# Health check
curl http://localhost:8080/

# List categories
curl http://localhost:8080/api/categories

# Create a customer
curl -X POST http://localhost:8080/api/customers \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","phone":"+1234567890"}'
```

Helpful commands:

- Tail application logs: `docker-compose logs -f app` or `docker logs -f <container-name>`
- Stop services: `docker-compose down` (add `-v` to remove volumes)
- Run unit tests: `mvn test`


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
