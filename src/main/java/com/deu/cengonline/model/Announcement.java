package com.deu.cengonline.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "announcements")
public class Announcement extends AuditModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(columnDefinition = "TEXT")
	private String description;
}
