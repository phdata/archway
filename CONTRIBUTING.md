# Contributing

Before contributing to this repository, please first discuss the change you wish to make in Slack (#heimdali). All
work should be associated with a ticket on the [Jira board](https://phdata.atlassian.net/jira/software/projects/HEIM/boards/29).

## Pull Request Process

1. Do not commit binary files unless absolutely necessary. If they are necessary please include rationale.
2. Ensure you've followed the code guidelines below.
3. Update the README.md if any of your changes impact it's integrity.
4. Ensure any database schema changes are reflected in the mysql model (heimdali.mwb), flyway/mysql, and flyway/pg
5. Test all code locally either in IntelliJ via `run all ScalaTests` or `sbt test` (see README.md)
6. Once you've created a pull request, please ensure your ticket reflects the "ready" status in the Jira board

## Application Design Principals

### Cats, Cats Effect, and Tagless Final
We are using cats and cats-effect as the foundation for this project. If you are writing new functionality please stick
with those libraries as they either solve 80% of the problems and provide the facilities for another 19%. If you are
tempted to use `Future` directly, `synchronize`, or other more "traditional" means to solve code problems, please ask
in #heimdali-dev and someone can help walk through the problem with you.

Benefits:

- Consistency -- it's currently used everywhere, if we have portions of code that stray, it becomes more challenging
  for everyone to keep up with where we do things different.
- Flexibility -- using higher kinds (`F[_]`) allows us to defer the decision of behavior to a centralized location (e.g. IOApp)
- Proven -- as mentioned above cats and cats effect provides facilities to solve for most situations that arise in development.
  If a data type is not available that suits your needs, you can utilize ad-hoc inheritance to provide your own capability.

### Separation of Concerns
If your logic can be influenced by external factors like `ExecutionContext`, `CacheService`, a http client, etc, they
should be parameters of your container. A container (i.e. class) should be responsible for one topic, if it depends on
a capability outside it's responsibility, it should be accepted as a parameter. See existing code for examples on how
to break apart concerns (e.g. `ClusterController` -> `ClusterService` -> `HttpClient` -> `Htt4s`).

Benefits:

- Unit Testing -- when unit testing the focus is on the current class's capability. mocking dependencies can be powerful
  to validate the functionality can tolerate different external behaviors. (e.g. what happens if we use cached pools?)
- Integration Testing -- when integration testing, providing different implementations can be powerful to verify what
  might happen in the full swing of a chain (i.e. when we call the api and use mysql vs pg, do we get what we expect?)
- Expectations -- when a developer is working on a problem it's much easier to determine where to solve problems if you
  know where to look (e.g. all `ExecutionContext`s are defined in `Server`, or all our http client calls go through "here").

### External Dependencies
Heimdali's API is heavily focused on cats-related dependencies in alignment with the previous principal discussed. If new
dependencies are required, it should be discussed first, and ideally should be within the TypeLevel ecosystem.

Current Primary Dependencies:

- Cats -- core FP library
- Cats Effect -- concurrency
- Circe -- JSON encoding/decoding
- Scalate -- templating
- Http4s -- HTTP Server & Client
- Hadoop -- Hadoop ;)

## Provisioning

## Creating New Provisioning Actions
Using the following template for IntelliJ, adding a new provisioning task is quite simple:
```scala
implicit object $PROV_TYPE$CompletionTask extends CompletionTask[$PROV_TYPE$] {

override def apply[F[_] : Sync](createDatabaseDirectory: CreateDatabaseDirectory, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
  workspaceContext.context.$COMPLETE_METHOD$
    .transact(workspaceContext.context.transactor).void

}

implicit object $PROV_TYPE$ProvisioningTask extends ProvisioningTask[$PROV_TYPE$] {

override def apply[F[_] : Sync : Clock](createDatabaseDirectory: CreateDatabaseDirectory, workspaceContext: WorkspaceContext[F]): F[Unit] =
  workspaceContext.context.$PROVISION_METHOD$.void

}

implicit val provisionable: Provisionable[$PROV_TYPE$] = Provisionable.deriveProvisionable
```

You can add it by going to "IntelliJ IDEA" > "Preferences..." > "Editor/Live Templates" then simply copy and paste the above into a new template.

You can then call it by using the name you gave it in the dialog.