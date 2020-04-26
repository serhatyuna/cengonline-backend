package com.deu.cengonline.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "submissions")
public class Submission extends AuditModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private Assignment assignment;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String content;

    protected Submission() {
    }

    public Submission(String content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
