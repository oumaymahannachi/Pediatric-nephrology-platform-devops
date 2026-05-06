package tn.pedialink.messaging.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.messaging.dto.ApiResponse;
import tn.pedialink.messaging.dto.MessageResponse;
import tn.pedialink.messaging.dto.SendMessageRequest;
import tn.pedialink.messaging.service.MessageService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        try {
            MessageResponse response = messageService.sendMessage(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Message sent successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to send message: " + e.getMessage()));
        }
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getConversationMessages(@PathVariable String conversationId) {
        try {
            List<MessageResponse> messages = messageService.getConversationMessages(conversationId);
            return ResponseEntity.ok(ApiResponse.ok("Messages retrieved successfully", messages));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to get messages: " + e.getMessage()));
        }
    }

    @GetMapping("/conversation/{conversationId}/paginated")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getConversationMessagesPaginated(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Page<MessageResponse> messages = messageService.getConversationMessagesPaginated(conversationId, page, size);
            return ResponseEntity.ok(ApiResponse.ok("Messages retrieved successfully", messages));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to get messages: " + e.getMessage()));
        }
    }

    @PutMapping("/conversation/{conversationId}/mark-read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @PathVariable String conversationId,
            @RequestParam String userId) {
        try {
            messageService.markMessagesAsRead(conversationId, userId);
            return ResponseEntity.ok(ApiResponse.ok("Messages marked as read", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to mark messages as read: " + e.getMessage()));
        }
    }

    @GetMapping("/conversation/{conversationId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable String conversationId) {
        try {
            Long count = messageService.getUnreadCount(conversationId);
            return ResponseEntity.ok(ApiResponse.ok("Unread count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to get unread count: " + e.getMessage()));
        }
    }

    // Search messages
    @GetMapping("/conversation/{conversationId}/search")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> searchMessages(
            @PathVariable String conversationId,
            @RequestParam String query) {
        try {
            List<MessageResponse> results = messageService.searchMessages(conversationId, query);
            return ResponseEntity.ok(ApiResponse.ok("Search results", results));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    // Add/toggle reaction
    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<ApiResponse<MessageResponse>> addReaction(
            @PathVariable String messageId,
            @RequestBody Map<String, String> body) {
        try {
            String emoji = body.get("emoji");
            String userId = body.get("userId");
            MessageResponse response = messageService.addReaction(messageId, emoji, userId);
            return ResponseEntity.ok(ApiResponse.ok("Reaction updated", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to add reaction: " + e.getMessage()));
        }
    }

    // Edit message
    @PutMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @PathVariable String messageId,
            @RequestBody Map<String, String> body) {
        try {
            String senderId = body.get("senderId");
            String content = body.get("content");
            MessageResponse response = messageService.editMessage(messageId, senderId, content);
            return ResponseEntity.ok(ApiResponse.ok("Message edited", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to edit message: " + e.getMessage()));
        }
    }

    // Delete message
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteMessage(
            @PathVariable String messageId,
            @RequestParam String senderId) {
        try {
            MessageResponse response = messageService.deleteMessage(messageId, senderId);
            return ResponseEntity.ok(ApiResponse.ok("Message deleted", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to delete message: " + e.getMessage()));
        }
    }
}
