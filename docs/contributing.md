# Contributing

Before contributing to this repository, please first discuss the change you wish to make in Slack (#archway). All
work should be associated with a ticket on the [Jira board](https://phdata.atlassian.net/jira/software/projects/HEIM/boards/29).

## Pull Request Process

1. Do not commit binary files unless absolutely necessary. If they are necessary please include rationale.
2. Ensure you've followed the code guidelines below.
3. Update the README.md if any of your changes impact it's integrity.
4. Ensure any database schema changes are reflected in the mysql model (archway.mwb), flyway/mysql, and flyway/pg
5. Test all code locally either in IntelliJ via `run all ScalaTests` or `sbt test` (see README.md)
6. Once you've created a pull request, please ensure your ticket reflects the "ready" status in the Jira board
