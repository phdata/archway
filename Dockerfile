FROM java:8

COPY ./heimdali-api /opt/heimdali-api

CMD /opt/heimdali-api/bin/heimdali-api
