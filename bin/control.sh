#!/bin/bash -x
SYNTAX="Please use the following arguments: [api start|migrate_db]"
COMPONENT=$1
CMD=$2

KEYTAB_FILE="${CONF_DIR}/heimdali.keytab"
JAAS_CONFIGS="
com.sun.security.jgss.krb5.initiate {
   com.sun.security.auth.module.Krb5LoginModule required
   doNotPrompt=true
   useKeyTab=true
   storeKey=true
   keyTab=\"$KEYTAB_FILE\"
   principal=\"$HEIMDALI_API_SERVICE_PRINCIPAL\";
};"

echo $JAAS_CONFIGS > ${CONF_DIR}/jaas.conf

JAVA_OPTS=""

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
HEIMDALI_CLASSPATH="${HEIMDALI_CLASSPATH:-${CONF_DIR}:${MYSQL_JAR}:${BOUNCY_JAR}:${PG_JAR}:${HIVE_JARS}:${SENTRY_JARS}:${HADOOP_JARS}:${HEIMDALI_ADDITIONAL_CLASSPATH}:${HEIMDALI_API_HOME}/heimdali-api.jar}"

case ${COMPONENT} in
    (api)
        case ${CMD} in
            (start)
                cp -f generated.conf runtime.conf
                sed -i -E 's/([[:alpha:]\.]+)\=(.*)/\1\="\2"/g' runtime.conf
                sed -i -E 's/([[:alpha:]\.]+)\="(true|false)"/\1\=\2/g' runtime.conf
                sed -i -E 's/([[:alpha:]\.]+)\="[:digit:]+"/\1\=\2/g' runtime.conf
                exec $JAVA_HOME/bin/java -Djavax.security.auth.useSubjectCredsOnly=false \
                          -Djava.security.auth.login.config=${CONF_DIR}/jaas.conf \
                          -Dconfig.resource=production.conf \
                          $JAVA_OPTS \
                          $CSD_JAVA_OPTS \
                          -cp $HEIMDALI_CLASSPATH \
                          com.heimdali.Server
                ;;
            (*)
                echo ${SYNTAX}
                ;;
        esac
        ;;
    (migrate_db)
        FLYWAY_DIR=$HEIMDALI_DIST/usr/lib/flyway/
        DATABASE_SCRIPT_DIR=""

        if [[ $DB_URL == *"mysql"* ]]; then
            DATABASE_SCRIPT_DIR="mysql"
        elif [[ $DB_URL == *"postgres"* ]]; then
            DATABASE_SCRIPT_DIR="sql"
        fi

        FLYWAY_EXECUTABLE="$FLYWAY_DIR/flyway"
        SCRIPTS_LOCATION="$FLYWAY_DIR/$DATABASE_SCRIPT_DIR"

        exec env FLYWAY_LOCATIONS="filesystem:$SCRIPTS_LOCATION" "$FLYWAY_EXECUTABLE" migrate -url="$DB_URL" -user="$DB_USERNAME" -password="$DB_PASSWORD"
        ;;
    (*)
        echo ${SYNTAX}
        ;;
esac
