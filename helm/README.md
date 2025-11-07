Module `helm`
===

Install in minikube
---

The following shell commands create a Keycloak and PostgreSQL instance in a new Kubernetes cluster called `keycloak-custom` in minikube:

### Create minikube `keycloak-custom` profile and watch pods

```shell
minikube -p keycloak-custom start
watch kubectl get pods --all-namespaces
```

### Declare Helm repos

```shell
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add codecentric https://codecentric.github.io/helm-charts
```

### Install PostgreSQL

```shell
helm install postgresql bitnami/postgresql --values ./src/test/resources/postgresql/values.minikube.yaml
```

### Register container image in minikube registry

By running the Maven build with env variables set for docker/minikube the container image is directly build into the minikube container registry:

```shell
cd ..
eval $(minikube -p keycloak-custom docker-env)
minikube -p keycloak-custom docker-env
mvn clean install -DskipTests
```

### Create necessary ConfigMap/Secret

We use a ConfigMap and a Secret for providing the environment specific values:

```shell
kubectl apply -f ./src/test/resources/local/keycloak-custom-config-vars.local.yaml
kubectl apply -f ./src/test/resources/local/keycloak-custom-secret-vars.local.yaml
```

### Install Keycloak

```shell
helm install keycloak codecentric/keycloakx --values ./src/generated/keycloak-custom-chart/values.yaml
```

### Make Keycloak accessible from localhost

```shell
kubectl port-forward service/keycloak-keycloakx-http "8080:80"
```

Open the Keycloak Admin Console: http://localhost:8080

Uninstall in minikube
---

### Uninstall Keycloak

```shell
helm uninstall keycloak
```

### Uninstall PostgreSQL

```shell
helm uninstall postgresql
```

### Delete minikube `keycloak-custom`  profile

```shell
minikube -p keycloak-custom delete
minikube profile list
```