# auto-release

<!-- PROJECT SHIELDS -->
![TeamCity Build](https://teamcity.elhub.cloud/app/rest/builds/buildType:(id:Tools_DevToolsAutoRelease_AutoRelease)/statusIcon)
[![Quality Gate Status](https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.tools%3Adev-tools-auto-release&metric=alert_status)](https://sonar.elhub.cloud/dashboard?id=no.elhub.tools%3Adev-tools-auto-release)
[![Lines of Code](https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.tools%3Adev-tools-auto-release&metric=ncloc)](https://sonar.elhub.cloud/dashboard?id=no.elhub.tools%3Adev-tools-auto-release)

[![Vulnerabilities](https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.tools%3Adev-tools-auto-release&metric=vulnerabilities)](https://sonar.elhub.cloud/dashboard?id=no.elhub.tools%3Adev-tools-auto-release)
[![Bugs](https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.tools%3Adev-tools-auto-release&metric=bugs)](https://sonar.elhub.cloud/dashboard?id=no.elhub.tools%3Adev-tools-auto-release)
[![Code Smells](https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.tools%3Adev-tools-auto-release&metric=code_smells)](https://sonar.elhub.cloud/dashboard?id=no.elhub.tools%3Adev-tools-auto-release)

## Table of Contents

* [About](#about)
* [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
  * [Installation](#installation)
* [Usage](#usage)
* [Testing](#testing)
* [Roadmap](#roadmap)
* [Contributing](#contributing)
* [Owners](#owners)
* [License](#license)


## About

**auto-release** is a small application that automates the semantic-versioning release workflow for software projects based on software commits. It:
* Determines the version number based on the git tags in the repository,
* Parses the commit log from the last version update to determine the next version,
* Builds and tags the new version, if required,
* Publishes a new version of the repository.

**auto-release** is built to work with a trunk-based development workflow.


## Getting Started

### Prerequisites

This application requires Java 1.8 or later. In addition, auto-release must be run in a directory with an initialized git repository.

### Installation

The latest version can be downloaded from Elhub's internal artifactory under _elhub-bin/auto-release/_.

To build the current version, run:

```sh
./gradlew assemble
```

To publish the executable jar to artifactory, run:

```sh
./gradlew publish
```

## Usage

To run the project on the existing repository for a gradle project, use:
```sh
java -jar auto-release.jar . -p gradle
```

The positional parameter can be used to specify the working directory to analyze. The "-p" option is used to
specify which type of project that is being analyzed.

The app works by analyzing the new version from Git, and then writes the next version into the appropriate file used by the project type it is working on. The 
file with the updated version does _not_ need to be committed to git, though you can of course do so if you prefer. Note that the actual number present in the
version file before auto-release is run, is not used by the app.

The app currently allows for the following default projects:

### Gradle

The project should contain a gradle.properties file, storing the project version in the form:
```properties
version=X.Y.Z
```

The project must include a gradle wrapper (gradlew) for building and publishing the project. It assumes that the project can be published using a "publish"
task.

### Maven

The project should contain a pom.xml file, storing the project version in the form:
```xml
<version>version=X.Y.Z</version>
```

It is assumed that the environment can run maven using the mvn command, and that the project can be built and published using a 'publish' task.


## Testing

The full suite of tests can be run using:

```sh
./gradlew test
```

## Roadmap

See the [open issues](https://jira.elhub.cloud/link-to-issues) for a list of proposed features (and known issues).

## Contributing

Contributing, issues and feature requests are welcome. See the
[Contributing](https://github.com/elhub/dev-tools-auto-release/blob/main/CONTRIBUTING.md) file.

## Owners

This project is developed by [Elhub](https://github.com/elhub). For the specific development group responsible for this
code, see the [Codeowners](https://github.com/elhub/dev-tools-auto-release/blob/main/CODEOWNERS) file.

## License

This project is [MIT](https://github.com/elhub/dev-tools-auto-release/blob/main/LICENSE.md) licensed.
