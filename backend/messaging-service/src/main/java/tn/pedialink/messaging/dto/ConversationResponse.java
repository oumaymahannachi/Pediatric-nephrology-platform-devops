package tn.pedialink.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pedialink.messaging.model.Conversation;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String id;
    private String doctorId;
    private String parentId;
    private String childId;
    private String subject;
    
    private Conversation.ContextType contextType;
    private String contextId;
    
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private String lastMessageSenderId;
    
    private Integer doctorUnreadCount;
    private Integer parentUnreadCount;
    
    private Conversation.ConversationStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Enhanced fields for display
    private String doctorName;
    private String parentName;
    private String childName;
}
