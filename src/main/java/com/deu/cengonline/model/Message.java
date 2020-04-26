package com.deu.cengonline.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "messages")
public class Message extends AuditModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(columnDefinition = "TEXT")
	private String content;

	@ManyToOne
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@ManyToOne
	@JoinColumn(name = "receiver_id", nullable = false)
	private User receiver;

	protected Message() {
	}

	public Message(String content) {
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public User getReceiver() {
		return receiver;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}
}
