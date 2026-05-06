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
@Table(name = "appointments")
public class AppointmentH2 {

    @Id
    @Column(name = "mongo_id", nullable = false, unique = true)
    private String mongoId;

    @Column(name = "child_id")
    private String childId;

    @Column(name = "doctor_id")
    private String doctorId;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "date_time")
    private String dateTime;

    @Column(name = "proposed_date_time")
    private String proposedDateTime;

    @Builder.Default
    private String status = "PENDING";

    @Column(length = 1000)
    private String reason;

    @Column(name = "parent_notes", length = 1000)
    private String parentNotes;

    @Column(name = "doctor_notes", length = 1000)
    private String doctorNotes;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
