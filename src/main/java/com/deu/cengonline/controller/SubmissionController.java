package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.*;
import com.deu.cengonline.repository.*;
import com.deu.cengonline.security.jwt.JwtProvider;
import com.deu.cengonline.security.services.UserPrinciple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.deu.cengonline.util.ErrorMessage.ERRORS;
import static com.deu.cengonline.util.ErrorName.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtProvider jwtProvider;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	AssignmentRepository assignmentRepository;

	@Autowired
	SubmissionRepository submissionRepository;

	@GetMapping()
	@PreAuthorize("hasRole('TEACHER')")    // get all submissions
	public ResponseEntity<?> getAllSubmissions() {
		List<Submission> list = submissionRepository.findAll();
		return ResponseEntity.ok(list);
	}

	@GetMapping("/assignment/{assignment-id}")  // get all submissions of an assignment with given id.
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<?> getAllSubmissionsByAssignmentID(@PathVariable(value = "assignment-id") Long assignmentID) {
		Optional<Assignment> assignment = assignmentRepository.findById(assignmentID);

		if (!assignment.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(ASSIGNMENT_NOT_FOUND), assignmentID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Set<Submission> submissions = assignment.get().getSubmissions();
		return ResponseEntity.ok(submissions);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')") // get an submission with id
	public ResponseEntity<?> getSubmissionById(@PathVariable(value = "id") Long submissionID) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String loggedInEmail = ((UserPrinciple) principal).getEmail();
		Optional<User> loggedInUser = userRepository.findByEmail(loggedInEmail);

		Optional<Submission> submission = submissionRepository.findById(submissionID);

		if (!submission.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(SUBMISSION_NOT_FOUND), submissionID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Long loggedInUserID = loggedInUser.get().getId();
		Long submittedUserID = submission.get().getUser().getId();

		if (!submittedUserID.equals(loggedInUserID)) {
			Response response = new Response(HttpStatus.METHOD_NOT_ALLOWED, ERRORS.get(NOT_ALLOWED_GET));
			return new ResponseEntity<>(response, response.getStatus());
		}

		return ResponseEntity.ok(submission.get());
	}

	@GetMapping("/student/{id}")
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')") // get all submissions of a student
	public ResponseEntity<?> getSubmissionsOfStudentById(@PathVariable(value = "id") Long studentID) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Long loggedInID = ((UserPrinciple) principal).getId();
		String loggedInEmail = ((UserPrinciple) principal).getEmail();

		Optional<User> loggedInUser = userRepository.findByEmail(loggedInEmail);

		// Users can only view their own submissions
		// or teachers can view
		Optional<Role> role = loggedInUser.get().getRoles().stream().findFirst();
		boolean isSameStudent = loggedInID.equals(studentID);
		boolean isTeacher = role.get().getName() == RoleName.ROLE_TEACHER;
		if (!isSameStudent && !isTeacher) {
			Response response = new Response(HttpStatus.METHOD_NOT_ALLOWED, ERRORS.get(NOT_ALLOWED_GET));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Set<Submission> submissions = loggedInUser.get().getSubmissions();
		return ResponseEntity.ok(submissions);
	}

	@PostMapping("/{assignment-id}")
	@PreAuthorize("hasRole('STUDENT')")  // add an assignment to a course with given id.
	public ResponseEntity<?> addSubmission(@Valid @RequestBody Submission submission, @PathVariable(value = "assignment-id") Long assignmentID) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String loggedInEmail = ((UserPrinciple) principal).getEmail();

		Optional<Assignment> assignment = assignmentRepository.findById(assignmentID);

		if (!assignment.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(ASSIGNMENT_NOT_FOUND), assignmentID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Optional<User> loggedInUser = userRepository.findByEmail(loggedInEmail);

		boolean alreadySubmitted = assignment
			.get()
			.getSubmissions()
			.stream()
			.anyMatch(s -> s.getUser().equals(loggedInUser.get()));

		if (alreadySubmitted) {
			Response response = new Response(HttpStatus.CONFLICT, ERRORS.get(ALREADY_SUBMITTED));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Submission newSubmission = new Submission(submission.getContent());
		newSubmission.setAssignment(assignment.get());
		newSubmission.setUser(loggedInUser.get());
		submissionRepository.save(newSubmission);

		return ResponseEntity.ok(newSubmission);
	}

}
