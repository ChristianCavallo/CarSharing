package entities.car;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor
public class CarResponse implements Serializable {
    private String car_id;
    private Boolean success;
    private String message;
}
