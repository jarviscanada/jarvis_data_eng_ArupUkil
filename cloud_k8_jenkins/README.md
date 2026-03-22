# Introduction
This project extends the Spring Boot trading application [[GitHub](https://github.com/jarviscanada/jarvis_data_eng_ArupUkil/tree/master/springboot)] with Kubernetes deployment manifests for local and Azure cloud environments (Azure Container Registry) and Jenkins-based CI/CD automation. The deployed system consists of a Spring Boot application, a PostgreSQL database, Kubernetes services for internal and external traffic, persistent storage for the database, and Jenkins pipelines that builds the application image in Azure Container Registry (ACR) and roll out updates to Kubernetes. Note the Jenkinsfiles are meant for working with Azure and not for anything local.

There are two deployment environments in this project:
- `minikube/`: local development environment for testing Kubernetes manifests with locally built images.
- `aks/`: cloud deployment environment for Azure Kubernetes Service using ACR-hosted images.

Jenkins pipelines for development and production live under `springboot/`. They are designed to build a new `trading-app` image, authenticate to Azure, connect to the target AKS cluster, and update the existing `trading` deployment with `kubectl set image`.

# Application Architecture
At runtime, traffic flows through a Kubernetes `LoadBalancer` service to the Spring Boot application pods. The application reads and writes trading data in PostgreSQL through JDBC/JPA and also calls the external Finnhub API for market data. PostgreSQL stores its data on a persistent volume so database state survives pod restarts.

```text
                                +----------------------+
                                |        Client        |
                                | Browser / curl / UI  |
                                +----------+-----------+
                                           |
                      http://<external-ip>/swagger-ui.html
                                           |
                                           v
  +-----------------------------------------------------------------------------------+
  |                                   AKS Cluster                                     |
  |                                                                                   |
  |   +--------------------------------+                                              |
  |   | Service: trading               |                                              |
  |   | type: LoadBalancer             |                                              |
  |   | exposed through Azure LB       |                                              |
  |   | port 80 -> targetPort 8080     |                                              |
  |   +----------------+---------------+                                              |
  |                    |                                                              |
  |          +---------+---------+                                                    |
  |          |                   |                                                    |
  |          v                   v                                                    |
  |   +-------------+     +-------------+                                             |
  |   | Pod: trading|     | Pod: trading|                                             |
  |   | app-server  |     | app-server  |                                             |
  |   +------+------+     +------+------+                                             |
  |          |                   |                                                    |
  |          +---------+---------+                                                    |
  |                    |                                                              |
  |                    v                                                              |
  |   +--------------------------------+                                              |
  |   | Service: trading-psql          |                                              |
  |   | clusterIP: None (headless)     |                                              |
  |   | port 5432                      |                                              |
  |   +----------------+---------------+                                              |
  |                    |                                                              |
  |                    v                                                              |
  |             +-------------+                                                       |
  |             | Pod: psql   |                                                       |
  |             | trading-psql|                                                       |
  |             +------+------+                                                       |
  |                    |                                                              |
  |                    v                                                              |
  |          +------------------------+                                               |
  |          | Persistent storage     |                                               |
  |          | PVC: psql-pv-claim     |                                               |
  |          +------------------------+                                               |
  +-----------------------------------------------------------------------------------+

           Each trading pod also makes outbound HTTPS requests to the Finnhub API
```

The Kubernetes deployment layout is:

```text
  +-----------------------------------------------------------------------------------+
  |                          Azure Subscription / Resource Group                      |
  |                                                                                   |
  |   AKS control plane is Azure-managed and schedules workloads onto worker nodes    |
  +----------------------------------------------+------------------------------------+
                                                 |
                                                 v
  +-----------------------------------------------------------------------------------+
  |                                   AKS Cluster                                     |
  |                                                                                   |
  |   +-------------------------------+                                               |
  |   | Control Plane (managed)       |                                               |
  |   | API server / scheduler / etcd |                                               |
  |   +---------------+---------------+                                               |
  |                   |                                                               |
  |                   v                                                               |
  |   +--------------------------------------------------------------------------+    |
  |   | Node Pool / VM Scale Set                                                 |    |
  |   |                                                                          |    |
  |   |  trading-deployment.yaml                                                 |    |
  |   |  - Deployment/trading                                                    |    |
  |   |  - replicas: 2                                                           |    |
  |   |  - container: acrjarvisarupukil.azurecr.io/trading-app:1.0.0            |    |
  |   |  - Service/trading type LoadBalancer                                     |    |
  |   |                                                                          |    |
  |   |        +-------------+                     +-------------+                |    |
  |   |        | Pod: trading|                     | Pod: trading|                |    |
  |   |        +------+------+                     +------+------+                |    |
  |   |               \                                  /                        |    |
  |   |                \                                /                         |    |
  |   |                 +-------- Service/trading -----+                          |    |
  |   |                                                                          |    |
  |   |  psql-deployment.yaml                                                    |    |
  |   |  - Deployment/trading-psql                                               |    |
  |   |  - replicas: 1                                                           |    |
  |   |  - container: acrjarvisarupukil.azurecr.io/trading-psql:1.0.0           |    |
  |   |  - Service/trading-psql clusterIP None                                   |    |
  |   |  - PVC/psql-pv-claim (5Gi, ReadWriteOnce)                                |    |
  |   |                                                                          |    |
  |   |                 +-----------------+                                      |    |
  |   |                 | Pod: trading-   |                                      |    |
  |   |                 | psql            |                                      |    |
  |   |                 +--------+--------+                                      |    |
  |   |                          |                                               |    |
  |   |                 +--------v--------+                                      |    |
  |   |                 | PVC: psql-pv-   |                                      |    |
  |   |                 | claim           |                                      |    |
  |   |                 +-----------------+                                      |    |
  |   +--------------------------------------------------------------------------+    |
  +-----------------------------------------------------------------------------------+
```

## Deployment
There are two ways to run the project: locally on Minikube and cloud deployment on AKS.

### Minikube
The local environment uses Minikube and the manifests are in `cloud_k8_jenkins/minikube`.

- Images use local tags: `trading-app:1.0.0` and `trading-psql:1.0.0`
- `kustomization.yaml` generates the secret values used for the PostgreSQL and Finnhub environment variables
- The app is typically reached with `minikube service trading --url` or `kubectl port-forward`

Typical deployment flow:

```bash
docker build -t trading-app:1.0.0 springboot
docker build -t trading-psql:1.0.0 springboot/psql
minikube image load trading-app:1.0.0
minikube image load trading-psql:1.0.0
kubectl apply -k cloud_k8_jenkins/minikube
```

### AKS
The cloud deployment uses AKS and the manifests are in `cloud_k8_jenkins/aks`.

- Images are pulled from ACR:
  - `acrjarvisarupukil.azurecr.io/trading-app:1.0.0`
  - `acrjarvisarupukil.azurecr.io/trading-psql:1.0.0`
- `kustomization.yaml` generates the secret values used for the PostgreSQL and Finnhub environment variables
- The app is typically reached through the external IP assigned to `Service/trading`; this can be obtained with `kubectl get svc trading`, and for debugging `kubectl port-forward` can also be used
- In AKS the Postgres container sets `PGDATA=/var/lib/postgresql/data/pgdata` so the database initializes in a subdirectory of the mounted volume

Typical AKS deployment flow:

```bash
kubectl apply -k cloud_k8_jenkins/aks
kubectl get pods
kubectl get svc trading
```

# Jenkins CI/CD Pipeline
Jenkins is deployed to Kubernetes with Helm and is used to automate build and deployment. The pipelines are defined in:

- `cloud_k8_jenkins/springboot/Jenkinsfile-dev`
- `cloud_k8_jenkins/springboot/Jenkinsfile-prod`

Each pipeline runs as a Kubernetes agent pod with:
- `jnlp` container for Jenkins agent connectivity
- `azcli` container for Azure login, ACR build, and AKS credential retrieval
- `kubectl` container for Kubernetes rollout commands

The CI/CD flow is:
1. Validate branch
2. Login to Azure using a service principal stored in Jenkins credentials
3. Build and push a new `trading-app` image to ACR with `az acr build`
4. Fetch AKS credentials with `az aks get-credentials`
5. Update the running `trading` deployment with `kubectl set image`
6. Wait for rollout and print deployment/pod status

Pipeline visualization:

```text
+-------------+      +----------------+      +------------------+
| Git Branch  | ---> | Jenkins Job    | ---> | Kubernetes Agent |
| develop /   |      | triggered      |      | Pod              |
| main        |      |                |      |                  |
+-------------+      +----------------+      +------------------+
                                                   |
                                                   v
                                          +------------------+
                                          | Azure Login      |
                                          | Service Principal|
                                          +------------------+
                                                   |
                                                   v
                                          +------------------+
                                          | ACR Build        |
                                          | Build + Push     |
                                          | trading-app      |
                                          +------------------+
                                                   |
                                                   v
                                          +------------------+
                                          | AKS Credentials  |
                                          | az aks get-cred  |
                                          +------------------+
                                                   |
                                                   v
                                          +------------------+
                                          | Deploy           |
                                          | kubectl set image|
                                          | rollout status   |
                                          +------------------+
```

The main differences between the two pipelines are:
- The dev pipeline runs for the `develop` branch, uses `AZ_USER_DEV`, `AZ_PWD_DEV`, and `AZ_TENANT_DEV`, and targets the `aks-trading-app` cluster.
- The prod pipeline runs for the `main` branch, uses `AZ_USER_PROD`, `AZ_PWD_PROD`, and `AZ_TENANT_PROD`, and targets the `aks-trading-app-prod` cluster.

# Improvements
- Add a dedicated test stage before deployment so the pipeline runs Maven unit or integration tests before publishing a new image.
- Move sensitive values such as database passwords and Finnhub tokens out of `secretGenerator` literals and into a proper secrets manager such as Azure Key Vault or external Kubernetes secrets.
- Replace direct `kubectl set image` rollouts with Helm or a manifest-based deployment flow so desired state stays versioned and consistent with the repository.
- Add Horizontal Pod Autoscaler and resource requests/limits so the app can scale more predictably under load.
