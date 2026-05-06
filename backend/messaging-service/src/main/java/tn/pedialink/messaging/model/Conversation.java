package tn.pedialink.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversations")
@CompoundIndexes({
        @CompoundIndex(name = "doctor_parent_idx", def = "{'doctorId': 1, 'parentId': 1}"),
        @CompoundIndex(name = "context_idx", def = "{'contextType': 1, 'contextId': 1}")
})
public class Conversation {

    @Id
    private String id;

    private String doctorId;
    private String parentId;
    private String childId;
    
    private String subject;
    
    // Context linking (optional)
    private ContextType contextType;
    private String contextId; // ID of prescription/treatment/lab-result
    
    // Last message info for preview
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private String lastMessageSenderId;
    
    // Unread counts
    private Integer doctorUnreadCount;
    private Integer parentUnreadCount;
    
    private ConversationStatus status;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ContextType {
        PRESCRIPTION,
        TREATMENT,
        LAB_RESULT,
        GENERAL
    }

    public enum ConversationStatus {
        ACTIVE,
        ARCHIVED,
        CLOSED
    }
}
