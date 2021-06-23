# 2. implementation-requirements

Date: 2020-12-23

## Status

Proposed

## Context

We have a handful of different build tools: gradle, maven, npm - to name a few, each having it's own way for bulding things, and therefore for "versioning" the builds and artifacts.
After some research (see meta links) we came to a conclusion that there is no "one tool to rule them all" that satisfies all of our requirements.
Therefore, rather than having different versioning tools for each build system we have, we will create our own versioning tool from scratch.

### Requirements

Below is the list of initial requirements that the tool has to satisfy:

* command-line tool as a standalone executable (script/binary) with minimal to none external dependencies - this will allow us to use it in our pipelines and will ensure that it is build system agnostic
* configuration/customization through global (localted in `/etc`), user-local (located in user's home dir), and repo-local (located in git repo) config files
* bumping next version through git commit messges (defaults to messages containing `[major]|[minor]|[patch]`, but should be configurable)
* printing current/next version to stdout
* creating a git tag for the current version
  * tags sometimes can have a prefix (i.e., in elhub we use `vX.Y.Z` for tags, where `X.Y.Z` is the version), and a suffix (i.e. `vX.Y.Z-rc.1`), and this should be supported
* updating versions in files - even though we aim to store versions as tags, a number of our tools require "static" versions, for example - maven only works with static versions, ansible collections need to have a version in `galaxy.yml`, scripts and cli tools have static versions, and so on. We need to be able to update these versions automatically.
  * list of files that need to be handled should be configurable per repo
  * optional replacing of "placeholders" (mostly applicable for cli tools where we can avoid committing the version, but need the version to be present when packaging the app)
  * we try to avoid committing versions in files as much as possible simply for the fact that it creates a lot of irrelevant commits, even when the version needs to be hardcoded; but this is out of the scope of this tool as it only deals with versions, and will be dealth with in the build pipes
* support for snapshots and pre-release versions
* support releasing artifacts to Artifactory

In addition to the above we essentially have two different versions to keep in mind in Elhub:
* a high-level version of Elhub (or a particular platform, e.g. Core Platform) - this is a version that gets deployed to an environment
* a low-level version of a component (deployable container) that belongs to a particular platform, e.g. version of `elhub-cp-ami-adapter` (Ami Adapter component in Core Platform)

Both of these need to be handled by this tool.

## Decision

We will implement the cli tool using Kotlin as it is more flexible than .kts, and our expectation is that a single .kts file might grow too large to support all of our requirements.

## Consequences

* Single approach to versioning all dev code
* Simplified releases of new versions
* Easier to use with in our automated build pipes

## Meta

* [Versioning Elhub](https://confluence.elhub.cloud/display/DI/Versioning+Elhub)
