package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.Assignment;
import com.deu.cengonline.model.Course;
import com.deu.cengonline.repository.AssignmentRepository;
import com.deu.cengonline.repository.CourseRepository;
import com.deu.cengonline.repository.RoleRepository;
import com.deu.cengonline.repository.UserRepository;
import com.deu.cengonline.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
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

	@GetMapping()
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> getAllAssignments() {
		List<Assignment> list = assignmentRepository.findAll();
		return ResponseEntity.ok(list);
	}

	@GetMapping("/course/{course-id}")  // get all assignments of a course with given id.
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> getAllAssignmentsByCourseID(@PathVariable(value = "course-id") Long courseID) {
		Optional<Course> course = courseRepository.findById(courseID);

		if (!course.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND, "Course is not found!");
			return new ResponseEntity<>(response, response.getStatus());
		}
		Set<Assignment> list = course.get().getAssignments();
		return ResponseEntity.ok(list);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')") // get an assignment with id
	public ResponseEntity<?> getAssignmentById(@PathVariable(value = "id") Long assignmentId) {
		Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);

		if (!assignment.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND, "Assignment is not found!");
			return new ResponseEntity<>(response, response.getStatus());
		}
		return ResponseEntity.ok(assignment);
	}

	@PostMapping("/course/{course-id}")
	@PreAuthorize("hasRole('TEACHER')")  // add an assignment to a course with given id.
	public ResponseEntity<?> addAssignment(@Valid @RequestBody Assignment assignment, @PathVariable(value = "course-id") Long courseID) {
		Optional<Course> course = courseRepository.findById(courseID);

		if (!course.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND, "Course is not found to make an assignment!");
			return new ResponseEntity<>(response, response.getStatus());
		}

		System.out.println(assignment.getDueDate());

		Assignment newAssignment = new Assignment(assignment.getTitle(), assignment.getDescription(), assignment.getDueDate());
		Course courseEntity = course.get();
		newAssignment.setCourse(courseEntity);
		assignmentRepository.save(newAssignment);
		return ResponseEntity.ok(newAssignment);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<?> updateAssignment(
		@PathVariable(value = "id") Long assignmentID, @Valid @RequestBody Assignment assignmentDetail) {
		Optional<Assignment> assignment = assignmentRepository.findById(assignmentID);

		if (!assignment.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format("The assignment with id(%d) does not exist!", assignmentID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Assignment newAssignment = assignment.get();
		newAssignment.setDescription(assignmentDetail.getDescription());
		newAssignment.setTitle(assignmentDetail.getTitle());
		newAssignment.setDueDate(assignmentDetail.getDueDate());
		assignmentRepository.save(newAssignment);
		return ResponseEntity.ok(newAssignment);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<?> deleteAssignment(@PathVariable(value = "id") Long assignmentID) {
		Optional<Assignment> assignment = assignmentRepository.findById(assignmentID);

		if (!assignment.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format("The assignment with id(%d) does not exist!", assignmentID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Assignment assignmentToDelete = assignment.get();
		assignmentRepository.delete(assignmentToDelete);

		Response response = new Response(HttpStatus.OK,
			String.format("The assignment with id(%d) deleted successfully!", assignmentID));
		return new ResponseEntity<>(response, response.getStatus());
	}
}
