# Release Documentation

This document describes how **TerseDecompress** performs releases, why releases happen, and how to interpret them.

## Purpose

Releases signal reviewed, stable versions of the software for users. They provide:
- a unique version identifier
- release notes describing functionality and fixes
- references to where users can obtain the artifact and ask questions

## Versioning

We use semantic versioning **MAJOR.MINOR.PATCH**:
- **PATCH**: bug and doc fixes
- **MINOR**: new backward-compatible enhancements
- **MAJOR**: breaking changes (rare)

## Release Frequency

Releases do not happen on a fixed schedule. They occur when:
- a bug impacting users is fixed
- a new feature is completed and tested
- accumulated changes benefit a stable release

## Development Process Transparency

Development happens continuously in this public repository. Commits and Pull Requests occur between official releases as new features are implemented and bugs are fixed. 

Release tags and the associated GitHub Releases represent stable snapshots of the project at a point in time, but they do not constitute the entirety of the project's history or activity. Users and contributors are encouraged to follow the `main` branch for the latest updates.

## Release Workflow

1. Work merged to `main` branch.
2. At least one additional maintainer approves.
3. All tests are passing in CI.
4. Release version bumped.
5. Human-readable **release notes** drafted (CHANGELOG/Release Notes).
6. Tag is created and pushed.
7. GitHub Release page is published with artifacts.

## Release Notes

Release notes include:
- high-level summary of what changed
- user-facing changes
- deprecated or removed behavior
- link to issues/PRs if relevant

## Dependencies

Current release also includes a list of tracked dependencies used at build time.

## Support and reporting

See SUPPORT.md for details on how users get help and report problems.
