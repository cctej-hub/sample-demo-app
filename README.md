# CBE Retail Transfer Service (`cbe-retail-transfer-service`)

Enterprise core banking service template for the **Central Bank of Ethiopia (CBE)**, integrated with the enterprise-governed Jenkins CI/CD model pipeline.

---

## 1. Architecture & Governance Overview

This repository operates under a **segregated governance model**:

```
┌─────────────────────────────────────────────────────────────┐
│ Application Repository (This Repository)                    │
│ • Business logic (Java 17 / Maven)                          │
│ • Dockerfile (Container runtime definition)                 │
│ • CI.json (Root pipeline metadata marker)                   │
└──────────────────────────────┬──────────────────────────────┘
                               │ Auto-discovered via CI.json
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ Centrally Governed Pipeline (cbe-shared-library)            │
│ • Standardized Jenkinsfile (agent none, stage retries)      │
│ • Central HashiCorp Vault credential injection              │
│ • Nexus & Harbor CLI integrations (No Jenkins UI plugins)   │
│ • Persistent NFS dependency caching                         │
└─────────────────────────────────────────────────────────────┘
```

### Why is there no `Jenkinsfile` here?
To prevent pipeline tampering, avoid accidental credential exposure, and enforce CBE compliance, **build pipelines are not defined inside application codebases**. 

Jenkins Organization Folders periodically scan the CBE GitLab group, discover repositories containing the `CI.json` marker file, and attach the centrally managed model pipeline automatically.

---

## 2. The `CI.json` Marker File

The root-level `CI.json` file is required for automated pipeline onboarding and discovery. It acts as both a discovery trigger and a runtime configuration provider.

### Specification:
```json
{
  "serviceName": "cbe-retail-transfer-service",
  "team": "core-banking",
  "buildType": "maven",
  "javaVersion": "17",
  "targetEnvironment": "uat",
  "notificationChannel": "#cbe-banking-ci",
  "testConfig": {
    "skipUnitTests": false,
    "runStaticAnalysis": true
  }
}
```

### Parameter Reference:
| Field | Type | Description | Mandatory |
| :--- | :--- | :--- | :--- |
| `serviceName` | String | Unique artifact identifier and container image name | **Yes** |
| `team` | String | CBE business unit / project team | **Yes** |
| `buildType` | String | Build ecosystem (`maven`, `gradle`, `nodejs`) | **Yes** |
| `javaVersion` | String | Target JDK runtime version (`17`, `21`) | **Yes** |
| `targetEnvironment`| String | Default destination namespace (`uat`, `qa`, `dr`) | **Yes** |
| `notificationChannel` | String | Internal notification channel for build alerts | No |

---

## 3. Local Development & Testing

### Prerequisites
* OpenJDK 17 or higher
* Apache Maven 3.9+
* Docker Engine 24+

### Build and Run Tests
```bash
# Execute local unit and integration tests
mvn clean test

# Package JAR artifact locally
mvn clean package -DskipTests=true
```

### Local Container Build
```bash
docker build -t cbe-retail-transfer-service:local .
```

---

## 4. CI/CD Lifecycle & Quality Gates

When changes are pushed to `main` or a feature branch, the centrally governed model pipeline executes the following stages on ephemeral Kubernetes agents:

1. **Governance & Marker Validation**: Validates `CI.json` schema and metadata.
2. **Compile, Test & Caching**: Compiles code against a persistent NFS `.m2` dependency cache to prevent internet re-downloads. Runs JUnit test suites with automatic stage-level retry.
3. **Artifact Stashing & Nexus Publication**: Packages and pushes versioned binaries to Nexus via secure CLI transfers.
4. **Containerization & Harbor Push**: Builds the container image and securely pushes it to the CBE Harbor registry with credentials injected via HashiCorp Vault.
5. **Zero-Compute Approval Gate**: If deploying to controlled environments, the pipeline enters an `agent none` human approval gate (holding zero Kubernetes pods or compute resources).
6. **Deployment**: Deploys the service Helm chart to the selected Kubernetes environment.

---

## 5. Security & Contribution Guidelines

* **No Hardcoded Secrets**: Never commit passwords, tokens, or private certificates. All secrets are managed dynamically via HashiCorp Vault.
* **Marker File Changes**: Any changes to `CI.json` (such as renaming `serviceName` or changing `targetEnvironment`) must be reviewed and approved by the project tech lead.
* **Branching Strategy**: Branch off `feature/*`, create merge requests targeting `main`, and ensure all unit tests and static quality checks pass prior to merging.