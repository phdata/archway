FROM java:8

COPY ./heimdali-api /opt/heimdali-api
COPY ./cloudera.list /etc/apt/sources.list.d/cloudera.list
COPY ./cloudera.pref /etc/apt/preferences.d/cloudera.pref
COPY ./hdfs/* /etc/hadoop/conf.odin/
COPY ./krb5.conf /etc/krb5.conf

RUN sed -i 's/#KERBEROS_SERVICE_HOST#/$KERBEROS_SERVICE_HOST/' /etc/krb5.conf

ADD https://archive.cloudera.com/cdh5/debian/jessie/amd64/cdh/archive.key archive.key

RUN apt-key add archive.key && \
    apt-get update && \
    apt-get install -y hadoop-client

RUN update-alternatives --install /etc/hadoop/conf hadoop-conf /etc/hadoop/conf.odin 99

CMD java -Duser.dir=/opt/heimdali-api -cp /opt/heimdali-api/lib/*:/etc/hadoop/conf.odin/:`hadoop classpath`:/opt/heimdali-api/conf/ play.core.server.ProdServerStart
