FROM maven:3-jdk-8 as builder
WORKDIR /project
COPY ./gateway/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./gateway/src ./src
RUN mvn package

FROM java:8-alpine
WORKDIR /app
COPY --from=builder /project/target/gateway-0.0.1-SNAPSHOT.jar ./gateway.jar
#ENTRYPOINT ["/bin/sh", "-c"]
CMD java -jar gateway.jar
