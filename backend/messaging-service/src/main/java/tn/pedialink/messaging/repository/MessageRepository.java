package tn.pedialink.messaging.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.messaging.model.Message;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    Long countByConversationIdAndIsReadFalse(String conversationId);

    List<Message> findByConversationIdAndIsReadFalse(String conversationId);
}
