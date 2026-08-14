# Morpheus Jenkins Plugin

The Morpheus Jenkins Plugin integrates Morpheus with Jenkins to enable triggering Jenkins build jobs as part of Morpheus task and workflow automation. The plugin provides a task provider that calls the Jenkins REST API to queue a build with optional parameters.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Repository structure](#repository-structure)
- [Building the plugin](#building-the-plugin)
- [License](#license)
- [Installing](#installing)
- [Detailed Usage Steps](#detailed-usage-steps)
- [API Endpoints](#api-endpoints)

---

## Features

### Jenkins Task Provider

Execute Jenkins build jobs from Morpheus tasks and workflows. Supports parameterised builds and configurable job names. The task can be used in provisioning workflows, operational tasks, and automation pipelines.

---

## Requirements

| Requirement | Version |
|-------------|---------|
| Morpheus | 9.0.0 or later |
| Java | 25 or later |
| Gradle | Use the included Gradle wrapper (`./gradlew`) |

Additional prerequisites:

- A running Jenkins server accessible over HTTP or HTTPS from the Morpheus appliance
- A Jenkins user account and API token with permission to trigger builds on the target job
- Network access from the Morpheus appliance to the Jenkins server on the configured port

---

## Repository structure

```
src/main/groovy/com/morpheusdata/jenkins/
├── JenkinsPlugin.groovy        - Plugin entry point; registers JenkinsTaskProvider
├── JenkinsTaskProvider.groovy  - TaskProvider implementation; OptionTypes and task execution logic
└── JenkinsTaskService.groovy   - Service class; calls the Jenkins API to trigger builds
build.gradle, gradle.properties - Build configuration and plugin metadata
```

---

## Building the plugin

Run the following command to compile and package the plugin jar:

```bash
./gradlew clean build
```

The packaged jar will be written to `build/libs/`.

To execute tests, use the following command:

```bash
./gradlew test
```

---

## License

This project is licensed under the Apache License 2.0.

See the [LICENSE](LICENSE) file for details.

---

## Installing

1. Build the plugin (see [Building the plugin](#building-the-plugin)) or download a released jar.
2. In Morpheus, navigate to **Administration > Integrations > Plugins**.
3. Click **Add** and upload the `morpheus-jenkins-plugin-<version>.jar` from `build/libs/`.
4. The **Jenkins Job** task type will be available under **Library > Automation > Tasks > Add**.

---

## Detailed Usage Steps

### Creating a Jenkins Task

1. Go to **Library > Automation > Tasks > Add**.
2. Select **Jenkins Job** as the task type.
3. Configure:
   - **API Url** — Jenkins base URL, e.g. `https://jenkins.example.com`
   - **Username** — Jenkins user for authentication
   - **Token** — Jenkins API token for the above user
   - **Job Name** — the Jenkins job path (e.g. `my-folder/my-job`)
   - **Build Parameters** — optional JSON key/value pairs passed to the parameterised build
4. Save the task.

### Running the Task in a Workflow

1. Go to **Library > Automation > Workflows > Add** (or edit an existing workflow).
2. Add the Jenkins task to the desired phase (e.g. Post Provision).
3. Associate the workflow with an instance type or run it manually from an instance.

### Running the Task Manually

1. From an instance detail page, go to **Actions > Run Task**.
2. Select the Jenkins task and confirm. Morpheus calls the Jenkins API to queue the build.

---

## API Endpoints

This plugin communicates with the **Jenkins REST API** at the configured API Url. Authentication uses HTTP Basic with username and API token.

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `{jenkinsUrl}/job/{jobName}/build` | POST | Trigger a build (no parameters) |
| `{jenkinsUrl}/job/{jobName}/buildWithParameters` | POST | Trigger a parameterised build |
| `{jenkinsUrl}/job/{jobName}/lastBuild/api/json` | GET | Get last build status |
