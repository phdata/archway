set -x

export HEIMDALI_LDAP_GROUP_PATH="ou=groups,ou=Heimdali,DC=phdata,DC=io"
export HEIMDALI_SECRET=NDg0MTZhMWI0MzI33zRmNjg1YzcxMWFm
export HEIMDALI_HDFS_DS_ROOT=/data/governed
export HEIMDALI_LDAP_PORT=389
export HEIMDALI_CM_ADMIN_PASSWORD=Jotunn123!
export HEIMDALI_DB_USER=root
export HEIMDALI_DB_PASS=my-secret-pw
export HEIMDALI_API_SERVICE_PRINCIPAL=heimdali_api/edge1.valhalla.phdata.io@PHDATA.IO
export HEIMDALI_CLUSTER_NAME=cluster
export HEIMDALI_CM_URL_BASE=https://manager.valhalla.phdata.io:7183/api/v14
export HEIMDALI_UI_HOME=/opt/cloudera/parcels/HEIMDALI-2018.04.61/usr/lib/heimdali-ui
export HEIMDALI_API_HOME=/opt/cloudera/parcels/HEIMDALI-2018.04.61/usr/lib/heimdali-api
export HEIMDALI_REST_PORT=8080
export HEIMDALI_LDAP_BASE_DN="DC=phdata,DC=io"
export HEIMDALI_HDFS_SW_ROOT=/data/shared_workspaces
export HEIMDALI_LDAP_ADMIN_PASS=zp7CuBQLYYdDczBx
export HEIMDALI_CM_ADMIN_USER=bthompson
export HEIMDALI_DB_DRIVER=com.mysql.cj.jdbc.Driver
export HEIMDALI_HDFS_USER_ROOT=/user
export HEIMDALI_HIVE_URL="jdbc:hive2://master2.valhalla.phdata.io:10000/default;ssl=true;principal=hive/_HOST@PHDATA.IO"
export HEIMDALI_REALM=PHDATA.IO
export HEIMDALI_DB_URL=jdbc:mysql://localhost/heimdali
export HEIMDALI_LDAP_HOST=ad1.valhalla.phdata.io
export HEIMDALI_LDAP_ADMIN_DN="CN=Heimdali Admin,OU=users,OU=Heimdali,DC=phdata,DC=io"
export HEIMDALI_CLUSTER_ENVIRONMENT=dev
export HEIMDALI_KEYTAB_REFRESH=1h
export HEIMDALI_HDFS_USER_SIZE=1
export HEIMDALI_HDFS_SHARED_SIZE=1
export HEIMDALI_HDFS_DATASET_SIZE=1
export HEIMDALI_YARN_USER_CORES=1
export HEIMDALI_YARN_USER_MEMORY=1
export HEIMDALI_YARN_SHARED_CORES=1
export HEIMDALI_YARN_SHARED_MEMORY=1
export HEIMDALI_YARN_DATASET_CORES=1
export HEIMDALI_YARN_DATASET_MEMORY=1
export HEIMDALI_YARN_USER_PARENTS=root,user
export HEIMDALI_YARN_SHARED_PARENTS=root
export HEIMDALI_YARN_DATASET_PARENTS=root
export HEIMDALI_INFRA_APPROVERS="cn=edh_admin_full,ou=groups,ou=Hadoop,dc=phdata,dc=io"
export HEIMDALI_RISK_APPROVERS="cn=edh_admin_full,ou=groups,ou=Hadoop,dc=phdata,dc=io"

sbt reStart --- -agentlib:jdwp=transport=dt_socket,server=y,address=9999,suspend=n -Dhadoop.home.dir=$PWD -Djava.security.krb5.conf=/${HOME}/projects/heimdali-api/phdata-conf/krb5.conf -classpath
