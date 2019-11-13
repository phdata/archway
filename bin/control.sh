#!/bin/bash -x
SYNTAX="Please use the following arguments: [server start]"
COMPONENT=$1
CMD=$2

KEYTAB_FILE="${CONF_DIR}/archway.keytab"
JAAS_CONFIGS="
com.sun.security.jgss.krb5.initiate {
   com.sun.security.auth.module.Krb5LoginModule required
   doNotPrompt=true
   useKeyTab=true
   storeKey=true
   keyTab=\"$KEYTAB_FILE\"
   principal=\"$ARCHWAY_SERVICE_PRINCIPAL\";
};"

echo $JAAS_CONFIGS > ${CONF_DIR}/jaas.conf

if [ ! -z "$JAVA_TRUST_STORE_LOCATION" ]; then
    JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=$JAVA_TRUST_STORE_LOCATION"
fi

if [ ! -z $JAVA_TRUST_STORE_PASSWORD ]; then
    JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStorePassword=$JAVA_TRUST_STORE_PASSWORD"
fi

echo "Using Java options: $JAVA_OPTS"

MYSQL_JAR="${MYSQL_JAR:-/usr/share/java/mysql-connector-java.jar}"
PG_JAR=${PG_JAR:-$(echo /usr/share/cmf/common_jars/postgres*.jar | tr ' ' ':')}
BOUNCY_JAR=${BOUNCY_JAR:-`find $PARCELS_ROOT/CDH/jars/ -name "bcprov-jdk*.jar"`}
HIVE_JARS=${HIVE_JARS:-"$PARCELS_ROOT/CDH/lib/hive/lib/hive-jdbc.jar:$PARCELS_ROOT/CDH/lib/hive/lib/hive-jdbc-standalone.jar"}
HADOOP_JARS=`hadoop classpath`
SENTRY_JARS="${SENTRY_JARS:-$PARCELS_ROOT/CDH/lib/sentry/lib/*}"
ARCHWAY_CLASSPATH="${ARCHWAY_CLASSPATH:-${CONF_DIR}:${MYSQL_JAR}:${BOUNCY_JAR}:${PG_JAR}:${HIVE_JARS}:${SENTRY_JARS}:${HADOOP_JARS}:${ARCHWAY_ADDITIONAL_CLASSPATH}:${ARCHWAY_SERVER_HOME}/archway-server.jar}"

cp -r $PARCELS_ROOT/ARCHWAY/usr/lib/archway-ui/images images

case ${COMPONENT} in
    (server)
        case ${CMD} in
            (start)
                FLYWAY_DIR=$ARCHWAY_DIST/usr/lib/flyway
                DATABASE_SCRIPT_DIR=""

                if [[ $DB_URL == *"mysql"* ]]; then
                    DATABASE_SCRIPT_DIR="mysql"
                elif [[ $DB_URL == *"postgres"* ]]; then
                    DATABASE_SCRIPT_DIR="sql"
                elif [[ $DB_URL == *"oracle"* ]]; then
                    DATABASE_SCRIPT_DIR="oracle"
                fi

                FLYWAY_EXECUTABLE="$FLYWAY_DIR/flyway"
                SCRIPTS_LOCATION="$FLYWAY_DIR/$DATABASE_SCRIPT_DIR"

                env FLYWAY_LOCATIONS="filesystem:$SCRIPTS_LOCATION" "$FLYWAY_EXECUTABLE" migrate -url="$DB_URL" -user="$DB_USERNAME" -password="$DB_PASSWORD" || exit 1

                cp -f generated.conf runtime.conf
                sed -i -E 's/([[:alpha:]\.]+)\=(.*)/\1\="\2"/g' runtime.conf
                sed -i -E 's/([[:alpha:]\.]+)\="(true|false)"/\1\=\2/g' runtime.conf
                sed -i -E 's/([[:alpha:]\.]+)\="[:digit:]+"/\1\=\2/g' runtime.conf
                exec $JAVA_HOME/bin/java -Djavax.security.auth.useSubjectCredsOnly=false \
                          -Dscalate.workdir=. \
                          -Djava.security.auth.login.config="${CONF_DIR}/jaas.conf" \
                          -Dconfig.resource=production.conf \
                          -Dlog4j.properties=file:"${CONF_DIR}/log4j.properties" \
                          $JAVA_OPTS \
                          $CSD_JAVA_OPTS \
                          -cp $ARCHWAY_CLASSPATH \
                          io.phdata.Server
                ;;
            (*)
                echo ${SYNTAX}
                ;;
        esac
        ;;
    (*)
        echo ${SYNTAX}
        ;;
esac
