FROM openjdk:8-jdk-alpine
MAINTAINER magno.mabreu@gmail.com
RUN mkdir /foms/
COPY ./foms /foms/
COPY ./target/flightradarproxy-1.0.war /opt/portico-2.1.0/
WORKDIR /opt/portico-2.1.0/
ENTRYPOINT ["java"]
ENV LANG=pt_BR.utf8 
CMD ["-jar", "/opt/portico-2.1.0/flightradarproxy-1.0.war"]