FROM openjdk:8

COPY distribution/ distribution
EXPOSE 80
WORKDIR /distribution
ENV JAVA_APP_DIR /distribution
ENV JAVA_MAIN_CLASS "io.phdata.Server"
ENV JAVA_MAX_MEM_RATIO 55
ENV PORT "${PORT:-80}"
CMD /bin/sh run-java.sh -D
