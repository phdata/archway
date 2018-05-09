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
                exec java -Djavax.security.auth.useSubjectCredsOnly=false \
                          -Djava.security.auth.login.config=${CONF_DIR}/jaas.conf \
                          -cp ${CONF_DIR}:/usr/share/cmf/common_jars/mysql*.jar:/usr/share/cmf/common_jars/postgres*.jar:/opt/cloudera/parcels/CDH/jars/bcprov-jdk15-1.45.jar:/opt/cloudera/parcels/CDH/lib/hive/lib/hive-jdbc-standalone.jar:`hadoop classpath`:$HEIMDALI_API_HOME/heimdali-api.jar \
                          com.heimdali.Main
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
                sed -i -e 's/%%BASE_URL%%/$HOST:$HEIMDALI_API_PORT/g' index.html
                exec python -m SimpleHTTPServer ${HEIMDALI_UI_PORT}
                ;;
            (*)
                echo ${SYNTAX}
        esac
        ;;
    (*)
        echo ${SYNTAX}
        ;;
esac