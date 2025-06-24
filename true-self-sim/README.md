# True Self Simulation Deployment

This project contains a Spring Boot backend and a Vite/React frontend. The stack can be run with Docker Compose or deployed to Kubernetes.

## Prerequisites

- Docker
- docker-compose (v2 recommended)
- kubectl connected to a cluster (for Kubernetes deployment)

## Environment Variables

Database credentials are loaded from the `.env` file in the project root. Example values are provided:

```
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=true_self
```

The backend service reads `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD` and `DB_NAME` which are set in `docker-compose.yml` and the Kubernetes manifests.

The frontend build accepts `VITE_API_URL` so it knows where to reach the backend API.

## Running with Docker Compose

Build and start all services:

```bash
docker compose up --build
```

The application will be available on:

- Frontend: <http://localhost:3000>
- Backend API: <http://localhost:8080>
- Postgres: on port 5432

## Deploying to Kubernetes

Build the container images:

```bash
docker build -t backend:latest back
docker build --build-arg VITE_API_URL=<API_URL> -t frontend:latest front
```

Push the images to your registry with `docker push` or load them into your local cluster.

Apply the manifests:

```bash
kubectl apply -f k8s/
```

This creates deployments and services for the frontend, backend and Postgres database. Configuration values are provided via ConfigMaps and Secrets.
