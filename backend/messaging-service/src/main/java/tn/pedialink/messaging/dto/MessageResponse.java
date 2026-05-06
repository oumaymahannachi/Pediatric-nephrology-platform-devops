package tn.pedialink.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pedialink.messaging.model.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private Message.SenderType senderType;
    private String content;
    private List<Message.Attachment> attachments;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    // New fields
    private Message.Priority priority;
    private String replyToId;
    private MessageResponse replyTo;
    private Map<String, List<String>> reactions;
    private Boolean isDeleted;
    private LocalDateTime editedAt;
    private List<String> editHistory;
}
