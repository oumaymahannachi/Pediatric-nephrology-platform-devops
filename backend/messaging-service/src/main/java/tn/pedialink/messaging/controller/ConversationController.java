package tn.pedialink.messaging.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.messaging.dto.ApiResponse;
import tn.pedialink.messaging.dto.ConversationResponse;
import tn.pedialink.messaging.dto.CreateConversationRequest;
import tn.pedialink.messaging.service.ConversationService;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @Valid @RequestBody CreateConversationRequest request) {
        try {
            log.info("Creating conversation");
            ConversationResponse response = conversationService.createConversation(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Conversation created successfully", response));
        } catch (Exception e) {
            log.error("Error creating conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create conversation: " + e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getDoctorConversations(
            @PathVariable String doctorId) {
        try {
            log.info("Getting conversations for doctor: {}", doctorId);
            List<ConversationResponse> conversations = conversationService.getDoctorConversations(doctorId);
            return ResponseEntity.ok(ApiResponse.ok("Conversations retrieved successfully", conversations));
        } catch (Exception e) {
            log.error("Error getting doctor conversations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get conversations: " + e.getMessage()));
        }
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getParentConversations(
            @PathVariable String parentId) {
        try {
            log.info("Getting conversations for parent: {}", parentId);
            List<ConversationResponse> conversations = conversationService.getParentConversations(parentId);
            return ResponseEntity.ok(ApiResponse.ok("Conversations retrieved successfully", conversations));
        } catch (Exception e) {
            log.error("Error getting parent conversations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get conversations: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @PathVariable String id) {
        try {
            log.info("Getting conversation: {}", id);
            ConversationResponse conversation = conversationService.getConversationById(id);
            return ResponseEntity.ok(ApiResponse.ok("Conversation retrieved successfully", conversation));
        } catch (Exception e) {
            log.error("Error getting conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get conversation: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/mark-read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String id,
            @RequestParam boolean isDoctor) {
        try {
            log.info("Marking conversation as read: {}", id);
            conversationService.markAsRead(id, isDoctor);
            return ResponseEntity.ok(ApiResponse.ok("Conversation marked as read", null));
        } catch (Exception e) {
            log.error("Error marking conversation as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark as read: " + e.getMessage()));
        }
    }
}
