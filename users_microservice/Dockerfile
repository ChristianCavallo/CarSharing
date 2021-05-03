FROM maven:3-jdk-8 as builder
WORKDIR /project
COPY ./users_microservice/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./users_microservice/src ./src
RUN mvn package

FROM java:8-alpine
WORKDIR /app
COPY --from=builder /project/target/users_microservice-0.0.1-SNAPSHOT.jar ./users_microservice.jar
#ENTRYPOINT ["/bin/sh", "-c"]
CMD java -jar users_microservice.jar
