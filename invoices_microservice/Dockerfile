FROM maven:3-jdk-8 as builder
WORKDIR /project
COPY ./invoices_microservice/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./invoices_microservice/src ./src
RUN mvn package

FROM java:8-alpine
WORKDIR /app
COPY --from=builder /project/target/invoices_microservice-0.0.1-SNAPSHOT.jar ./invoices_microservice.jar
#ENTRYPOINT ["/bin/sh", "-c"]
CMD java -jar invoices_microservice.jar
