FROM java:8

COPY ./cloudera.list /etc/apt/sources.list.d/cloudera.list
COPY ./cloudera.pref /etc/apt/preferences.d/cloudera.pref

ADD https://archive.cloudera.com/cdh5/debian/jessie/amd64/cdh/archive.key archive.key

RUN apt-key add archive.key && \
    apt-get update && \
    apt-get install -y hadoop-client

COPY ./heimdali-api /opt/heimdali-api
COPY ./hdfs/* /etc/hadoop/conf.odin/
COPY ./krb5.conf /etc/krb5.conf
COPY ./scripts/jaas.conf /etc/jaas.conf
COPY ./scripts/api_init.sh /usr/bin/api_init.sh

RUN chmod +x /usr/bin/api_init.sh

RUN update-alternatives --install /etc/hadoop/conf hadoop-conf /etc/hadoop/conf.odin 99

ENTRYPOINT /usr/bin/api_init.sh

CMD java -Djava.security.auth.login.config=/etc/jaas.conf -Duser.dir=/opt/heimdali-api -cp /opt/heimdali-api/lib/*:/etc/hadoop/conf.odin/:`hadoop classpath`:/opt/heimdali-api/conf/ play.core.server.ProdServerStart
