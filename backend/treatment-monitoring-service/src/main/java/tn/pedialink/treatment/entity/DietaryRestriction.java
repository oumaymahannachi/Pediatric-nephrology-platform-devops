package tn.pedialink.treatment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("dietary_restrictions")
public class DietaryRestriction {

    @Id
    private String id;

    private String childId;
    private String type;
    private String allergen;
    private String severity;
    private String description;
    private String notes;

    @CreatedDate
    private Instant createdAt;
}
