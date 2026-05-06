package tn.pedialink.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pedialink.messaging.model.Message;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    private Message.SenderType senderType;
    private String content;
    private List<Message.Attachment> attachments;

    // New fields
    private Message.Priority priority;
    private String replyToId;
}
