package tn.pedialink.messaging.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.messaging.model.Conversation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    List<Conversation> findByDoctorIdOrderByLastMessageTimeDesc(String doctorId);

    List<Conversation> findByParentIdOrderByLastMessageTimeDesc(String parentId);

    Optional<Conversation> findByDoctorIdAndParentIdAndChildId(String doctorId, String parentId, String childId);

    Optional<Conversation> findByContextTypeAndContextId(Conversation.ContextType contextType, String contextId);

    List<Conversation> findByDoctorIdAndStatus(String doctorId, Conversation.ConversationStatus status);

    List<Conversation> findByParentIdAndStatus(String parentId, Conversation.ConversationStatus status);
}
