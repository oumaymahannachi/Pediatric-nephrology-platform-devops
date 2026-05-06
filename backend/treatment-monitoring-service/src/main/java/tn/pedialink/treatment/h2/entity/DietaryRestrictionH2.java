package tn.pedialink.treatment.h2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dietary_restrictions")
public class DietaryRestrictionH2 {

    @Id
    @Column(name = "mongo_id", nullable = false, unique = true)
    private String mongoId;

    @Column(name = "child_id")
    private String childId;

    private String type;
    private String allergen;
    private String severity;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at")
    private Instant createdAt;
}
