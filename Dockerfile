FROM java:8

COPY ./heimdali-api /opt/heimdali-api
COPY ./docker/cloudera.list /etc/apt/sources.list.d/cloudera.list
COPY ./docker/hdfs/* /etc/hadoop/conf.odin/

ADD https://archive.cloudera.com/cdh5/debian/jessie/amd64/cdh/archive.key archive.key

RUN apt-key add archive.key
RUN apt-get update
RUN apt-get install -y -f hadoop-client

ENV CLASSPATH "/etc/hadoop/conf.odin/*:/usr/lib/hdfs/lib/*"

CMD /opt/heimdali-api/bin/heimdali-api
