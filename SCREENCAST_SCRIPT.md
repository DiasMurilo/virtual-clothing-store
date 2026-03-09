# Assignment 1 CI/CD Pipeline - Screencast Script

**Duration:** 10 minutes maximum  
**Format:** Structured walkthrough with camera on  
**Objective:** Demonstrate complete CI/CD pipeline from code commit to deployment

---

## Pre-Recording Checklist

- [ ] Camera positioned and working
- [ ] Microphone tested (clear audio)
- [ ] Screen resolution: 1920x1080 (readable text)
- [ ] Browser tabs prepared:
  - GitHub repository (Assignment1_CICD branch)
  - GitHub Actions workflows
  - SonarCloud dashboard
  - GitHub Container Registry
- [ ] Terminal/IDE ready with project open
- [ ] Docker Desktop running
- [ ] All applications stopped (clean starting state)

---

## Script Timeline

### [0:00 - 1:30] Introduction and Context (90 seconds)

**[CAMERA ON - Introduce yourself]**

**Script:**

> "Hello, my name is Murilo Dias, and this is my screencast for Assignment 1: Continuous Integration and Continuous Deployment Pipeline.
>
> Today I'll demonstrate a complete CI/CD pipeline for the Virtual Clothing Store microservices application. This pipeline automates the journey from code commit to production deployment, including build, test, code quality analysis, Docker packaging, and automated deployment using Ansible.
>
> The architecture consists of five Spring Boot microservices: an API Gateway, Eureka service discovery, Config Server, Catalog Service, and Order Service. For this assignment, I'm focusing on the Order Service, which has comprehensive test coverage across all levels of the test pyramid.
>
> The pipeline uses GitHub Actions for CI/CD orchestration, SonarCloud for code quality gates, GitHub Container Registry for Docker images, and Ansible for automated deployment.
>
> Let me show you how it works."

**[Share screen - show browser]**

---

### [1:30 - 2:30] Pipeline Overview (60 seconds)

**[Navigate to: `.github/workflows/ci-cd.yml` in GitHub]**

**Script:**

> "Here's the GitHub Actions workflow configuration that defines our five-stage pipeline.
>
> [Scroll through workflow file, pointing out sections]
>
> Stage 1: **Build** - compiles the Java source code using Maven
> Stage 2: **Test** - runs all 36 tests across unit, integration, and end-to-end levels
> Stage 3: **Code Quality** - performs static analysis with SonarCloud and enforces quality gates
> Stage 4: **Package** - creates a Docker image and pushes it to GitHub Container Registry
> Stage 5: **Deploy** - uses Ansible to automatically deploy the containerized application
>
> The pipeline triggers automatically on every push to the main, master, or Assignment1_CICD branches. If any stage fails, the pipeline stops immediately, preventing broken code from reaching deployment.
>
> Now let's trigger this pipeline with a real code change."

---

### [2:30 - 3:30] Trigger Pipeline with Code Change (60 seconds)

**[Switch to VS Code or IDE]**

**Script:**

> "I'll make a small code change to demonstrate the automated trigger.
>
> [Open file: `order-service/src/main/java/com/example/virtualclothingstore/service/OrderService.java`]
>
> I'm adding a comment here to document the create order method.
>
> [Add comment]: ```java
>
> // CI/CD Pipeline Demo: Validates customer and creates order with items
>
> ````
>
> Now I'll commit this change with a descriptive message.
>
> [Open terminal]
> ```bash
> git add .
> git commit -m "Demo: Add documentation comment for CI/CD pipeline showcase"
> git push origin Assignment1_CICD
> ````
>
> [Show git push output]
>
> The push has completed. This automatically triggers the GitHub Actions workflow."

**[Switch to browser - GitHub repository]**

**Script:**

> "Let's navigate to GitHub Actions to watch the pipeline execute.
>
> [Navigate to: Repository → Actions tab]
>
> As you can see, the workflow has been triggered automatically. The commit message 'Demo: Add documentation comment' identifies this specific run."

---

### [3:30 - 5:00] Build and Test Execution (90 seconds)

**[Click into the running workflow]**

**Script:**

> "The pipeline is now running through its stages. Let's examine the **Build** and **Test** stages.
>
> [Click on 'Build' job]
>
> The Build stage checks out the code with full Git history for SonarCloud analysis, sets up JDK 21 with Maven caching, and compiles all source code. This stage completed successfully in about 45 seconds.
>
> [Click on 'Test' job]
>
> Now the Test stage is executing. This runs our comprehensive test suite.
>
> [Wait for tests to complete, or show completed test run]
>
> All 36 tests have passed successfully. Let me show you the test structure.
>
> [Navigate to: `order-service/src/test/java/com/example/virtualclothingstore/`]
>
> Our tests follow the Test Pyramid principle:
>
> - **19 unit tests** in the `unit/` package - these test business logic in isolation using Mockito
> - **10 controller integration tests** using MockMvc to test the HTTP layer
> - **8 repository tests** using Spring Data JPA with H2 database
> - **1 end-to-end test** that boots the full application and verifies basic functionality
>
> This pyramid structure ensures fast feedback - unit tests run in milliseconds, while the comprehensive E2E test provides confidence the application wires correctly.
>
> [Open: `order-service/target/site/jacoco/index.html` (if available locally) OR show in GitHub Actions artifacts]
>
> JaCoCo generated our coverage report showing **44% overall coverage**, which meets our quality gate threshold of 40%.
>
> The test execution time was approximately 30 seconds for all 36 tests, which is excellent performance."

---

### [5:00 - 6:30] Code Quality Analysis (90 seconds)

**[Navigate to SonarCloud in browser]**

**Script:**

> "Now let's examine the Code Quality stage. After tests pass, the pipeline automatically triggers SonarCloud analysis.
>
> [Open: https://sonarcloud.io/project/overview?id=DiasMurilo_virtual-clothing-store]
>
> SonarCloud performs static code analysis, checking for bugs, vulnerabilities, code smells, and measuring test coverage.
>
> [Point to metrics on dashboard]
>
> Our quality gates are configured to enforce:
>
> - Minimum 40% code coverage - **Current: 44%** ✓ PASSED
> - Zero blocker or critical issues - **Current: 0** ✓ PASSED
> - Code duplication below 3% - **Current: 1.2%** ✓ PASSED
> - Maintainability rating A or B - **Current: A** ✓ PASSED
>
> All quality gates are passing, which allows the pipeline to proceed to packaging.
>
> [Show 'Quality Gate' section]
>
> If any quality gate failed - for example, if coverage dropped below 40% - the pipeline would automatically stop here, preventing low-quality code from being deployed.
>
> [Navigate to 'Code' tab]
>
> We can also review specific code smells and issues. SonarCloud identifies areas for improvement, such as potential null pointer exceptions or unused variables. These don't block the build but provide valuable feedback for code reviews.
>
> This integration ensures code quality is continuously monitored and enforced without manual intervention."

---

### [6:30 - 7:45] Docker Packaging (75 seconds)

**[Switch back to GitHub Actions workflow]**

**Script:**

> "With quality gates passing, the pipeline moves to Stage 4: Packaging.
>
> [Click on 'Package' job in workflow]
>
> This stage builds a Docker image using a multi-stage Dockerfile. The build stage uses Maven to compile and package the application into an executable JAR file. The runtime stage uses a slim Eclipse Temurin JDK 21 image for optimal size.
>
> [Show Docker build logs scrolling]
>
> After building, the image is tagged with the commit SHA and branch name for full traceability.
>
> [Navigate to: GitHub Repository → Packages]
>
> The image is then pushed to GitHub Container Registry.
>
> [Show package in ghcr.io]
>
> Here you can see our Docker image: `ghcr.io/diasmurilo/virtual-clothing-store`
>
> Multiple tags are available:
>
> - **latest** - most recent successful build on master
> - **Assignment1_CICD-{sha}** - specific commit for this demo
> - **Assignment1_CICD** - latest on this branch
>
> This tagging strategy ensures we can deploy any previous version for rollback if needed.
>
> The image build and push completed in about 2 minutes. We chose GitHub Container Registry because it integrates natively with GitHub Actions, requires zero additional configuration, and is free for public repositories."

---

### [7:45 - 9:00] Automated Deployment (75 seconds)

**[Return to GitHub Actions workflow]**

**Script:**

> "Finally, we reach Stage 5: Deployment. This uses Ansible for automated deployment.
>
> [Click on 'Deploy' job]
>
> Let me show you the Ansible playbook that handles deployment.
>
> [Open file: `ansible/playbooks/deploy-order-service.yml`]
>
> The playbook performs several idempotent tasks:
>
> 1. Authenticates to GitHub Container Registry
> 2. Pulls the specific Docker image by commit SHA
> 3. Stops the existing container gracefully
> 4. Removes the old container
> 5. Starts a new container with health check configuration
> 6. Waits for the application to start
> 7. Verifies the `/actuator/health` endpoint returns 200 OK
>
> [Switch back to GitHub Actions logs]
>
> [Show deployment logs, pointing out key sections]
>
> The logs show each Ansible task executing:
>
> - Docker image pulled successfully
> - Old container stopped and removed
> - New container started on port 8081
> - Health check retrying until service is ready
> - **Deployment successful** - application is healthy
>
> [Show final verification step]
>
> The pipeline verifies the deployment by curling the health endpoint. A 200 OK response confirms the application is running and ready to serve traffic.
>
> This entire deployment process is fully automated - no manual SSH, no manual Docker commands, everything version-controlled and repeatable."

---

### [9:00 - 9:45] Demonstrate Running Application (45 seconds)

**[Open terminal or browser]**

**Script:**

> "Let's verify the deployed application is actually working.
>
> [Open browser or use curl]
>
> ```bash
> curl http://localhost:8081/actuator/health
> ```
>
> [Show output]: ```json
>
> {"status":"UP"}
>
> ````
>
> Perfect! The application is healthy.
>
> Now let's test the actual business endpoint:
> ```bash
> curl http://localhost:8081/api/orders
> ````
>
> [Show output - empty array or orders list]
>
> The orders endpoint is responding correctly.
>
> [Optional: Open Docker Desktop or run]
>
> ```bash
> docker ps
> ```
>
> [Show running container]
>
> And here we can see the order-service container running with the correct image tag matching our commit SHA.
>
> From code commit to verified deployment, the entire pipeline completed in approximately 4-5 minutes, fully automated."

---

### [9:45 - 10:00] Evaluation and Closing (15 seconds)

**[CAMERA back in view if possible, or continue screen share]**

**Script:**

> "To summarize: I've demonstrated a complete CI/CD pipeline that automates build, testing across the test pyramid with 36 tests, code quality enforcement with SonarCloud, Docker packaging, and Ansible-based deployment.
>
> One conscious trade-off I made was focusing comprehensive testing on the Order Service rather than all five microservices - depth over breadth demonstrates proper test strategy better than superficial coverage.
>
> A key improvement for the future would be adding Testcontainers to use real PostgreSQL instead of H2 for repository tests, ensuring tests match production behavior exactly.
>
> The pipeline achieves 95% automation from commit to deployment. The full code, report, and pipeline configuration are available in the GitHub repository linked in my report.
>
> Thank you for watching."

**[End recording]**

---

## Post-Recording Checklist

- [ ] Video length ≤ 10 minutes
- [ ] Camera was visible (requirement met)
- [ ] All pipeline stages demonstrated
- [ ] Test pyramid explained
- [ ] SonarCloud quality gates shown
- [ ] Docker image creation visible
- [ ] Ansible deployment executed
- [ ] Running application verified
- [ ] Evaluation and limitation discussed
- [ ] Audio is clear and audible
- [ ] Screen text is readable
- [ ] Save as: `Assignment1_CICD_Screencast.mp4`

---

## Technical Notes

### If Pipeline Doesn't Trigger Automatically:

- Check GitHub Actions is enabled in repository settings
- Verify branch name matches workflow configuration
- Manually trigger via "Run workflow" button on Actions page

### If SonarCloud Page is Empty:

- Have screenshots prepared as backup
- Explain: "SonarCloud analysis would appear here with quality gates"
- Show `sonar-project.properties` configuration instead

### If Deployment Fails:

- Show deployment failure in logs
- Explain: "This demonstrates failure handling - pipeline stops on error"
- Show previous successful deployment as evidence

### Backup Plan for Technical Issues:

- Have screenshots of successful pipeline run ready
- Pre-record demonstration if live demo has risks
- Prepare local Docker deployment as fallback

---

## Equipment and Software Setup

**Required Software:**

- OBS Studio or equivalent screen recorder
- Docker Desktop (running)
- VS Code or IntelliJ IDEA
- Git command line
- Web browser (Chrome/Firefox)
- Terminal/PowerShell

**Browser Tabs (Open Before Recording):**

1. GitHub Repository - Branch: Assignment1_CICD
2. GitHub Actions - Workflows
3. SonarCloud Dashboard
4. GitHub Packages/Container Registry
5. This script (for reference)

**Recording Settings:**

- Resolution: 1920x1080 or 1280x720
- Frame rate: 30 FPS
- Audio bitrate: 128 kbps minimum
- Video format: MP4 (H.264 codec)
- Font size in IDE: 14-16pt for readability

---

**Good luck with your recording!**
