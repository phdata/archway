## Creating a release

### Versioning

Archway follows [Semantic Versioning](https://semver.org/). Given a version like 1.1.1, with the parts called
<major>.<minor>.<patch>

- Major versions will increment if there are breaking API changes
- Minor versions will increment if there is new functionality
- Patch versions will increment if there are fixes to existing functionality

We also add a 'release candidate' version to the end of the semver version so we can test and track versions internally
before they are released to the public, for example `1.1.0-rc1` for release candiate '1'.

### Builds

There are three builds defined in Bitbucket

- default: This build runs on each committed branch, building the UI and API code
- nightly: This build creates a CSD and parcel in the parcels-dev Artifactory Repo nightly
- commit: This build can be run to create a custom build for a specific commit hash. This is useful for on-demand testing
