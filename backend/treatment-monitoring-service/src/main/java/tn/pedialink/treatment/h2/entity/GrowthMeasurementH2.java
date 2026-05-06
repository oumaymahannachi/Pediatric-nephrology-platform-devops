package tn.pedialink.treatment.h2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "growth_measurements")
public class GrowthMeasurementH2 {

    @Id
    @Column(name = "mongo_id", nullable = false, unique = true)
    private String mongoId;

    @Column(name = "child_id")
    private String childId;

    private LocalDate date;
    private Double weight;
    private Double height;
    private Double bmi;

    @Column(name = "head_circumference")
    private Double headCircumference;

    @Column(length = 1000)
    private String notes;

    @Column(name = "recorded_by")
    private String recordedBy;

    @Column(name = "created_at")
    private Instant createdAt;
}
