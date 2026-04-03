# SoPra FS26 – Group 21 · Backend

Spring Boot 4 / Java 17 REST API for the SoPra FS26 Group 21 project.
Deployed on **Google Cloud App Engine** · Exposes the REST API on port `8080`. The frontend (Next.js) runs separately on port `3000`.

---

## Prerequisites

- Java 17 (ensure `JAVA_HOME` points to the correct version on Windows)
- No additional installation required — the Gradle Wrapper (`./gradlew`) handles all dependencies

---

## IDE Setup

### IntelliJ IDEA
A free educational license is available at [jetbrains.com/community/education](https://www.jetbrains.com/community/education/#students).

1. **File → Open** → select the `sopra-fs26-group-21-server` folder
2. Accept the prompt to import as a Gradle project
3. Right-click `build.gradle` → **Run Build**

### VS Code
Install the following extensions:
- `vmware.vscode-spring-boot`
- `vscjava.vscode-spring-initializr`
- `vscjava.vscode-spring-boot-dashboard`
- `vscjava.vscode-java-pack`

Build the project first via the Gradle Tasks panel, then start the server from the Spring Boot Dashboard.

---

## Development

```bash
./gradlew bootRun                      # start server at http://localhost:8080
./gradlew build --continuous -xtest   # watch mode (rebuilds on file change, skips tests)
```

For watch mode, run `./gradlew build --continuous -xtest` in one terminal and `./gradlew bootRun` in a second.

**H2 console:** `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa` / Password: *(leave blank)*

The database is in-memory and resets on every restart.

---

## Build & Test

```bash
./gradlew build                        # compile, test, and package
./gradlew test                         # run all tests
./gradlew test jacocoTestReport        # run tests and generate JaCoCo coverage report
./gradlew sonar                        # run SonarCloud analysis
```

Run a single test class:
```bash
./gradlew test --tests "ch.uzh.ifi.hase.soprafs26.service.UserServiceTest"
```

---

## Debugging

1. Start the server with the debug agent attached:
   ```bash
   ./gradlew bootRun --debug-jvm
   ```
2. In your IDE, add a **Remote JVM Debug** run configuration (default port `5005`).
3. Launch the configuration and set breakpoints as needed.

---

## API Testing

Use [Postman](https://www.postman.com/) or any HTTP client to test the REST endpoints. The server runs at `http://localhost:8080` by default.

---

## Docker

Push to `main` automatically builds and pushes a Docker image to Docker Hub via GitHub Actions.

**One-time setup** (one team member):
1. Create a [Docker Hub](https://hub.docker.com/) account (include the group number in the username, e.g. `sopra_group_21`).
2. Create a repository on Docker Hub with the same name as the GitHub repository.
3. Add the following [repository secrets](https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions#creating-secrets-for-a-repository):
   - `dockerhub_username`
   - `dockerhub_password` — a Docker Hub [personal access token](https://docs.docker.com/docker-hub/access-tokens/) with read/write access
   - `dockerhub_repo_name`

**Run locally:**
```bash
docker pull <dockerhub_username>/<dockerhub_repo_name>
docker run -p 8080:8080 <dockerhub_username>/<dockerhub_repo_name>
```
