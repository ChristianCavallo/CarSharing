package com.carsharing.logging_microservice.data;

import com.carsharing.logging_microservice.entities.Log;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LoggingRepository extends MongoRepository<Log, String> {
    List<Log> findByKeyAndTimestampBetween(String key, long t1, long t2);
    List<Log> findByServiceAndKeyAndTimestampBetween(String service, String key, long t1, long t2);
}
