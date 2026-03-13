# Virtual Clothing Store – Microservices CI/CD Demo

A Spring Boot microservices application for managing customers, products, orders, and categories in a virtual clothing store, built to demonstrate a complete **CI/CD pipeline** using GitHub Actions and Jenkins.

**Repository:** https://github.com/DiasMurilo/virtual-clothing-store  
**Branch:** `master`

---

## Technologies

| Layer             | Tool                                 |
| ----------------- | ------------------------------------ |
| Language          | Java 21                              |
| Framework         | Spring Boot 3.2, Spring Cloud 2023.x |
| Build             | Maven 3.9                            |
| Database          | PostgreSQL 15                        |
| Containerisation  | Docker, Docker Compose V2            |
| CI                | GitHub Actions                       |
| CD                | Jenkins (Docker container)           |
| Code Quality      | SonarCloud + JaCoCo                  |
| Tracing           | Zipkin                               |
| Service Discovery | Netflix Eureka                       |
| Config            | Spring Cloud Config Server           |

---

## Quick Start

```powershell
# Clone
git clone https://github.com/DiasMurilo/virtual-clothing-store.git
cd virtual-clothing-store

# Build JAR artefacts
mvn clean package -DskipTests

# Start all services (builds Docker images on first run)
docker compose up --build

# Or start detached
docker compose up -d --build

# Stop everything
docker compose down --remove-orphans
```

Wait ~30 seconds for Eureka to populate, then test:

```powershell
curl http://localhost:8080/api/products
curl http://localhost:8080/api/orders
```

## Prerequisites

- Docker Desktop (includes Docker Compose V2 — the `docker compose` plugin)
- JDK 21 + Maven 3.9+ (only needed for local builds outside Docker)

### Service ports

| Port | Service          | URL                   |
| ---- | ---------------- | --------------------- |
| 8761 | Eureka dashboard | http://localhost:8761 |
| 8888 | Config server    | http://localhost:8888 |
| 8080 | API Gateway      | http://localhost:8080 |
| 8081 | Order service    | http://localhost:8081 |
| 8082 | Catalog service  | http://localhost:8082 |
| 5432 | PostgreSQL       | localhost:5432        |
| 9411 | Zipkin tracing   | http://localhost:9411 |
| 8090 | Jenkins UI       | http://localhost:8090 |

Each JVM exposes `/actuator/health`; Docker Compose healthchecks use that endpoint so containers are marked healthy only when the application is up.

---

## Running Tests

```powershell
# Run all tests across all modules
mvn test

# Run all tests and generate aggregate JaCoCo coverage report
mvn test jacoco:report-aggregate -B
# Coverage report → target/site/jacoco-aggregate/index.html
```

## CI/CD Pipeline

The project uses a **two-tier pipeline**:

- **GitHub Actions** — CI: build → test → code quality → package & push Docker images to GHCR
- **Jenkins** — CD: waits for CI to pass → builds Docker images locally → deploys with `docker compose`

```
git push → GitHub Actions (CI)                    Jenkins (CD)
           ├── 1. Build    (mvn compile)          ├── 1. Package  (GHA gate + JARs + Docker images)
           ├── 2. Test     (JUnit + JaCoCo)        └── 2. Deploy   (docker compose up -d)
           ├── 3. Quality  (SonarCloud)
           └── 4. Package  (JARs + Docker → GHCR → trigger Jenkins)
```

### GitHub Actions (`.github/workflows/ci-cd.yml`)

**Stage 1 – Build**

Checks out the repository with full history (`fetch-depth: 0`), sets up Eclipse Temurin JDK 21 with Maven dependency caching, and compiles all five modules.

```yaml
- uses: actions/setup-java@v4
  with:
    java-version: "21"
    distribution: "temurin"
    cache: "maven"
- run: mvn clean compile -B
```

**Stage 2 – Test**

Runs the full test suite (unit, integration, E2E) and generates an aggregate JaCoCo coverage report. Test results are published as GitHub Checks via `dorny/test-reporter`.

```bash
mvn test jacoco:report-aggregate -B
```

**Stage 3 – Code Quality (SonarCloud)**

Runs the Sonar Maven plugin. The `SONAR_TOKEN` secret must be in repository settings. Enforces a quality gate (≥ 80 % coverage on new code, 0 new blocker issues).

```bash
mvn -B verify sonar:sonar \
  -DskipTests \
  -Dsonar.projectKey=DiasMurilo_virtual-clothing-store \
  -Dsonar.organization=diasmurilo \
  -Dsonar.host.url=https://sonarcloud.io
```

**Stage 4 – Package & Docker** _(master branch only)_

Packages all modules into fat JARs, builds one Docker image per microservice, pushes to GHCR tagged with the commit SHA and `:latest`, then triggers Jenkins via REST API.

```bash
mvn package -DskipTests -B

for MODULE in discovery-server config-server api-gateway catalog-service order-service; do
  IMAGE=ghcr.io/diasmurilo/virtual-clothing-store/${MODULE}
  docker build --build-arg MODULE=${MODULE} -t ${IMAGE}:${GITHUB_SHA} -t ${IMAGE}:latest .
  docker push ${IMAGE}:${GITHUB_SHA}
  docker push ${IMAGE}:latest
done

# Trigger Jenkins (requires JENKINS_URL secret)
curl -X POST "${JENKINS_URL}/job/virtual-clothing-store/buildWithParameters" \
  --user "${JENKINS_USER}:${JENKINS_API_TOKEN}" \
  --data-urlencode "GIT_SHA=${GITHUB_SHA}"
```

### Jenkins stages (`Jenkinsfile`)

**Requirement:** Jenkins must be running with the Docker socket mounted.

```powershell
# Start Jenkins (PowerShell – use backtick ` for line continuation, NOT backslash)
docker run -d --name jenkins `
  -p 8090:8080 -p 50000:50000 `
  -v jenkins_home:/var/jenkins_home `
  -v /var/run/docker.sock:/var/run/docker.sock `
  jenkins/jenkins:lts

# Or restart an existing container
docker start jenkins

# Get the initial admin password (first-time setup only)
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Jenkins UI: http://localhost:8090

**One-time Jenkins setup:**

```
Manage Jenkins → Tools:
  JDK:   name=JDK-21,   install from Adoptium, version 21
  Maven: name=Maven-3,  install automatically, version 3.9.x

Manage Jenkins → Credentials:
  ghcr-token  (Secret text) → GitHub PAT with packages:write scope

New Item → Pipeline:
  SCM: Git → https://github.com/DiasMurilo/virtual-clothing-store
  Branch: master
  Script path: Jenkinsfile
```

**Stage 1 – Package**

```bash
# Auto-installs Docker Compose V2 plugin if missing
mkdir -p /var/jenkins_home/.docker/cli-plugins
curl -sSfL https://github.com/docker/compose/releases/download/v2.24.7/docker-compose-linux-x86_64 \
  -o /var/jenkins_home/.docker/cli-plugins/docker-compose

# Builds all JARs (tests skipped – CI already validated)
mvn package -DskipTests -B

# Builds one Docker image per module
for MODULE in discovery-server config-server api-gateway catalog-service order-service; do
  docker build --build-arg MODULE=${MODULE} \
    -t ghcr.io/diasmurilo/virtual-clothing-store/${MODULE}:${BUILD_NUMBER} \
    -t ghcr.io/diasmurilo/virtual-clothing-store/${MODULE}:latest .
done
```

**Stage 2 – Deploy**

```bash
# Tear down previous stack
docker compose down --remove-orphans || true

# Start all services detached
docker compose up -d

# Health check: fail build if any container exited
UNHEALTHY=$(docker compose ps --status exited -q | wc -l)
if [ "$UNHEALTHY" -gt "0" ]; then
  docker compose logs --tail=60
  exit 1
fi
```

### Required GitHub repository secrets

| Secret              | Required | Purpose                            |
| ------------------- | -------- | ---------------------------------- |
| `SONAR_TOKEN`       | Yes      | SonarCloud analysis authentication |
| `GITHUB_TOKEN`      | Auto     | GHCR login + PR status (built-in)  |
| `JENKINS_URL`       | Optional | Trigger Jenkins via REST API       |
| `JENKINS_USER`      | Optional | Jenkins username for API trigger   |
| `JENKINS_API_TOKEN` | Optional | Jenkins API token for trigger      |

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
docker compose up -d --build config-server

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

Watch the gateway logs (`docker compose logs -f api-gateway`) to see retry/circuit-breaker events.

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
