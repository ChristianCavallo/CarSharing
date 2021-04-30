package entities.car;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CarRequest implements Serializable {

    private String car_id;
    private String user_id;
    private String rental_id;
    private CarOperation operation;
    private Double lat;
    private Double lon;
    private String carCode;

}
