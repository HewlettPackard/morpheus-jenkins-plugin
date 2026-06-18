# Morpheus Jenkins Plugin

This plugin provides task automation integration between [Jenkins](https://www.jenkins.io) and [Morpheus](https://morpheusdata.com). It enables Jenkins job triggering, parameterized builds, queue polling, build status polling, and task result chaining from within the Morpheus platform.

## Requirements

| Component | Minimum Version |
|-----------|----------------|
| Morpheus | 9.0.0 |

## Installation

1. Download the latest `.jar` from the [Releases](https://github.com/HewlettPackard/morpheus-jenkins-plugin/releases) page, or [build it yourself](#building).
2. In Morpheus, navigate to **Administration → Integrations → Plugins**.
3. Click **Browse** and upload the `.jar` file.
4. The **Jenkins Trigger Build** task type will appear after the plugin loads.

## Configuration

When adding a Jenkins task in Morpheus (**Library → Automation → Tasks → Add Task**), provide the following:

| Field | Description |
|-------|-------------|
| **API Url** | Jenkins base URL used for API calls |
| **Username** | Jenkins username used to trigger builds |
| **Token** | Jenkins API token or password for the configured user |
| **Job Name** | Jenkins job name to trigger |
| **Build Parameters** | Optional JSON object of build parameters passed to `buildWithParameters` |

## Features

### Jenkins Task Type
The plugin registers a Morpheus `TaskProvider` named **Jenkins Trigger Build**. Supported task behavior includes:

- Execute as a Morpheus app-scoped local task
- Trigger Jenkins jobs through the Jenkins API
- Trigger `build` when no parameters are supplied
- Trigger `buildWithParameters` when build parameters are supplied
- Accept build parameters as JSON and send them as Jenkins query parameters

### Build Monitoring
Triggered Jenkins builds are monitored until completion. Supported operations include:

- Poll the Jenkins queue API until the queued item resolves to a build
- Detect stuck queue items and fail the Morpheus task
- Poll the Jenkins build API until the build is no longer running
- Mark Morpheus task success when the Jenkins result is not `FAILURE`
- Mark Morpheus task failure for failed Jenkins builds or timeout conditions

### Task Results
The task type exposes Jenkins build details back to Morpheus automation workflows. Supported result behavior includes:

- Return the Jenkins build API response as task result data
- Use the Jenkins build `fullDisplayName` as task output
- Enable downstream Morpheus tasks to consume Jenkins build results through task result chaining

## Building

```bash
./gradlew shadowJar
```

The plugin JAR will be written to `build/libs/`.

## License

Copyright 2024 Morpheus Data, LLC. Licensed under the [Apache License, Version 2.0](LICENSE).
