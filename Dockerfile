# syntax=docker/dockerfile:1

# Java 8 is required
FROM openjdk:8-jdk-alpine

# Update and install
RUN apk update && \
    apk upgrade && \
    apk add --no-cache wget tar

# Change to workind directory
WORKDIR /citygml-change-detection

# Preparations
RUN mkdir ./neo4jDB && \
    mkdir ./output && \
    mkdir ./output/changes && \
    mkdir ./output/logs && \
    mkdir ./output/rtrees && \
    mkdir ./output/statbot && \
    wget -qO- https://github.com/tum-gis/citygml-change-detection/releases/latest/download/citygml-change-detection.tar.gz | tar xvz -C . && \
    chmod +x ./citygml-change-detection.jar

# This will be executed while running the container
ENTRYPOINT [ "java", "-jar", "./citygml-change-detection.jar" ]