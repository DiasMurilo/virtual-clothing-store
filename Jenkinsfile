// ─────────────────────────────────────────────────────────────────────────────
// Jenkins CD Pipeline – Virtual Clothing Store
//
// Responsibility: Package → Deploy  (Continuous Delivery)
//
// Build, Test, and Code Quality (CI) are handled by the GitHub Actions workflow
// (.github/workflows/ci-cd.yml) which runs on every push and triggers this
// pipeline via the Jenkins REST API once all checks pass.
//
// This pipeline therefore NEVER re-runs the test suite; it only produces
// deployable artefacts and brings the stack up with the new versions.
// ─────────────────────────────────────────────────────────────────────────────
pipeline {
    agent any

    parameters {
        // SHA passed by the GitHub Actions trigger (informational only)
        string(name: 'GIT_SHA', defaultValue: '', description: 'Commit SHA that triggered this deployment')
    }

    environment {
        REGISTRY   = 'ghcr.io'
        IMAGE_NAME = 'diasmurilo/virtual-clothing-store'
    }

    tools {
        jdk   'JDK-21'
        maven 'Maven-3'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    stages {

        // ─────────────────────────────────────────────────────────────────────
        // Stage 1 – Package
        // Produces fat JARs with -DskipTests (CI already verified the tests).
        // Then builds a Docker image per module and optionally pushes to GHCR.
        // ─────────────────────────────────────────────────────────────────────
        stage('Package') {
            steps {
                echo "=== Stage 1: Packaging build #${BUILD_NUMBER} (commit ${params.GIT_SHA ?: 'manual'}) ==="

                // Ensure Docker CLI is available (install once if missing)
                sh '''
                    if ! command -v docker &> /dev/null; then
                        echo "Docker CLI not found – installing..."
                        apt-get update -qq && apt-get install -y docker.io
                        chmod 666 /var/run/docker.sock || true
                    fi
                '''

                // Produce all module JARs without re-running the test suite
                sh 'mvn package -DskipTests -B'

                // Attempt GHCR login (credential is optional – pipeline continues without it)
                script {
                    try {
                        withCredentials([string(credentialsId: 'ghcr-token', variable: 'GHCR_TOKEN')]) {
                            sh 'echo ${GHCR_TOKEN} | docker login ghcr.io -u diasmurilo --password-stdin'
                            env.GHCR_LOGGED_IN = 'true'
                        }
                    } catch (e) {
                        echo "ghcr-token credential not configured – images will be built locally only."
                        env.GHCR_LOGGED_IN = 'false'
                    }
                }

                // Build one image per module from the shared multi-stage Dockerfile
                sh '''
                    for MODULE in discovery-server config-server api-gateway catalog-service order-service; do
                        IMAGE=${REGISTRY}/${IMAGE_NAME}/${MODULE}
                        echo "--- Building ${IMAGE}:${BUILD_NUMBER} ---"
                        docker build \
                            --build-arg MODULE=${MODULE} \
                            -t ${IMAGE}:${BUILD_NUMBER} \
                            -t ${IMAGE}:latest \
                            .
                    done
                '''

                // Push to GHCR only when logged in
                script {
                    if (env.GHCR_LOGGED_IN == 'true') {
                        sh '''
                            for MODULE in discovery-server config-server api-gateway catalog-service order-service; do
                                IMAGE=${REGISTRY}/${IMAGE_NAME}/${MODULE}
                                docker push ${IMAGE}:${BUILD_NUMBER}
                                docker push ${IMAGE}:latest
                            done
                        '''
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: '**/target/*.jar,!**/target/*-sources.jar',
                                     allowEmptyArchive: true
                }
                failure { echo 'Package / Docker build FAILED – aborting pipeline.' }
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // Stage 2 – Deploy
        // Tears down the previous stack, brings up the freshly built images,
        // and verifies all containers reach a running state.
        // ─────────────────────────────────────────────────────────────────────
        stage('Deploy') {
            steps {
                echo '=== Stage 2: Deploying with Docker Compose ==='

                // Graceful shutdown of the previous version
                sh 'docker compose down --remove-orphans || true'

                // Bring the full stack up in detached mode using the images
                // built in the Package stage (--no-build skips redundant rebuilds)
                sh 'docker compose up -d'

                // Wait for services to stabilise, then assert no container exited
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
            node('built-in') {
                cleanWs()
            }
        }
    }
}
