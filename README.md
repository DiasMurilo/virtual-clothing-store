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

### Docker — check / stop / clear & run from scratch

Quick reference of useful Docker and Docker‑Compose commands for inspecting, stopping, cleaning and restarting the application from a clean state.

Inspect what's running

```bash
# List running containers
docker ps

# List all containers (including stopped)
docker ps -a

# Show services managed by docker-compose (project folder)
docker-compose ps

# Tail combined logs for compose services
docker-compose logs -f

# Tail logs for the app service only
docker-compose logs -f app

# Inspect a single container (detailed JSON)
docker inspect <container-name-or-id>
```

Stop / remove containers

```bash
# Stop a single container
docker stop <container-name-or-id>

# Stop all compose services
docker-compose stop

# Remove a stopped container
docker rm <container-name-or-id>

# Force remove (stop then remove)
docker rm -f <container-name-or-id>

# Remove all stopped containers (interactive)
docker container prune
```

Remove images, volumes and clean system (destructive)

```bash
# List images
docker image ls

# Remove an image
docker rmi <image-id-or-name>

# List volumes
docker volume ls

# Remove a volume
docker volume rm <volume-name>

# Remove unused volumes (interactive)
docker volume prune

# Aggressive cleanup (removes unused images, containers, networks, volumes)
docker system prune --all --volumes
# WARNING: the above is destructive — use with caution
```

Run from scratch (recommended sequence)

```bash
# 1) Stop and remove containers, networks and volumes created by compose
docker-compose down -v --remove-orphans

# 2) (optional) Remove images built by compose
docker-compose down --rmi all -v --remove-orphans

# 3) Rebuild and start fresh
docker-compose up -d --build

# 4) Check logs to ensure app started
docker-compose logs -f app
```

Quick project-specific commands

```bash
# Show compose status for this project
docker-compose ps

# Tail the Spring Boot application logs
docker-compose logs -f app

# Enter the running app container (shell)
docker exec -it <app-container-name> /bin/sh

# Connect to Postgres container (example)
docker exec -it virtual-clothing-store-db-1 psql -U postgres -d virtualclothingstore -c "SELECT version();"
```

**Safety note:** commands that prune or remove volumes/images will permanently delete data. Use `docker-compose down -v` only when you intend to remove the database volume or reset state.

### Option 2: Docker (Quick Testing)

For quick testing with H2 in-memory database (no external database required).

```bash
# Build the image
docker build -t virtual-clothing-store .

# Run the container
docker run -d -p 8080:8080 virtual-clothing-store

# Access the API
curl http://localhost:8080/
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

**Run Application:**

```bash
# Build and run
mvn clean install
mvn spring-boot:run

# Or run directly
mvn spring-boot:run
```

**Access:** `http://localhost:8080`

## Testing the API

Once running, test the endpoints:

```bash
# Check if API is running
curl http://localhost:8080/

# Get all categories
curl http://localhost:8080/api/categories

# Create a customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","phone":"+1234567890"}'
```

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
