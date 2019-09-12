#!/bin/bash
set -xeu
ARCHWAY_CLASSPATH="${ARCHWAY_SERVER_HOME}/*"
exec $JAVA_HOME/bin/java -cp $ARCHWAY_CLASSPATH io.phdata.services.BundleService "$ARTIFACTORY_TOKEN"
