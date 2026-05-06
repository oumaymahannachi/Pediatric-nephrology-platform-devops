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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("appointments")
public class Appointment {

    @Id
    private String id;

    private String childId;
    private String doctorId;
    private String parentId;
    private String dateTime;
    private String proposedDateTime;

    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    private String reason;
    private String parentNotes;
    private String doctorNotes;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
