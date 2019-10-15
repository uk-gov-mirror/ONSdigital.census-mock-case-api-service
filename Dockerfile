FROM openjdk:11-jre-slim

ARG JAR_FILE=census-mock-case-api-service*.jar
RUN apt-get update
RUN apt-get -yq clean
RUN groupadd -g 989 census-mock-case-api-service && \
    useradd -r -u 989 -g census-mock-case-api-service census-mock-case-api-service
USER census-mock-case-api-service
COPY target/$JAR_FILE /opt/census-mock-case-api-service.jar

ENTRYPOINT [ "java", "-jar", "/opt/census-mock-case-api-service.jar" ]

