## Running Locally

_NOTE_ Make sure you have `npm` and IntelliJ installed.

### Configuration

In order to run the API locally, you'll need to prepare your development environment.

This will download the integration test repository which contains credentials and place them in `itest-config`, as well
as softlink the application config to the API resources so the application can be run locally.

```bash
$ make itest-init
```

### Run The App (API-focused development)

- Open the project in IntelliJ
- Open `io.phdata.Server` and click the green "play" button next to the `object` definition
- The first run won't work, because we need to change the newly created run configuration
- Open the run configuration drop down and click "Edit Configurations..."
- Select the "Server" configuration and enable "Include dependencies with 'Provided' scope"
- Restart the app
- Open the "Terminal" tab
- Run `npm i` which will install the npm dependencies
- Run `npm start` which will spin up the UI

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

#### Packages

The code is comprised of a few primary packages:

- REST API - `io.phdata.rest`
  Responsible for serving requests via HTTP.
- Startup - `io.phdata.startup`
  Responsible for managing initial (and often repeating) tasks.
- Services - `io.phdata.services`
  Responsible for providing business logic.
- Clients - `io.phdata.clients`
  Responsible for interacting with third party integrations like CM API.
- Repositories - `io.phdata.repositories`
  Responsible for managing interactions with the meta database.
- Provisioning - `io.phdata.provisioning`
  Responsible for applying metadata to the cluster and resources requested.
- Models - `io.phdata.models`
  Responsible for representing the domain model of the application.

#### Functional Programming/Cats

Some projects are based on AKKA, Play, Spark (in the case of batch), or some other "pattern library." Archway is built on Cats, Cats Effect, Http4s, Circe, and Doobie, all in the [Typelevel](http://typelevel.org) stack. These libraries are all built on Cats and encourage functional programming. If you've never used Cats or done functional programming, the good thing is, most patterns are already established and can just be repeated. All of these libraries have excellent documentation, and even better people in Gitter ready and willing to answer questions (just like us on ##archway-dev in Slack).

### Database

The metadata for Archway is broken up into two main parts: workspace metadata and application configuration.

#### Workspace Metadata

All workspaces can contain a collection of topics, applications, databases, and resource pools. Depending on the template used, a workspace is stored and when applied, timestamps on the related entity are updated to indicate progress.
![](metadata.png)

#### Configuration

![](config.png)

### Integration test package

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
make package-tests
```

Create integration tests with `make package-tests`.

## Building a CSD and Parcel locally

Set the parcel/csd version

```
export ARCHWAY_VERSION=1.5.1
```

```
make dist
```
