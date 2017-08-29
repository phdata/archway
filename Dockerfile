FROM java:8

COPY ./target/universal/heimdali-api /opt/heimdali-api

CMD /opt/heimdali-api/bin/heimdali-api
