FROM maven:3-jdk-8 as builder
WORKDIR /project
COPY ./rentals_microservice/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./rentals_microservice/src ./src
RUN mvn package

FROM java:8-alpine
WORKDIR /app
COPY --from=builder /project/target/rentals_microservice-0.0.1-SNAPSHOT.jar ./rentals_microservice.jar
#ENTRYPOINT ["/bin/sh", "-c"]
CMD java -jar rentals_microservice.jar
