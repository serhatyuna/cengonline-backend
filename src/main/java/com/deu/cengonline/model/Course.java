package com.deu.cengonline.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name = "courses")
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class Course extends AuditModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(min = 2, max = 100)
	private String title;

	@NotBlank
	@Size(min = 2, max = 100)
	private String term;

	@OneToMany(mappedBy = "course")
	@JsonIgnore
	private Set<Announcement> announcements;

	@OneToMany(mappedBy = "course")
	private Set<Assignment> assignments;

	@ManyToMany(mappedBy = "enrollments")
	private Set<User> students;

	@ManyToOne
	@JoinColumn(name = "teacher_id", nullable = false)
	private User teacher;

	protected Course() {
	}

	public Course(String title, String term) {
		this.title = title;
		this.term = term;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getTitle() {
		return title;
	}

	public String getTerm() {
		return term;
	}

	public Set<Announcement> getAnnouncements() {
		return announcements;
	}

	public void setAnnouncements(Set<Announcement> announcements) {
		this.announcements = announcements;
	}

	public Set<Assignment> getAssignments() {
		return assignments;
	}

	public void setAssignments(Set<Assignment> assignments) {
		this.assignments = assignments;
	}

	public Set<User> getStudents() {
		return students;
	}

	public void setStudents(Set<User> students) {
		this.students = students;
	}

	public User getTeacher() {
		return teacher;
	}

	public void setTeacher(User teacher) {
		this.teacher = teacher;
	}
}
