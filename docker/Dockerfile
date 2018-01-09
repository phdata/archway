FROM java:8

COPY ./cloudera.list /etc/apt/sources.list.d/cloudera.list
COPY ./cloudera.pref /etc/apt/preferences.d/cloudera.pref

ADD https://archive.cloudera.com/cdh5/debian/jessie/amd64/cdh/archive.key archive.key

RUN apt-key add archive.key && \
    apt-get update && \
    apt-get install -y hadoop-client

COPY ./heimdali-api.jar /opt/heimdali-api.jar
COPY ./hdfs/* /etc/hadoop/conf.odin/
COPY ./krb5.conf /etc/krb5.conf
COPY ./jaas.conf /etc/jaas.conf
COPY ./hdfs.keytab /opt/hdfs.keytab

RUN update-alternatives --install /etc/hadoop/conf hadoop-conf /etc/hadoop/conf.odin 99

CMD java -Djava.security.auth.login.config=/etc/jaas.conf -cp /opt/heimdali-api.jar:`hadoop classpath`:/etc/hadoop/conf.odin com.heimdali.Main
