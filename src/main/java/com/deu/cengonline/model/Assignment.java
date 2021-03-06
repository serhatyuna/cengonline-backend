package com.deu.cengonline.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Set;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Table(name = "assignments")
public class Assignment extends AuditModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(min = 2, max = 100)
	private String title;

	@NotBlank
	@Column(columnDefinition = "TEXT")
	private String description;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "due_date", nullable = false)
	@DateTimeFormat(pattern = "dd.MM.yyyy HH:mm")
	@JsonFormat(pattern = "dd.MM.yyyy HH:mm")
	private Date dueDate;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@OneToMany(
		mappedBy = "assignment",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	private Set<Submission> submissions;

	protected Assignment() {
	}

	public Assignment(String title, String description) {
		this.title = title;
		this.description = description;
	}

	public Assignment(String title, String description, Date dueDate) {
		this.title = title;
		this.description = description;
		this.dueDate = dueDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Set<Submission> getSubmissions() {
		return submissions;
	}

	public void setSubmissions(Set<Submission> submissions) {
		this.submissions = submissions;
	}

	public void addSubmission(Submission submission) {
		submissions.add(submission);
	}
}
