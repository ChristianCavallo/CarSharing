FROM maven:3-jdk-8 as builder
WORKDIR /project
COPY ./eureka_service/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./eureka_service/src ./src
RUN mvn package

FROM java:8-alpine
WORKDIR /app
COPY --from=builder /project/target/eureka_service-0.0.1-SNAPSHOT.jar ./eureka_service.jar
#ENTRYPOINT ["/bin/sh", "-c"]
CMD java -jar eureka_service.jar
