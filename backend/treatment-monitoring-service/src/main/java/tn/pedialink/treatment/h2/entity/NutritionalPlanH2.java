package tn.pedialink.treatment.h2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pedialink.treatment.entity.Meal;
import tn.pedialink.treatment.h2.util.MealsJsonConverter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nutritional_plans")
public class NutritionalPlanH2 {

    @Id
    @Column(name = "mongo_id", nullable = false, unique = true)
    private String mongoId;

    @Column(name = "child_id")
    private String childId;

    @Column(name = "doctor_id")
    private String doctorId;

    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @Builder.Default
    private String status = "ACTIVE";

    @Column(length = 2000)
    private String goals;

    @Column(length = 2000)
    private String restrictions;

    @Builder.Default
    @Convert(converter = MealsJsonConverter.class)
    @Column(name = "meals_json", length = 10000)
    private List<Meal> meals = new ArrayList<>();

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
