package tn.pedialink.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.pedialink.messaging.model.Conversation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {

    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    @NotBlank(message = "Parent ID is required")
    private String parentId;

    @NotBlank(message = "Child ID is required")
    private String childId;

    private String subject;

    // Optional context
    private Conversation.ContextType contextType;
    private String contextId;
}
