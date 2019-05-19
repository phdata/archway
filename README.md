# Heimdali API
Heimdali API is the REST interface primally enabling functionality to it's UI counterpart but also a means for automation.

## Code
### Packages
The code is comprised of a few primary packages:

* REST API -  `com.heimdali.rest`
Responsible for serving requests via HTTP.
* Startup -  `com.heimdali.startup`
Responsible for managing initial (and often repeating) tasks.
* Services -  `com.heimdali.services`
Responsible for providing business logic.
* Clients -  `com.heimdali.clients`
Responsible for interacting with third party integrations like CM API.
* Repositories - `com.heimdali.repositories`
Responsible for managing interactions with the meta database.
* Provisioning - `com.heimdali.provisioning`
Responsible for applying metadata to the cluster and resources requested.
* Models - `com.heimdali.models`
Responsible for representing the domain model of the application.

### Functional Programming/Cats
Some projects are based on AKKA, Play, Spark (in the case of batch), or some other "pattern library." Heimdali is built on Cats, Cats Effect, Http4s, Circe, and Doobie, all in the [Typelevel](http://typelevel.org) stack. These libraries are all built on Cats and encourage functional programming. If you've never used Cats or done functional programming, the good thing is, most patterns are already established and can just be repeated. All of these libraries have excellent documentation, and even better people in Gitter ready and willing to answer questions (just like us on ##heimdali-dev in Slack).

## Database
The metadata for Heimdali is broken up into two main parts: workspace metadata and application configuration.

### Workspace Metadata
All workspaces can contain a collection of topics, applications, databases, and resource pools. Depending on the template used, a workspace is stored and when applied, timestamps on the related entity are updated to indicate progress.
![](metadata.png)

### Configuration
![](config.png)

## Contributing
Pull requests are welcome. For major changes, please start the discussion on [#heimdali-dev](https://phdata.slack.com/app_redirect?channel=heimdali-dev).

Please make sure to review [CONTRIBUTING.md](CONTRIBUTING.md)