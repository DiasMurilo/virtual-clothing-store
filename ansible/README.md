# Ansible Deployment Automation

This directory contains Ansible playbooks and configuration for automated deployment of the Virtual Clothing Store microservices.

## Prerequisites

- Ansible 2.9 or higher
- Docker installed on target host
- Python 3.8+ with `docker` module

Install Ansible and dependencies:

```bash
pip install ansible docker
ansible-galaxy collection install community.docker
```

## Directory Structure

```
ansible/
├── ansible.cfg           # Ansible configuration
├── inventory/
│   └── hosts.yml        # Target hosts inventory
├── playbooks/
│   └── deploy-order-service.yml  # Order service deployment
└── README.md            # This file
```

## Usage

### Deploy Order Service

Deploy from local machine:

```bash
cd ansible
ansible-playbook playbooks/deploy-order-service.yml
```

Deploy with custom Docker image:

```bash
ansible-playbook playbooks/deploy-order-service.yml \
  -e "docker_image=ghcr.io/diasmurilo/virtual-clothing-store:Assignment1_CICD-abc123"
```

Deploy with GitHub token (for private registries):

```bash
ansible-playbook playbooks/deploy-order-service.yml \
  -e "github_token=YOUR_TOKEN_HERE"
```

### Verify Deployment

Check application health:

```bash
curl http://localhost:8081/actuator/health
```

View container logs:

```bash
docker logs order-service
```

View all containers:

```bash
docker ps -a
```

## Playbook Features

The `deploy-order-service.yml` playbook includes:

- ✅ Docker installation verification
- ✅ Network creation/validation
- ✅ Image pulling from registry (with retries)
- ✅ Graceful container stop/removal
- ✅ Container deployment with health checks
- ✅ Application health verification
- ✅ Deployment logs and status reporting

## Configuration

### Inventory Variables

Edit `inventory/hosts.yml` to customize:

- `app_port` - Application port (default: 8081)
- `docker_network` - Docker network name
- `health_check_path` - Health endpoint path
- Database configuration (for PostgreSQL deployment)

### Playbook Variables

Available variables in `deploy-order-service.yml`:

- `docker_image` - Docker image to deploy
- `container_name` - Name for the container
- `host_port` - Host port mapping
- `github_token` - GitHub Container Registry token

## CI/CD Integration

This playbook is integrated into the GitHub Actions workflow (`.github/workflows/ci-cd.yml`) and runs automatically on successful builds to the `Assignment1_CICD`, `main`, or `master` branches.

## Troubleshooting

**Issue**: "docker not found"

```bash
# Install Docker first
```

**Issue**: "Permission denied while trying to connect to Docker daemon"

```bash
# Add user to docker group or use sudo
sudo usermod -aG docker $USER
```

**Issue**: "Container health check failed"

```bash
# Check container logs
docker logs order-service

# Check if port is already in use
netstat -tuln | grep 8081
```

## Future Enhancements

- Multi-environment support (dev/staging/prod)
- Blue-green deployment strategy
- Rollback capabilities
- PostgreSQL deployment automation
- Full microservices stack deployment
- Secrets management with Ansible Vault
