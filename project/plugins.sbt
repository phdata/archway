addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("org.scalatra.scalate" % "sbt-scalate-precompiler" % "1.9.0.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")