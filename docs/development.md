## Running Locally

_NOTE_ Make sure you have [npm](https://www.npmjs.com) and [IntelliJ](https://www.jetbrains.com/idea/) installed. For running project and integration tests locally is also recommended to have installed [Docker](https://www.docker.com).

### Configuration

In order to run the API locally, you'll need to prepare your development environment.

Create your `application.conf` file in `api/src/main/resources/` and initialize properties based on description in file [Service.md](service.md). File `application-template.conf` serves as an empty template for configuration file.

### Run The App (API-focused development)

- Open the project in IntelliJ
- Open the run configuration drop down and click "Add Configurations" / "Edit Configurations..."
- Click on plus "Add New Configuration" or hit CMD+N/Ctrl+N
- Select "Application" from dropdown
- Set `io.phdata.Server` as a "Main class"
- Set `api` for "Use classpath of module"
- Check "Include dependencies with 'Provided' scope"
- Open the "Terminal" tab
- Run `make init-ui` which will install the npm dependencies
- Run `serve-ui` which will spin up the UI

### Run The App (UI-focused development)

- In the terminal tab, run `./sbt -Djavax.net.ssl.trustStore=<truststore> compile "api/runMain io.phdata.Server"`
- In another terminal tab, run `npm i` which will install the npm dependencies
- Run `npm start` which will spin up the UI
- Once the UI has started you can start editing and the UI will refresh based on your changes
- To run the UI in browsers, you need to run browsers without CORS. To run the UI
  in chrome browser, you need to run chrome using the command below:
  `open -n -a /Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --args --user-data-dir="/tmp/chrome_dev_test" --disable-web-security`
  in safari browser, go to developer settings, and check `Disable cross-origin restrictions`.

## Archway API

Archway API is the REST interface primally enabling functionality to it's UI counterpart but also a means for automation.

### Code

All backend code is written in Scala and follows [Scala stylistic guidelines](https://docs.scala-lang.org/style/). Project structure can be find in [Architecture description](architecture.md).

For code formation is used [Scalafmt](https://scalameta.org/scalafmt/). To format current file press `Opt + Cmd + L`(macOS) or `Ctrl + Alt + L`(other), to format whole codebase run in terminal `sbt scalafmt`.

#### Functional Programming/Cats

Some projects are based on AKKA, Play, Spark (in the case of batch), or some other "pattern library." Archway is built on Cats, Cats Effect, Http4s, Circe, and Doobie, all in the [Typelevel](http://typelevel.org) stack. These libraries are all built on Cats and encourage functional programming. If you've never used Cats or done functional programming, the good thing is, most patterns are already established and can just be repeated. All of these libraries have excellent documentation, and even better people in Gitter ready and willing to answer questions.

#### Database

Database overview can be found in [Architecture desctiption](architecture.md).

Archway support three databases:

- [Postgresql](https://www.postgresql.org)
- [MySQL](https://www.mysql.com)
- [Oracle](https://www.oracle.com/database/)

For better database schema management and schema version control is used [Flyway](https://flywaydb.org). Migration scripts are localized in `flyway` directory in specific subdirectory for each database because each one requires its own migration script.

Before running the database migrations, database instance has to be running. The easiest way how to locally run a database instance is using [Docker](https://www.docker.com). To run database migration manually run in terminal: `./flyway/flyway migrate -url=... -user=... -password=...`, however recommended way is to use preprepared commands in [Makefile](development.md#makefile).

#### Makefile

Most of the terminal scripts don't have to be run manually but can be run using Makefile, which simplifies process because it usually groups multiple commands under single one.

##### How to run makefile command:

- open terminal
- choose command from [Makefile](../Makefile)
- run `make <command name>` e.g. `make test`

### Tests

Archway contains two type of tests: unit and integration tests.

#### Unit tests

Unit tests represent simple tests which verify specific functionality. Those tests are short running and use mocks instead of real services. The tests are located in `test` directory of each module and flow the same package structure as a real implementation.

- To run all unit tests the project run `sbt test` in a terminal.
- To run one specific unit test click right on the test name and select `Run "test-name"` from context menu.
- To run all unit tests for one class, right click on class name and select `Run "class-name"`.

#### Integration tests

Integration tests represent complex tests which verify functionality against real services and it usually takes them longer time to finish. All integration tests are located in separated module `integration-test`. Those tests require [configuration](development.md#configuration) to run.

- To run all integration tests run `sbt it:test` in a terminal.
- To run one specific unit test click right on the test name and select `Run "test-name"` from context menu.
- To run all unit tests for one class, right click on class name and select `Run "class-name"`.

An integration test jar is included in the parcel at \$PARCELS_ROOT/ARCHWAY/usr/lib/archway-server/archway-test.jar

Run tests by adding your application.conf to the classpath and choosing a test:

````bash
java -cp "/path/to/application.conf:cloudera-integration/build/ARCHWAY-1.5.1/usr/lib/archway-server/*:cloudera-integration/build/ARCHWAY-1.5.1/usr/lib/archway-server-tests/*" org.scalatest.tools.Runner -o -R cloudera-integration/build/ARCHWAY-1.5.1/usr/lib/archway-server-tests/archway-integration-tests.jar -q Spec```bash

When run in a dev environment this looks like:

```bash
java -cp "common/src/test/resources/application.test.conf:integration-test/target/scala-2.12/archway-test.jar" org.scalatest.run io.phdata.clients.LDAPClientImplIntegrationSpec
````

Build the test jar locally with

```bash
make test-jar
```

## Building a CSD and Parcel locally

Set the parcel/csd version

```
export ARCHWAY_VERSION=1.5.1
```

```
make dist
```
