# auto-release

[<img src="https://img.shields.io/badge/repo-github-blue" alt="">](https://github.com/elhub/devxp-auto-release)
[<img src="https://img.shields.io/badge/issues-jira-orange" alt="">](https://jira.elhub.cloud/issues/?jql=project%20%3D%20%22Team%20Dev%22%20AND%20component%20%3D%20devxp-auto-release%20AND%20status%20!%3D%20Done)
[<img src="https://teamcity.elhub.cloud/app/rest/builds/buildType:(id:DevXp_DevXpAutoRelease_PublishDocs)/statusIcon" alt="">](https://teamcity.elhub.cloud/project/DevXp_DevXpAutoRelease?mode=builds#all-projects)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-auto-release&metric=alert_status" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-auto-release)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-auto-release&metric=ncloc" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-auto-release)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-auto-release&metric=bugs" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-auto-release)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-auto-release&metric=vulnerabilities" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-auto-release)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-auto-release&metric=coverage" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-auto-release)


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
│   └── pom.xml <--- moduleA POM
├── moduleB
│   └── pom.xml <--- moduleB POM
├── moduleC
│   └── pom.xml <--- moduleC POM
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

Sub-sub(-sub)*? modules are also supported so long as they follow the same rules:

- the sub-module A has `<modules>` tag in the pom.xml with a list of it's sub-modules
- the sub-sub-modules AA and AB declare their parent `moduleA` in their pom.xml and use the same version as the parent

```
.
├── moduleA
│   ├── moduleAA
│   │   └── pom.xml <--- moduleAA POM
│   ├── moduleAB
│   │   └── pom.xml <--- moduleAB POM
│   └── pom.xml <--- moduleA POM
├── moduleB
│   └── pom.xml <--- moduleB POM
├── moduleC
│   └── pom.xml <--- moduleC POM
└── pom.xml     <--- project-root
```

#### Distribution Management Configuration

It is possible to override `distributionManagement` node in the `pom.xml` file with auto-release using `--maven-distribution-management` option and a json string with the following format:

```json
{
  "repository": {
    "uniqueVersion": true,
    "id": "repo-id",
    "name": "repo-name",
    "url": "repo-url",
    "layout": "repo-layout"
  },
  "snapshotRepository": {
    "uniqueVersion": true,
    "id": "repo-id",
    "name": "repo-name",
    "url": "repo-url",
    "layout": "repo-layout"
  }
}
```

The `snapshotRepository` object is optional and can be omitted if not needed.
The `layout` field is optional and will use `"default"` value if not specified otherwise.

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

See the [open issues](https://github.com/elhub/devxp-auto-release/issues) for a list of proposed features (and known issues).

## Contributing

Contributing, issues and feature requests are welcome. See the
[Contributing](https://github.com/elhub/devxp-auto-release/blob/main/CONTRIBUTING.md) file.

## Owners

This project is developed by [Elhub](https://github.com/elhub). For the specific development group responsible for this
code, see the [Codeowners](https://github.com/elhub/devxp-auto-release/blob/main/CODEOWNERS) file.

## License

This project is [MIT](https://github.com/elhub/devxp-auto-release/blob/main/LICENSE.md) licensed.
