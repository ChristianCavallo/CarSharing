package com.carsharing.cars_microservice.service;

import com.carsharing.cars_microservice.data.CarRepository;
import com.carsharing.cars_microservice.entities.Car;
import com.carsharing.cars_microservice.entities.CarStatus;
import entities.car.CarOperation;
import entities.car.CarRequest;
import entities.car.CarResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

@Service
public class CarService {

    @Autowired
    CarRepository repository;

    @Value("${min_distance}")
    private Integer min_dist;


    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CarResponse SwitchCarStatus(CarRequest request) {
        CarResponse response = new CarResponse();
        response.setCar_id(request.getCar_id());
        response.setSuccess(false);

        Optional<Car> c = repository.findById(request.getCar_id());

        if (!c.isPresent()) {
            response.setMessage("Car not found");
            return response;
        }

        if (GetDistance(c.get(), request.getLat(), request.getLon()) > min_dist) {
            response.setMessage("User too far from the car");
            return response;
        }

        if (!request.getCarCode().equals(c.get().getCode())) {
            response.setMessage("Wrong car code");
            return response;
        }

        if (request.getOperation() == CarOperation.LOCK) {
            if (c.get().getCarStatus() == CarStatus.LOCKED) {
                response.setMessage("Car already locked");
                return response;
            }

            c.get().setCarStatus(CarStatus.LOCKED);

        } else {

            if (c.get().getCarStatus() == CarStatus.UNLOCKED) {
                response.setMessage("Car already unlocked");
                return response;
            }

            c.get().setCarStatus(CarStatus.UNLOCKED);
        }

        repository.save(c.get());

        response.setMessage("Operation " + request.getOperation().toString() + " done successfully");
        response.setSuccess(true);
        return response;
    }

    public Car AddCar(Car c) {
        return repository.save(c);
    }

    //Calcolare la distanza geometrica tra le auto e il client sulla base dei parametri “lat” e “long”.
    public Double GetDistance(Car c, Double userLat, Double userLong) {
        Integer R = 6371;
        Double pigreco = 3.1415927;

        /* Converte i gradi in radianti */
        Double lat_alfa = pigreco * userLat / 180;
        Double lat_beta = pigreco * c.getLat() / 180;
        Double lon_alfa = pigreco * userLong / 180;
        Double lon_beta = pigreco * c.getLon() / 180;

        /* Calcola l'angolo compreso fi */
        Double fi = Math.abs(lon_alfa - lon_beta);

        /* Calcola il terzo lato del triangolo sferico */
        Double p = Math.acos(sin(lat_beta) * sin(lat_alfa) + cos(lat_beta) * cos(lat_alfa) * cos(fi));

        /* Calcola la distanza sulla superficie terrestre R = ~6371 km */

        Double d = p * R;
        return d;
    }

    public List<Car> SearchCarsByPosition(String pos) {
        return repository.findByLocation(pos);
    }


    public Optional<Car> SearchCarById(String car_id) {
        return repository.findByCarId(car_id);
    }


}
