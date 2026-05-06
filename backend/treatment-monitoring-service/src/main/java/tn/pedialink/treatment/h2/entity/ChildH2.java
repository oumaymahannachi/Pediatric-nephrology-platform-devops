package tn.pedialink.treatment.h2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pedialink.treatment.h2.util.StringListConverter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "children")
public class ChildH2 {

    @Id
    @Column(name = "mongo_id", nullable = false, unique = true)
    private String mongoId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    private String gender;

    @Column(name = "parent_id")
    private String parentId;

    @Builder.Default
    @Convert(converter = StringListConverter.class)
    @Column(name = "doctor_ids", length = 2000)
    private List<String> doctorIds = new ArrayList<>();

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
