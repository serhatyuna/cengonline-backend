package com.deu.cengonline.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
	@UniqueConstraint(columnNames = {
		"email"
	})
})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class User extends AuditModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(min = 2, max = 50)
	private String name;

	@NotBlank
	@Size(min = 2, max = 50)
	private String surname;

	@NaturalId
	@NotBlank
	@Size(max = 100)
	@Email
	private String email;

	@NotBlank
	@Size(min = 6, max = 100)
	@JsonIgnore
	private String password;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_roles",
		joinColumns = @JoinColumn(name = "user_id"),
		inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "enrollments",
		joinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "course_id", referencedColumnName = "id"))
	private Set<Course> enrollments = new HashSet<>();

	@OneToMany(
		mappedBy = "user",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	private Set<Submission> submissions;

	@OneToMany(
		mappedBy = "teacher",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	private Set<Course> courses;

	@OneToMany(
		mappedBy = "sender",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	@JsonIgnore
	private Set<Message> sentMessages;

	@OneToMany(
		mappedBy = "receiver",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	@JsonIgnore
	private Set<Message> receivedMessages;

	protected User() {
	}

	public User(String name, String surname, String email, String password) {
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Set<Course> getEnrollments() {
		return enrollments;
	}

	public void setEnrollments(Set<Course> enrollments) {
		this.enrollments = enrollments;
	}

	public Set<Submission> getSubmissions() {
		return submissions;
	}

	public void setSubmissions(Set<Submission> submissions) {
		this.submissions = submissions;
	}

	public Set<Course> getCourses() {
		return courses;
	}

	public void setCourses(Set<Course> courses) {
		this.courses = courses;
	}

	public Set<Message> getSentMessages() {
		return sentMessages;
	}

	public void setSentMessages(Set<Message> sentMessages) {
		this.sentMessages = sentMessages;
	}

	public Set<Message> getReceivedMessages() {
		return receivedMessages;
	}

	public void setReceivedMessages(Set<Message> receivedMessages) {
		this.receivedMessages = receivedMessages;
	}
}