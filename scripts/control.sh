#!/bin/bash -x
SYNTAX="Please use the following arguments: [api|ui] [start|stop]"
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

case ${COMPONENT} in
    (api)
        case ${CMD} in
            (start)
                sed -i -E 's/([[:alpha:]\.]+)\=(.*)/\1\="\2"/g' runtime.conf
                sed -i -E 's/([[:alpha:]\.]+)\="(true|false)"/\1\=\2/g' runtime.conf
                sed -i -E 's/([[:alpha:]\.]+)\="[:digit:]+"/\1\=\2/g' runtime.conf
                exec $JAVA_HOME/bin/java -Djavax.security.auth.useSubjectCredsOnly=false \
                          -Djava.security.auth.login.config=${CONF_DIR}/jaas.conf \
                          -Dconfig.resource=production.conf \
                          -cp ${CONF_DIR}:/usr/share/java/mysql-connector-java.jar:/usr/share/cmf/common_jars/postgres*.jar:/opt/cloudera/parcels/CDH/jars/bcprov-jdk15-1.45.jar:/opt/cloudera/parcels/CDH/lib/hive/lib/hive-jdbc-standalone.jar:/opt/cloudera/parcels/CDH/lib/sentry/lib/*:`hadoop classpath`:$HEIMDALI_ADDITIONAL_CLASSPATH:$HEIMDALI_API_HOME/heimdali-api.jar \
                          com.heimdali.Server
                ;;
            (*)
                echo ${SYNTAX}
                ;;
        esac
        ;;
    (ui)
        case ${CMD} in
            (start)
                cd $HEIMDALI_UI_HOME
                sed -i -e "s@%%BASE_URL%%@https://$(hostname -f):$HEIMDALI_REST_PORT@g" index.html
                exec python $CONF_DIR/scripts/ui-server.py
                ;;
            (*)
                echo ${SYNTAX}
        esac
        ;;
    (*)
        echo ${SYNTAX}
        ;;
esac
