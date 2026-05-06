package tn.pedialink.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    @Indexed
    private String conversationId;

    private String senderId;
    private SenderType senderType;

    @TextIndexed
    private String content;

    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    private Boolean isRead;
    private LocalDateTime readAt;

    // Priority
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    // Reply/Quote
    private String replyToId;

    // Reactions: emoji -> list of userIds
    @Builder.Default
    private Map<String, List<String>> reactions = new HashMap<>();

    // Edit/Delete
    @Builder.Default
    private Boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private LocalDateTime editedAt;
    @Builder.Default
    private List<String> editHistory = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    public enum SenderType {
        DOCTOR,
        PARENT
    }

    public enum Priority {
        NORMAL,
        URGENT
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String fileName;
        private String fileUrl;
        private String fileType;
        private Long fileSize;
    }
}
