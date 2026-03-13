// Jenkins CD Pipeline – Virtual Clothing Store
// Stages: Package → Deploy (CI handled by GitHub Actions)
pipeline {
    agent any

    parameters {
        // SHA passed by the GitHub Actions trigger (informational only)
        string(name: 'GIT_SHA', defaultValue: '', description: 'Commit SHA that triggered this deployment')
    }

    environment {
        REGISTRY   = 'ghcr.io'
        IMAGE_NAME = 'diasmurilo/virtual-clothing-store'
        GITHUB_REPO = 'DiasMurilo/virtual-clothing-store'
        BRANCH      = 'master'
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

    // Poll SCM every 2 minutes; CI gate check in Package stage ensures GHA passed first.
    triggers {
        pollSCM('H/2 * * * *')
    }

    stages {

        stage('Package') {
            steps {
                echo "=== Stage 1: Packaging build #${BUILD_NUMBER} (commit ${params.GIT_SHA ?: 'manual'}) ==="

                // Wait for GitHub Actions CI to pass before deploying
                sh '''
                    SHA=$(git rev-parse HEAD)
                    echo "Waiting for GitHub Actions to pass for commit ${SHA}..."
                    for i in $(seq 1 40); do
                        CONCLUSION=$(curl -s \
                          "https://api.github.com/repos/${GITHUB_REPO}/actions/runs?head_sha=${SHA}&branch=${BRANCH}&per_page=5" \
                          -H "Accept: application/vnd.github.v3+json" \
                          | python3 -c "
import sys, json
data = json.load(sys.stdin)
runs = data.get('workflow_runs', [])
if not runs:
    print('pending')
    sys.exit(0)
latest = runs[0]
status = latest.get('status', '')
conclusion = latest.get('conclusion', '')
if status == 'completed':
    print(conclusion)
else:
    print('pending')
")
                        echo "Attempt ${i}/40 – GHA status: ${CONCLUSION}"
                        if [ "${CONCLUSION}" = "success" ]; then
                            echo "GitHub Actions passed – proceeding with deployment."
                            break
                        elif [ "${CONCLUSION}" = "failure" ] || [ "${CONCLUSION}" = "cancelled" ]; then
                            echo "GitHub Actions ${CONCLUSION} for ${SHA} – aborting deployment."
                            exit 1
                        fi
                        sleep 30
                    done
                    if [ "${CONCLUSION}" != "success" ]; then
                        echo "Timed out waiting for GitHub Actions (20 min). Aborting."
                        exit 1
                    fi
                '''

                // Ensure Docker CLI and Compose V2 plugin are available
                sh '''
                    if ! command -v docker &>/dev/null; then
                        echo "Docker CLI not found – installing..."
                        apt-get update -qq && apt-get install -y docker.io
                        chmod 666 /var/run/docker.sock || true
                    fi

                    if ! docker compose version &>/dev/null; then
                        echo "Docker Compose plugin not found – installing..."
                        ARCH=$(uname -m)
                        mkdir -p /var/jenkins_home/.docker/cli-plugins
                        curl -sSfL \
                            "https://github.com/docker/compose/releases/download/v2.24.7/docker-compose-linux-${ARCH}" \
                            -o /var/jenkins_home/.docker/cli-plugins/docker-compose
                        chmod +x /var/jenkins_home/.docker/cli-plugins/docker-compose
                        echo "Installed: $(docker compose version)"
                    else
                        echo "Found: $(docker compose version)"
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

    post {
        success {
            echo "Pipeline PASSED – build #${BUILD_NUMBER}. Services live on :8080 (API Gateway), :8082 (Catalog), :8081 (Order), :8761 (Eureka), :9411 (Zipkin)."
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
