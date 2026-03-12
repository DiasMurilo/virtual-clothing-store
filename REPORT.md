# Continuous Build and Delivery – Assignment Report 2025/26

**Module:** Continuous Build and Delivery  
**Student:** Murilo Dias  
**Repository:** https://github.com/DiasMurilo/virtual-clothing-store  
**Branch:** master  

---

## Table of Contents

1. [Introduction to CI/CD](#1-introduction-to-cicd)
2. [User Stories](#2-user-stories)
3. [High-Level Architecture](#3-high-level-architecture)
4. [Test Strategy](#4-test-strategy)
5. [Pipeline Description](#5-pipeline-description)
6. [Evaluation and Reflection](#6-evaluation-and-reflection)
7. [Repository](#7-repository)

---

## 1. Introduction to CI/CD

### 1.1 Continuous Integration vs. Continuous Delivery

**Continuous Integration (CI)** is the practice of automatically building and verifying application code whenever a developer pushes a change to a shared repository. The core idea, first formalised by Martin Fowler and Kent Beck as part of Extreme Programming, is that integration bugs are cheapest to fix when discovered immediately after they are introduced (Fowler, 2006). In a CI workflow every commit triggers a pipeline that compiles the code, executes the automated test suite, and runs static analysis; the pipeline fails fast if any step breaks, giving the whole team instant feedback.

**Continuous Delivery (CD)** extends CI by ensuring the application is always in a releasable state. The pipeline automates everything up to—and sometimes including—deployment, so that promoting a build to production becomes a deliberate, low-risk business decision rather than a technical hurdle (Humble & Farley, 2010). Continuous Deployment (a stricter variant) goes one step further and releases to production automatically on every green build, eliminating the manual approval gate.

The relationship between the two is sequential: CI gates quality (the build and tests must pass) before CD packages and publishes the artefact. Together they embody the DevOps philosophy of shortening feedback loops, reducing integration risk, and enabling sustainable delivery velocity.

### 1.2 Current State-of-the-Art Practices and Tools

The industry has converged on a set of mature practices around CI/CD:

- **Pipeline-as-Code** — the pipeline definition is stored in the same repository as the application code (e.g. `.github/workflows/` for GitHub Actions, `Jenkinsfile` for Jenkins). This makes pipeline changes subject to code review and version history.
- **Immutable artefacts** — a binary (JAR, Docker image) built once is promoted through environments unchanged, eliminating environment-specific build surprises.
- **Quality gates** — automated pass/fail thresholds (minimum coverage, zero new critical bugs) enforced by tools such as SonarQube/SonarCloud before the pipeline advances.
- **Container-native delivery** — applications are packaged as OCI-compliant Docker images and deployed via orchestrators such as Kubernetes or cloud-managed container services.
- **Shift-left testing** — security scanning (e.g. Trivy, Snyk), dependency vulnerability checks, and code quality analysis occur early in the pipeline rather than at release time.

Widely used toolchains include:

| Layer | Tools |
|---|---|
| Version control | Git, GitHub, GitLab, Bitbucket |
| CI/CD server | GitHub Actions, GitLab CI, Jenkins, CircleCI |
| Build tool | Maven, Gradle, npm |
| Static analysis | SonarQube/SonarCloud, PMD, Checkstyle, SpotBugs |
| Coverage | JaCoCo, Istanbul, Cobertura |
| Containerisation | Docker, Buildah, Kaniko |
| Container registry | GitHub Container Registry (GHCR), Docker Hub, AWS ECR |
| Orchestration | Kubernetes, Docker Swarm, AWS ECS |
| Observability | Prometheus, Grafana, Zipkin, Jaeger |

### 1.3 References

- Fowler, M. (2006) *Continuous Integration*. Available at: https://martinfowler.com/articles/continuousIntegration.html (Accessed: 10 March 2026).
- Humble, J. and Farley, D. (2010) *Continuous Delivery: Reliable Software Releases through Build, Test, and Deployment Automation*. Upper Saddle River: Addison-Wesley.
- Kim, G., Humble, J., Debois, P. and Willis, J. (2016) *The DevOps Handbook*. Portland: IT Revolution Press.
- Forsgren, N., Humble, J. and Kim, G. (2018) *Accelerate: The Science of Lean Software and DevOps*. Portland: IT Revolution Press.
- SonarSource (2024) *SonarCloud Documentation*. Available at: https://docs.sonarsource.com/sonarcloud (Accessed: 10 March 2026).

---

## 2. User Stories

The following user stories describe the functional requirements of the Virtual Clothing Store REST API, written in Who/What/Why format with acceptance criteria.

---

**US-01 – Browse Products**

*As a* customer,  
*I want to* retrieve a list of all available products,  
*so that* I can choose items to include in my order.

**Acceptance Criteria:**
- `GET /api/products` returns HTTP 200 and a JSON array.
- Each product object contains at minimum: `id`, `name`, `price`, `categoryId`.
- An empty catalogue returns an empty array, not an error.
- The catalog-service remains accessible through the API gateway (`GET http://localhost:8080/api/products`).

---

**US-02 – View Product Details**

*As a* customer,  
*I want to* view the full details of a specific product by its identifier,  
*so that* I can decide whether to purchase it.

**Acceptance Criteria:**
- `GET /api/products/{id}` returns HTTP 200 and the product JSON when the product exists.
- Returns HTTP 404 with a meaningful message when the product does not exist.
- Response is consistent whether requests arrive directly to catalog-service or via the gateway.

---

**US-03 – Manage Categories**

*As a* store administrator,  
*I want to* create, update, and delete product categories,  
*so that* the catalogue remains organised and navigable.

**Acceptance Criteria:**
- `POST /api/categories` with a valid body creates a new category and returns HTTP 201.
- `PUT /api/categories/{id}` updates an existing category and returns HTTP 200.
- `DELETE /api/categories/{id}` removes a category and returns HTTP 204.
- Returns HTTP 404 when acting on a non-existent category.

---

**US-04 – Place an Order**

*As a* customer,  
*I want to* create an order containing one or more products,  
*so that* I can purchase items from the store.

**Acceptance Criteria:**
- `POST /api/orders` with a valid customer ID and item list creates an order and returns HTTP 201.
- Each order item records `productId`, `quantity`, and resolved `price`.
- Returns HTTP 400 when required fields are missing or invalid.
- Returns HTTP 404 when the referenced customer does not exist.

---

**US-05 – Track Orders**

*As a* customer,  
*I want to* list my orders and view individual order details,  
*so that* I can track the status and contents of my purchases.

**Acceptance Criteria:**
- `GET /api/orders` returns a paginated list of all orders.
- `GET /api/orders/{id}` returns a single order when it exists, HTTP 404 otherwise.
- `GET /api/orders/customer/{customerId}` returns all orders for a specific customer.
- Orders can be filtered by date range using `startDate` and `endDate` query parameters.

---

**US-06 – Manage Customers**

*As a* store administrator,  
*I want to* create and retrieve customer records,  
*so that* orders can be associated with known customers.

**Acceptance Criteria:**
- `POST /api/customers` creates a new customer and returns HTTP 201.
- `GET /api/customers/{id}` returns the customer when found, HTTP 404 otherwise.
- Customer data includes at minimum: `id`, `name`, `email`.

---

**US-07 – Service Resilience**

*As a* customer,  
*I want to* receive a graceful empty response rather than an error when a downstream service is unavailable,  
*so that* my browsing experience is not interrupted by temporary outages.

**Acceptance Criteria:**
- When catalog-service is down, `GET /api/products` via the gateway returns HTTP 200 with `[]`.
- When order-service is down, `GET /api/orders` via the gateway returns HTTP 200 with `[]`.
- Circuit breaker events are logged in the gateway.

---

**US-08 – Distributed Tracing**

*As a* developer,  
*I want to* trace a request across all microservices via Zipkin,  
*so that* I can diagnose latency and identify failing hops.

**Acceptance Criteria:**
- All services export trace spans to Zipkin at `http://localhost:9411`.
- A single user request generates a correlated trace visible in the Zipkin UI.
- `GET http://localhost:9411/api/v2/services` lists all participating service names.

---

## 3. High-Level Architecture

### 3.1 Overview

The Virtual Clothing Store is a **Spring Boot microservices application** composed of five independent services orchestrated with Docker Compose and communicating over an internal Docker network. Service discovery is managed by **Netflix Eureka**, and centralised external configuration is provided by **Spring Cloud Config Server**.

### 3.2 Services

| Service | Port (host) | Responsibility |
|---|---|---|
| `discovery-server` | 8761 | Eureka service registry |
| `config-server` | 8888 | Spring Cloud Config – serves YAML properties from classpath config-repo |
| `api-gateway` | 8080 | Spring Cloud Gateway – routing, retry, circuit breaker, fallback |
| `catalog-service` | 8082 | Products and Categories REST API; backed by PostgreSQL |
| `order-service` (alias `app`) | 8081 | Orders and Customers REST API; backed by PostgreSQL; Feign client to catalog-service |
| `db` | 5432 | PostgreSQL 15 – shared persistence |
| `zipkin` | 9411 | Distributed tracing |

### 3.3 Architecture Diagram

```
                        ┌─────────────────────────────────────────────────────┐
                        │                  Docker Network: app-network          │
                        │                                                       │
  Client                │  ┌─────────────────────────────────────────────────┐ │
  HTTP ──────────────►  │  │            API Gateway  :8080                   │ │
                        │  │  Spring Cloud Gateway                           │ │
                        │  │  • Route /api/products  → catalog-service       │ │
                        │  │  • Route /api/orders    → order-service         │ │
                        │  │  • Retry filter (3×)                            │ │
                        │  │  • CircuitBreaker → /fallback/**                │ │
                        │  └──────────┬────────────────────┬─────────────────┘ │
                        │             │                    │                    │
                        │  ┌──────────▼──────────┐  ┌─────▼────────────────┐  │
                        │  │   catalog-service    │  │    order-service     │  │
                        │  │   :8082              │  │    :8081             │  │
                        │  │  • ProductController │  │  • OrderController   │  │
                        │  │  • CategoryController│  │  • CustomerController│  │
                        │  │  • Eureka client     │  │  • Feign → catalog   │  │
                        │  │  • @RefreshScope     │  │  • Eureka client     │  │
                        │  └──────────┬───────────┘  └─────┬────────────────┘  │
                        │             │                     │                   │
                        │  ┌──────────▼─────────────────────▼───────────────┐  │
                        │  │              PostgreSQL (db) :5432              │  │
                        │  └─────────────────────────────────────────────────┘  │
                        │                                                        │
                        │  ┌────────────────────────┐  ┌────────────────────┐   │
                        │  │  discovery-server :8761 │  │ config-server :8888│   │
                        │  │  Netflix Eureka         │  │ Spring Cloud Config│   │
                        │  └────────────────────────┘  └────────────────────┘   │
                        │                                                        │
                        │  ┌────────────────────────┐                           │
                        │  │  Zipkin :9411           │  (all services export     │
                        │  │  Distributed Tracing    │   Micrometer spans)       │
                        │  └────────────────────────┘                           │
                        └─────────────────────────────────────────────────────┘
```

### 3.4 Key Architectural Decisions

**Service Discovery (Eureka):** All services register with the discovery server on startup. The gateway and Feign client resolve service addresses dynamically by name (`catalog-service`, `order-service`) without requiring hardcoded IPs.

**Centralised Configuration (Spring Cloud Config):** The Config Server serves `catalog-service.yml` and `order-service.yml` from a classpath config-repo. `@RefreshScope` on `ConfigController` allows properties to be reloaded at runtime via `POST /actuator/refresh` without restarting the container.

**Resilience patterns:** The gateway applies a `Retry` filter (3 attempts) and a `CircuitBreaker` filter backed by Resilience4j. When the circuit opens, requests are forwarded to `/fallback/api/products` or `/fallback/api/orders` which return empty HTTP 200 lists. The order-service also implements a `CatalogClientFallback` class registered on the `@FeignClient` annotation, returning empty lists when the catalog-service is unreachable.

**Container build:** A single multi-stage `Dockerfile` accepts a build argument `MODULE` (e.g. `--build-arg MODULE=catalog-service`) to select which Spring Boot application to package. Docker Compose sets this argument per service, reducing Dockerfile duplication. The Maven builder image compiles the full multi-module project; only the resulting fat JAR is copied to the runtime image.

---

## 4. Test Strategy

### 4.1 Approach

The testing strategy is aligned with the **Test Pyramid** (Fowler, 2012), which prescribes a large base of fast, isolated unit tests, a smaller middle layer of integration tests, and a thin top layer of end-to-end tests. This distribution minimises overall feedback time while maintaining confidence that the integrated system behaves correctly.

### 4.2 Test Pyramid Alignment

```
              ▲
             /E\        End-to-End Tests (OrderServiceE2ETest)
            /---\       • Real HTTP client (TestRestTemplate)
           / Intg\      • Full Spring context + H2 in-memory
          /-------\     Integration Tests (@WebMvcTest slices)
         /  Unit   \    • OrderControllerIntegrationTest
        /___________\   • Web layer loaded; service layer mocked
                        Unit Tests (majority)
                        • Pure JUnit 5 + Mockito, no Spring context
                        • Service, controller, entity, DTO, exception layers
```

| Level | Framework | Scope | Example Tests |
|---|---|---|---|
| Unit | JUnit 5 + Mockito | Single class in isolation | `OrderServiceUnitTest`, `CategoryServiceUnitTest`, `CustomerServiceUnitTest`, `ProductServiceUnitTest`, `OrderControllerUnitTest`, `CategoryControllerUnitTest`, `GlobalExceptionHandlerTest`, entity/DTO tests |
| Integration | JUnit 5 + `@WebMvcTest` + MockMvc | Web layer with mocked service | `OrderControllerIntegrationTest` |
| End-to-End | JUnit 5 + `@SpringBootTest` + `TestRestTemplate` | Full application context over HTTP | `OrderServiceE2ETest` |

### 4.3 Test Coverage Configuration

Coverage is measured by **JaCoCo 0.8.12**, configured in the parent POM. The `prepare-agent` execution instruments bytecode before the Surefire plugin runs tests. The key fix enabling correct coverage collection is the use of **late-binding argLine** in Surefire:

```xml
<argLine>@{argLine} -Dnet.bytebuddy.experimental=true</argLine>
```

Without `@{argLine}`, Surefire would replace the JaCoCo agent flag entirely, recording zero coverage. With it, JaCoCo's agent is prepended at build time and coverage data is written to each module's `target/jacoco.exec` file. The `report-aggregate` goal then merges all per-module reports into a single XML consumed by SonarCloud.

### 4.4 Quality Gate

SonarCloud quality gate enforcement:

| Condition | Threshold | Outcome |
|---|---|---|
| Test coverage on new code | ≥ 80% | Enforced |
| Duplicated lines on new code | ≤ 3% | Enforced via `sonar.cpd.exclusions` |
| New blocker/critical issues | 0 | Enforced |

Coverage and CPD exclusions are configured as Maven properties in `pom.xml` (not `sonar-project.properties`, which the Maven Sonar plugin ignores) so that the CI command `mvn -B verify sonar:sonar` correctly passes them to SonarCloud.

### 4.5 Test Tooling Justification

- **JUnit 5** – The de-facto standard for Java unit testing. Native support in Maven Surefire 3.x and excellent IDE integration.
- **Mockito (strict mode)** – Allows precise stubbing of collaborators, detects unnecessary stubs, and verifies interaction expectations without requiring Spring context startup time.
- **`@WebMvcTest`** – Spring slice test that loads only the web layer (controllers, filters, converters). Faster than a full `@SpringBootTest` and allows MockMvc assertions on HTTP-level behaviour.
- **`@SpringBootTest` + `TestRestTemplate`** – Boots the complete application on a random port without an external database by using `@AutoConfigureTestDatabase` with H2. Validates end-to-end HTTP request/response contracts.
- **JaCoCo** – integrates transparently with Maven without any changes to production code, produces both per-module and aggregated XML suitable for SonarCloud ingestion.

---

## 5. Pipeline Description

The project uses a **two-tier pipeline** that separates continuous integration (CI) from continuous deployment (CD):

- **CI tier — GitHub Actions** (`.github/workflows/ci-cd.yml`): triggered on every push or pull request to `main`/`master`. Four sequential jobs — Build → Test → Code Quality → Package — are chained with `needs:` so each job only begins when the previous one succeeds. A failure in any job terminates the pipeline and blocks the CD tier, satisfying the requirement for automatic failure propagation.
- **CD tier — Jenkins** (`Jenkinsfile`): a Jenkins pipeline running in a Docker container (with Docker socket mounted) polls SCM every two minutes. It first verifies that the GitHub Actions CI workflow has reached `success` on the current commit, then builds the Docker images locally and deploys the full stack with `docker compose`.

### 5.1 Stage 1 – Build

**Purpose:** Verify that all modules compile cleanly.

The runner checks out the repository with full Git history (required by SonarCloud's incremental analysis), sets up Eclipse Temurin JDK 21 with Maven dependency caching, and runs `mvn clean compile -B`. Compiled class files and generated sources are uploaded as workflow artifacts (retention 1 day) so they can be inspected if a subsequent job fails.

**Key configuration:**

```yaml
- uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: 'maven'

- run: mvn clean compile -B
```

**Tool choice:** Maven is the natural build tool for this Spring Boot multi-module project; its `pom.xml` inheritance model lets JaCoCo and Surefire be configured once in the parent and inherited by all five modules.

### 5.2 Stage 2 – Test

**Purpose:** Run the full test suite across all modules and produce a coverage report.

Because the catalog-service and order-service can optionally connect to Spring Cloud Config Server, the step injects a DNS alias `127.0.0.1 config-server` into `/etc/hosts` and starts the Config Server module in the background (`spring-boot:run ... &`) before executing `mvn test -B`. This ensures integration tests that load the Spring context do not fail due to missing configuration. A second Maven invocation generates the JaCoCo aggregate report. Test results (Surefire XML) and coverage data are uploaded as artifacts. The `dorny/test-reporter` action reads the JUnit XML reports and publishes a pass/fail summary directly into the GitHub Checks UI.

**Key configuration:**

```bash
# inject hostname so config-server is reachable during integration tests
sudo sh -c 'echo "127.0.0.1 config-server" >> /etc/hosts'

# start config server in background
mvn -pl config-server spring-boot:run -Dspring.profiles.active=native &
sleep 15

# run full test suite
mvn test -B

# generate aggregated JaCoCo report
mvn clean test jacoco:report-aggregate -B
```

**Tool choice:** `dorny/test-reporter` renders test results natively in GitHub without requiring a separate reporting server, keeping the tool stack self-contained within GitHub Actions.

### 5.3 Stage 3 – Code Quality Analysis

**Purpose:** Run static analysis and enforce the quality gate using SonarCloud.

The step disables shallow clone (`fetch-depth: 0`) so SonarCloud can calculate leak-period metrics accurately by comparing the current commit to the previous analysis. The SonarCloud cache (`~/.sonar/cache`) is restored between runs to avoid re-downloading rule sets. Tests are re-executed with `jacoco:report-aggregate` to ensure a fresh coverage XML exists, then the Sonar Maven plugin runs with the `SONAR_TOKEN` secret. All Sonar properties (`sonar.cpd.exclusions`, `sonar.coverage.jacoco.xmlReportPaths`, etc.) are declared in the parent POM `<properties>` block rather than in `sonar-project.properties` because the Maven plugin does not read the properties file.

**Key configuration:**

```yaml
- uses: actions/cache@v4
  with:
    path: ~/.sonar/cache
    key: ${{ runner.os }}-sonar

- run: mvn -B verify sonar:sonar
         -Dsonar.projectKey=DiasMurilo_virtual-clothing-store
         -Dsonar.organization=diasmurilo
         -Dsonar.host.url=https://sonarcloud.io
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

**POM quality gate properties:**

```xml
<sonar.qualitygate.wait>true</sonar.qualitygate.wait>
<sonar.cpd.exclusions>**/dto/**,**/entity/**,**/exception/**,**/repository/**</sonar.cpd.exclusions>
<sonar.coverage.exclusions>**/dto/**,**/entity/**,**/*Application.java</sonar.coverage.exclusions>
```

**Tool choice:** SonarCloud is the cloud-hosted version of SonarQube, requiring no self-hosted server. It integrates natively with GitHub, decorates pull requests with inline analysis comments, and stores historical quality metrics.

### 5.4 Stage 4 – Package and Docker Build

**Purpose:** Produce deployable artefacts (JARs and Docker images) and publish them to the GitHub Container Registry.

This job runs only on the `master` branch (`if: github.ref == 'refs/heads/master'`). It runs `mvn package -DskipTests -B` to produce all module fat JARs, uploads them as artifacts (7-day retention), logs in to GHCR using the built-in `GITHUB_TOKEN`, and builds/pushes a Docker image using `docker/build-push-action@v5`. The image is tagged with both the branch name and the short commit SHA using `docker/metadata-action`.

**Key configuration:**

```yaml
- run: mvn package -DskipTests -B

- uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}

- uses: docker/metadata-action@v5
  with:
    images: ghcr.io/${{ github.repository }}
    tags: |
      type=ref,event=branch
      type=sha
```

**Dockerfile strategy:** A single multi-stage `Dockerfile` accepts a `MODULE` build argument. The first stage uses `maven:3.9.6-eclipse-temurin-21` to compile the entire project and package the selected module. The second stage uses a slim JRE image and copies only the resulting JAR, producing a minimal runtime image. Docker Compose uses `build.args` to select the module per service.

**Tool choice:** GHCR requires no additional account beyond GitHub, is free for public repositories, and integrates with existing repository permissions. Tagging with commit SHA ensures every image is traceable back to an exact source commit.

### 5.5 Stage 5 – Jenkins: Package

**Purpose:** Build the Docker images on the deployment host after CI has been verified green.

Jenkins polls the repository every two minutes (`H/2 * * * *`). When a new commit is detected on `master`, the pipeline first queries the **GitHub Checks API** to confirm the GitHub Actions `CI` workflow has reached `success` on that commit. This gate prevents Jenkins from deploying a build that did not pass CI (e.g. a direct push that bypassed the normal CI trigger).

If the Docker Compose V2 CLI plugin is absent on the Jenkins container, the pipeline auto-installs it into `/var/jenkins_home/.docker/cli-plugins/` before proceeding. This eliminates the manual setup step and makes the pipeline self-healing on fresh Jenkins containers.

The Maven Wrapper then packages all five modules into fat JARs (tests are skipped — CI has already validated them), and Docker builds one image per microservice using the shared multi-stage `Dockerfile` with `--build-arg MODULE=<name>`.

**Jenkins startup (run once on the host):**

```powershell
# PowerShell – use backtick ` for line continuation
docker run -d --name jenkins `
  -p 8090:8080 -p 50000:50000 `
  -v jenkins_home:/var/jenkins_home `
  -v /var/run/docker.sock:/var/run/docker.sock `
  jenkins/jenkins:lts

# Restart existing container on subsequent runs
docker start jenkins
```

**Key Jenkinsfile commands:**

```bash
# --- GitHub Actions gate ---
COMMIT_SHA=$(git rev-parse HEAD)
STATUS=$(curl -sH "Authorization: token ${GITHUB_TOKEN}" \
  "https://api.github.com/repos/DiasMurilo/virtual-clothing-store/commits/${COMMIT_SHA}/check-runs" \
  | python3 -c "import sys,json; runs=json.load(sys.stdin)['check_runs']; \
    print('success' if all(r['conclusion']=='success' for r in runs if r['name']=='CI') else 'pending')")
[ "$STATUS" != "success" ] && echo "CI not green, aborting." && exit 1

# --- Docker Compose V2 auto-install ---
if ! docker compose version &>/dev/null; then
  mkdir -p /var/jenkins_home/.docker/cli-plugins
  curl -sSfL https://github.com/docker/compose/releases/download/v2.24.7/docker-compose-linux-x86_64 \
    -o /var/jenkins_home/.docker/cli-plugins/docker-compose
  chmod +x /var/jenkins_home/.docker/cli-plugins/docker-compose
fi

# --- Build JARs ---
mvn package -DskipTests -B

# --- Build Docker images ---
for MODULE in discovery-server config-server api-gateway catalog-service order-service; do
  docker build --build-arg MODULE=${MODULE} \
    -t ghcr.io/diasmurilo/virtual-clothing-store/${MODULE}:${BUILD_NUMBER} \
    -t ghcr.io/diasmurilo/virtual-clothing-store/${MODULE}:latest .
done
```

**Tool choice:** Jenkins was chosen for the CD tier because it integrates readily with Docker via socket mounting, provides SCM polling with configurable frequency, and supports Groovy declarative pipelines that can contain shell logic (such as the GitHub API gate). Running Jenkins as a Docker container removes the need for a separate server and keeps the entire system portable.

### 5.6 Stage 6 – Jenkins: Deploy

**Purpose:** Replace the running Docker Compose stack with the newly built images.

The deploy stage tears down the previous stack gracefully (using `|| true` to tolerate the case where no stack is running), then restarts all services in detached mode. After a 30-second warm-up pause, it queries `docker compose ps --status exited` to check whether any container has stopped. If any container exited, the stage prints the last 60 lines of logs and fails the build, triggering a Jenkins notification.

**Key Jenkinsfile commands:**

```bash
# Tear down previous stack (tolerates no previous stack)
docker compose down --remove-orphans || true

# Start all services detached
docker compose up -d

# Allow services to reach /actuator/health before checking
sleep 30
docker compose ps

# Fail the build if any service has exited
UNHEALTHY=$(docker compose ps --status exited -q | wc -l)
if [ "$UNHEALTHY" -gt "0" ]; then
  echo "One or more services failed to start:"
  docker compose logs --tail=60
  exit 1
fi
```

**Tool choice:** `docker compose` (V2 plugin) was used over the legacy `docker-compose` V1 binary because V2 is actively maintained and ships as part of Docker Desktop. The health-check strategy (`--status exited`) reliably distinguishes containers that failed to stay running from those still initialising, without requiring custom health endpoints or external tooling.

**Required Jenkins credentials and environment variables:**

| Variable | Source | Purpose |
|---|---|---|
| `GITHUB_TOKEN` | Jenkins credential (secret text) | GitHub Checks API gate |
| `GHCR_PAT` | Jenkins credential (ghcr-token) | Optional GHCR push from Jenkins |
| `BUILD_NUMBER` | Built-in Jenkins variable | Docker image version tag |

---

## 6. Evaluation and Reflection

### 6.1 Pipeline Execution Time

Based on observed runs, the typical wall-clock time for each stage is:

| Stage | Tier | Approximate Duration |
|---|---|---|
| 1 – Build | GitHub Actions | 2–3 min (cold cache ~5 min) |
| 2 – Test | GitHub Actions | 4–6 min (config-server startup adds ~15 s) |
| 3 – Code Quality | GitHub Actions | 3–5 min (SonarCloud analysis + network) |
| 4 – Package & Docker | GitHub Actions | 3–5 min (JAR packaging + GHCR image push) |
| 5 – Jenkins: Package | Jenkins | 2–4 min (GHA gate poll + mvn package + docker build ×5) |
| 6 – Jenkins: Deploy | Jenkins | 1–2 min (compose down + up + 30 s warm-up) |
| **Total (end-to-end)** | | **~15–25 minutes** |

The Maven dependency cache (`actions/setup-java cache: 'maven'`) and SonarCloud package cache (`~/.sonar/cache`) are the most significant speed improvements. Without caching, the Build stage alone would exceed 10 minutes on a cold runner due to downloading the Spring Boot parent POM dependency graph.

### 6.2 Automation Level

The pipeline is **fully automated** from commit to running containers. No manual intervention is required between a `git push` and a fully deployed stack. Test execution, coverage measurement, quality gate evaluation, JAR packaging, Docker image push (GHCR), and the full `docker compose` deployment are all performed without human input. The only intentional manual steps are the one-time Jenkins container creation and credential configuration, after which every subsequent deployment is triggered automatically by SCM polling.

### 6.3 Identified Trade-offs

**Trade-off 1: Speed vs. Coverage accuracy (JaCoCo re-execution)**

The Code Quality Analysis stage re-runs all tests with `mvn clean test jacoco:report-aggregate` even though the Test stage has already generated the coverage data. This duplicate execution adds 3–4 minutes to every pipeline run but was chosen to guarantee that SonarCloud receives a fresh, correct coverage XML. The alternative—uploading the JaCoCo report artifact from the Test stage and downloading it in the Code Quality stage—would require more complex artifact path handling and risks report staleness if a path mismatch occurs. The trade-off was therefore: **accuracy and simplicity over speed**.

**Trade-off 2: Coverage exclusions vs. Measured coverage breadth**

DTO classes, JPA entity classes, and `@SpringBootApplication` stubs are excluded from both coverage and CPD measurement (`sonar.coverage.exclusions`, `sonar.cpd.exclusions`). These files contain only Lombok-replaceable getter/setter boilerplate, JPA annotations, and trivial constructors. Testing them with reflection-heavy unit tests would inflate the test count without adding meaningful quality signal. The trade-off is: **a narrower but more meaningful coverage metric at the cost of some file visibility in SonarCloud**.

**Trade-off 3: Single Dockerfile with build arg vs. per-service Dockerfiles**

Using one Dockerfile with a `MODULE` argument reduces maintenance overhead but forces the entire multi-module Maven build to compile every time any module's image is built (because all modules must be compiled to resolve the selected module's JAR). Per-service Dockerfiles with module-scoped builds would be faster but significantly increase the number of files to maintain across five modules.

### 6.4 Suggested Improvements

1. **Separate JaCoCo report upload/download between stages.** Upload the `target/site/jacoco-aggregate/jacoco.xml` artifact at the end of the Test stage and download it at the start of the Code Quality stage. This eliminates the redundant full test re-run and would reduce total pipeline time by 3–4 minutes.

2. **Introduce a smoke-test stage after Jenkins deploy.** After `docker compose up -d` succeeds, a post-deploy stage could execute a small suite of HTTP smoke tests (e.g. with `curl`) against the running gateway. This would add deployment confidence beyond the container-exited health check currently used.

3. **Cache Docker layers.** The current pipeline does not use Docker layer caching. Adding `cache-from` / `cache-to` options in `docker/build-push-action` using GitHub Actions cache would reduce image build time, especially after minor source changes that leave the dependency layer unchanged.

4. **Replace the `sleep 15` config-server startup wait with a health-check poll.** The fixed `sleep 15` in the Test stage is fragile on slow runners. Replacing it with a `curl --retry` loop (`curl --retry 10 --retry-connrefused http://localhost:8888/actuator/health`) would make the wait adaptive and eliminate the risk of flaky test failures on under-resourced agents.

5. **Migrate to Dependabot or Renovate for dependency updates.** All Spring Boot, Spring Cloud, and plugin versions are pinned in the parent POM. Automated dependency update tools would create pull requests when new versions are released, keeping the dependency graph current without manual effort.

---

## 7. Repository

**GitHub Repository:** https://github.com/DiasMurilo/virtual-clothing-store  
**Branch:** `master`  
**Jenkins pipeline:** `Jenkinsfile` (project root)  

The repository is publicly accessible. The pipeline definition is at:  
`.github/workflows/ci-cd.yml`

All changes described in this report correspond to commits on the `master` branch. Key commit messages include:

| Commit Message | Content |
|---|---|
| `Tests added, duplication removed` | Initial test suite across all modules |
| `Additional unit tests for coverage` | FallbackController, DTO, Config/Discovery bean tests |
| `Boost coverage: catalog-service service/entity/DTO/exception tests and order-service DTO/entity tests` | Extended unit tests for service and entity layers |
| `Fix JaCoCo coverage reporting and extend tests for coverage/duplication goals` | `@{argLine}` Surefire fix; extended exception/controller/service tests |
| `Move Sonar CPD/coverage properties into POM for mvn sonar:sonar compatibility` | Moved all Sonar properties from `sonar-project.properties` into parent POM |
| `Document CI/CD pipeline steps and relation to assignment in README` | README pipeline documentation |

---

*End of Report*
