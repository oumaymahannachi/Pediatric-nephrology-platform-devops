package tn.pedialink.treatment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meal {

    private String name;
    private String time;
    private String description;
    private String calories;
    private String notes;
}
