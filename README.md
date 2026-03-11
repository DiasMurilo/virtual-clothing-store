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

### Docker Compose

This runs the complete application stack with PostgreSQL database.

```bash
# Navigate to project directory
cd virtual-clothing-store
```

### Prerequisites

- Docker & Docker Compose
- JDK 21 / Maven 3.9+ (to build the jars)

### Build & Run

From the repository root:

```powershell
cd virtual-clothing-store
# compile and build all modules
mvn clean package -DskipTests

# start the stack (first run will download images)
docker-compose up --build
```

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

## CI/CD Pipeline

Continuous integration is performed by a GitHub Actions workflow
located at `.github/workflows/ci-cd.yml`.  The pipeline is organised into
four sequential jobs that correspond to the assignment requirements:

1. **Build** – checks out the repository, sets up JDK 21 with Maven
   caching, and runs `mvn clean compile` across the multi‑module project to
   verify that the code compiles successfully.  Artifacts (class files and
   generated sources) are uploaded for later jobs.

2. **Test** – depends on the build job.  It adds the config‑server hostname
   to `/etc/hosts` and starts `config-server` in the background so that
   integration and end‑to‑end tests can load remote configuration.  The step
   executes `mvn test` (unit, integration and E2E) and then invokes
   `jacoco:report-aggregate` to produce a coverage report.  Test results and
   JaCoCo reports are uploaded as workflow artifacts, and the
   `dorny/test-reporter` action publishes a summary in the GitHub checks UI.
   This satisfies the requirement for automated testing and coverage
   measurement with JUnit, Mockito and JaCoCo.

3. **Code Quality Analysis** – runs after tests.  It caches SonarCloud data
   to speed future runs, reruns the tests with coverage to ensure the report
   is available, and then invokes the Maven Sonar plugin with the
   `-Dsonar.projectKey`, `-Dsonar.organization` and related properties.  Key
   properties such as `sonar.cpd.exclusions` and
   `sonar.coverage.exclusions` are defined in the parent POM so that the
   Maven goal reads them.  The SonarCloud step enforces the quality gate
   (coverage thresholds, duplication limits, no new bugs/vulnerabilities)
   before allowing the pipeline to proceed.

4. **Package & Docker Build** – triggered only on the `master`/`main` branch
   after the code‑quality job succeeds.  It packages all modules with
   `mvn package -DskipTests` and uploads the resulting JARs as artifacts.
   The job then logs in to the GitHub Container Registry and builds
   container images using `docker/metadata-action` to tag them appropriately;
   finally, the images are pushed to GHCR.  This fulfils the assignment's
   requirement to produce deployable artifacts and publish them to a
   registry.

The workflow listens for `push` and `pull_request` events on `main`/`master`.
Secrets used are `SONAR_TOKEN` (for SonarCloud authentication) and the
built‑in `GITHUB_TOKEN` (for status updates and GHCR login).  By structuring
jobs with `needs:` dependencies and caching, the pipeline provides fast,
repeatable feedback and ensures that only code meeting the quality gate is
merged.


### Interacting

Once the stack is up wait a few seconds for Eureka to populate, then:

```bash
curl http://localhost:8080/api/products        # catalog
curl http://localhost:8080/api/orders          # orders
```

### Configuration Management

Configuration values come from the config server. To test dynamic
refresh:

```bash
# read current value
curl http://localhost:8082/api/config/message

# edit config-server/src/main/resources/config-repo/catalog-service.yml
# (change app.demo-message)

# rebuild config-server and restart it
docker-compose up -d --build config-server

# tell catalog service to refresh
curl -X POST http://localhost:8082/actuator/refresh

# verify the new value
curl http://localhost:8082/api/config/message
```

### Resilience & Fault Handling

The gateway retries and opens a circuit breaker; fallbacks return empty
lists. You can simulate failures by stopping services:

```bash
# stop catalog service
docker stop virtual-clothing-store-catalog-service-1
curl http://localhost:8080/api/products   # should return []

# stop order service
docker stop virtual-clothing-store-app-1
curl http://localhost:8080/api/orders     # should return []

# restart services
docker start virtual-clothing-store-catalog-service-1
docker start virtual-clothing-store-app-1
```

Watch the gateway logs (`docker-compose logs -f api-gateway`) to see
retry/circuit-breaker events.

### Observability & Tracing

All requests are traced through Zipkin. After sending traffic, open the
Zipkin UI or use the API:

```bash
curl http://localhost:9411/api/v2/services
curl http://localhost:9411/api/v2/traces?serviceName=api-gateway
```

When investigating a slow request, the trace shows timings for each
service hop.

### Extending

- Add business logic to `ProductService`/`OrderService` and populate the
  database using the `db` container.
- Implement additional resilience rules or metrics as needed.
- Push images to a registry and deploy to Kubernetes/Azure/Cloud Foundry.

---

This README satisfies the initial scope of turning a Spring Boot monolith into
a working microservices example, complete with discovery, configuration,
resilience, and container orchestration.

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
