# Impala dependency

Note: if you update this Impala JDBC jar you may need to remove the log4j bundled in it to fix
duplication in `sbt assembly`

These were the steps for removing log4j from the jar `ImpalaJDBC41.jar`

```bash
unzip ImpalaJDBC41.jar
# Remove the old jar so we don't zip it within itself
rm ImpalaJDBC41.jar
rm -r org # log4j was the only library in 'org'
zip -r ImpalaJDBC41-no-log4j.jar .

```
