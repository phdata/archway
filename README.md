# Heimdali
[![Build Status](http://ci.jotunn.io/api/badges/bennythomps/heimdali-api/status.svg)](http://ci.jotunn.io/bennythomps/heimdali-api)

## Developers
* We'll be using GitFlow http://nvie.com/posts/a-successful-git-branching-model/
* https://github.com/agis/git-style-guide
* Branch and create pull requests for all work
* Each acceptance criteria item should have a test
* Use your Jotunn email address for committing

## CI/CD
* Drone CI is purely based on Docker
* Deployments will be automated, so get changes in before Sunday evening
* Versions will be YYYY.MM.WW

## Testing

Run tests with
```bash
$ sbt test
```

Integration tests with

```bash
$ sbt it:test
```

Integration tests live in `src/it/`

## Requirements
* Scala 2.12
* Java 1.8
* Play Framework 2.6.x

More to come...
