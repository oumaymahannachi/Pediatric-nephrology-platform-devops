package tn.pedialink.treatment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("growth_measurements")
public class GrowthMeasurement {

    @Id
    private String id;

    private String childId;
    private LocalDate date;
    private Double weight;
    private Double height;
    private Double bmi;
    private Double headCircumference;
    private String notes;
    private String recordedBy;

    @CreatedDate
    private Instant createdAt;

    public void calculateBmi() {
        if (weight != null && height != null && height > 0) {
            double heightInMeters = height / 100.0;
            this.bmi = Math.round((weight / (heightInMeters * heightInMeters)) * 100.0) / 100.0;
        }
    }
}
