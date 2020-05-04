package com.deu.cengonline.repository;

import com.deu.cengonline.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	List<Message> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByCreatedAt(Long sender_id, Long receiver_id, Long receiver_id2, Long sender_id2);
}