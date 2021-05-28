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
  * [Gradle](#gradle)
  * [Maven](#maven)
  * [Multi-module Maven](#multi-module-maven)
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

The positional parameter can be used to specify the working directory to analyze. The "-p" option is used to specify which type of project that is being analyzed.

The app works by analyzing the new version from Git, and then writes the next version into the appropriate file used by the project type it is working on. The file with the updated version does _not_ need to be committed to git, though you can of course do so if you prefer. Note that the actual number present in the version file before auto-release is run, is not used by the app.

The app currently allows for the following default projects:

### Gradle

The project should contain a gradle.properties file, storing the project version in the form:

```properties
version=X.Y.Z
```

The project must include a gradle wrapper (gradlew) for building and publishing the project. It assumes that the project can be published using the `publish` gradle task.

### Maven

The project should contain a pom.xml file, storing the project version in the form:

```xml
<version>version=X.Y.Z</version>
```

The `pom.xml` file should be a valid maven project pom.

It is assumed that the environment can run maven using the `mvn` command, and that the project can be built and published using the `deploy` maven goal.

#### Multi-module Maven

Multi-module maven projects are also supported. Given the parent `pom.xml` contains a list of `modules` in the form:

```xml
<project>
    <groupId>com.example.organization</groupId>
    <artifactId>my-application</artifactId>
    <version>0.1.0-SNAPSHOT</version>

    <modules>
        <module>moduleA</module>
        <module>moduleB</module>
        <module>moduleC</module>
    </modules>
</project>
```

And the following directory structure:

```
.
├── moduleA
│   └── pom.xml <--- Module A POM
├── moduleB
│   └── pom.xml <--- Module B POM
├── moduleC
│   └── pom.xml <--- Module C POM
└── pom.xml     <--- project-root
```

And each of the modules declares the parent:

```xml
<project>
    <parent>
        <groupId>com.example.organization</groupId>
        <artifactId>my-application</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>my-application-module-C</artifactId>
    <!-- Note that the version for the module is not declared and is the same as parent -->
</project>
```

*Note: it is assumed that each of the modules follows the "parent" (`project-root`) version*

Then the following updates will happen:

- the `<version/>` tag in the `project-root` pom.xml will be updated with the next version
- the `<version/>` tag under the `<parent/>` in each module's pom.xml will be updated with the next version.

### Ansible

Currently, we only support updating the version in the `galaxy.yml` file in the project's root directory, so in a way releasing is supported only for ansible collections.

The project should contain a `galaxy.yml` file, storing the project version in the form:

```yml
version: X.Y.Z
```

*Note that the changes made to the `galaxy.yml` file will be committed automatically.*

## Testing

The full suite of tests can be run using:

```sh
./gradlew test
```

## Roadmap

See the [open issues](https://github.com/elhub/dev-tools-auto-release/issues) for a list of proposed features (and known issues).

## Contributing

Contributing, issues and feature requests are welcome. See the
[Contributing](https://github.com/elhub/dev-tools-auto-release/blob/main/CONTRIBUTING.md) file.

## Owners

This project is developed by [Elhub](https://github.com/elhub). For the specific development group responsible for this
code, see the [Codeowners](https://github.com/elhub/dev-tools-auto-release/blob/main/CODEOWNERS) file.

## License

This project is [MIT](https://github.com/elhub/dev-tools-auto-release/blob/main/LICENSE.md) licensed.
