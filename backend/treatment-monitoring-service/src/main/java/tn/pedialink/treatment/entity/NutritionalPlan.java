package tn.pedialink.treatment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("nutritional_plans")
public class NutritionalPlan {

    @Id
    private String id;

    private String childId;
    private String doctorId;
    private String title;
    private String description;
    private String startDate;
    private String endDate;

    @Builder.Default
    private String status = "ACTIVE";

    private String goals;
    private String restrictions;

    @Builder.Default
    private List<Meal> meals = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
