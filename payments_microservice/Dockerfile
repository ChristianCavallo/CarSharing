FROM maven:3-jdk-8 as builder
WORKDIR /project
COPY ./payments_microservice/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./payments_microservice/src ./src
RUN mvn package

FROM java:8-alpine
WORKDIR /app
COPY --from=builder /project/target/payments_microservice-0.0.1-SNAPSHOT.jar ./payments_microservice.jar
#ENTRYPOINT ["/bin/sh", "-c"]
CMD java -jar payments_microservice.jar
