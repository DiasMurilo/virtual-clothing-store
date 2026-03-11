pipeline {
    agent any

    environment {
        // SonarCloud – add a Jenkins Secret Text credential with id 'sonar-token'
        SONAR_TOKEN       = credentials('sonar-token')
        // GitHub Container Registry (optional – only used in Package stage)
        REGISTRY          = 'ghcr.io'
        IMAGE_NAME        = 'diasmurilo/virtual-clothing-store'
        GHCR_TOKEN        = credentials('ghcr-token')   // GitHub PAT with packages:write
    }

    tools {
        // Names must match what you configured under Jenkins → Global Tool Configuration
        jdk   'JDK-21'
        maven 'Maven-3'
    }

    options {
        // Keep last 10 builds to save disk space
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Fail the build if it takes longer than 30 minutes
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    stages {

        // ─────────────────────────────────────────────────────────────────────
        // Stage 1 – Build
        // ─────────────────────────────────────────────────────────────────────
        stage('Build') {
            steps {
                echo '=== Stage 1: Compiling all modules ==='
                sh 'mvn clean compile -B'
            }
            post {
                failure { echo 'Build FAILED – aborting pipeline.' }
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // Stage 2 – Test
        // ─────────────────────────────────────────────────────────────────────
        stage('Test') {
            steps {
                echo '=== Stage 2: Running unit, integration and E2E tests ==='

                // Make config-server reachable by hostname during integration tests
                sh 'echo "127.0.0.1 config-server" | sudo tee -a /etc/hosts'

                // Start the config server in the background
                sh '''
                    mvn -pl config-server spring-boot:run \
                        -Dspring.profiles.active=native &
                    sleep 15
                '''

                // Run the full test suite
                sh 'mvn test -B'

                // Generate aggregated JaCoCo report
                sh 'mvn clean test jacoco:report-aggregate -B'
            }
            post {
                always {
                    // Publish JUnit results so Jenkins shows pass/fail per test
                    junit allowEmptyResults: true,
                          testResults: '**/target/surefire-reports/TEST-*.xml'

                    // Publish JaCoCo coverage report
                    jacoco(
                        execPattern:         '**/target/jacoco.exec',
                        classPattern:        '**/target/classes',
                        sourcePattern:       '**/src/main/java',
                        exclusionPattern:    '**/dto/**,**/entity/**,**/*Application.class',
                        minimumLineCoverage: '70'
                    )
                }
                failure { echo 'Tests FAILED – aborting pipeline.' }
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // Stage 3 – Code Quality (SonarCloud)
        // ─────────────────────────────────────────────────────────────────────
        stage('Code Quality') {
            steps {
                echo '=== Stage 3: SonarCloud analysis ==='
                sh '''
                    mvn -B verify sonar:sonar \
                        -Dsonar.projectKey=DiasMurilo_virtual-clothing-store \
                        -Dsonar.organization=diasmurilo \
                        -Dsonar.host.url=https://sonarcloud.io \
                        -Dsonar.token=${SONAR_TOKEN} \
                        -Dsonar.qualitygate.wait=true
                '''
            }
            post {
                failure { echo 'Quality gate FAILED – aborting pipeline.' }
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // Stage 4 – Package & Docker Build
        // ─────────────────────────────────────────────────────────────────────
        stage('Package') {
            steps {
                echo '=== Stage 4: Packaging JARs and building Docker images ==='

                sh 'mvn package -DskipTests -B'

                // Log in to GitHub Container Registry
                sh 'echo ${GHCR_TOKEN} | docker login ghcr.io -u diasmurilo --password-stdin'

                // Build and push one image per module
                sh '''
                    for MODULE in discovery-server config-server api-gateway catalog-service order-service; do
                        IMAGE=${REGISTRY}/${IMAGE_NAME}/${MODULE}
                        docker build \
                            --build-arg MODULE=${MODULE} \
                            -t ${IMAGE}:${BUILD_NUMBER} \
                            -t ${IMAGE}:latest \
                            .
                        docker push ${IMAGE}:${BUILD_NUMBER}
                        docker push ${IMAGE}:latest
                    done
                '''
            }
            post {
                always {
                    // Archive the fat JARs so they can be downloaded from the build page
                    archiveArtifacts artifacts: '**/target/*.jar,!**/target/*-sources.jar',
                                     allowEmptyArchive: true
                }
                failure { echo 'Package / Docker build FAILED – aborting pipeline.' }
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // Stage 5 – Deploy (local Docker Compose)
        // Runs automatically only after all previous stages succeed.
        // Requires the Jenkins agent to have Docker + Compose available.
        // ─────────────────────────────────────────────────────────────────────
        stage('Deploy') {
            steps {
                echo '=== Stage 5: Deploying with Docker Compose ==='

                // Tear down any previously running stack gracefully
                sh 'docker compose down --remove-orphans || true'

                // Bring the full stack up (detached)
                sh 'docker compose up -d --build'

                // Wait for services to stabilise, then verify
                sh '''
                    echo "Waiting 30 s for containers to start..."
                    sleep 30
                    docker compose ps

                    UNHEALTHY=$(docker compose ps --status exited -q | wc -l)
                    if [ "$UNHEALTHY" -gt "0" ]; then
                        echo "One or more containers exited unexpectedly:"
                        docker compose logs --tail=60
                        exit 1
                    fi
                    echo "All services are running successfully."
                '''
            }
            post {
                success { echo 'Deployment SUCCESSFUL – stack is live on localhost.' }
                failure { echo 'Deployment FAILED – check docker compose logs above.' }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Global post actions
    // ─────────────────────────────────────────────────────────────────────────
    post {
        success {
            echo """
╔══════════════════════════════════════════╗
║  Pipeline PASSED – build #${BUILD_NUMBER}  ║
║  Services are live:                      ║
║    API Gateway  → http://localhost:8080  ║
║    Catalog      → http://localhost:8082  ║
║    Order        → http://localhost:8081  ║
║    Eureka       → http://localhost:8761  ║
║    Zipkin       → http://localhost:9411  ║
╚══════════════════════════════════════════╝
"""
        }
        failure {
            echo 'Pipeline FAILED – review the stage that turned red above.'
        }
        always {
            cleanWs()   // clean workspace after every run
        }
    }
}
