package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.deu.cengonline.util.ErrorMessage.ERRORS;
import static com.deu.cengonline.util.ErrorName.ASSIGNMENT_NOT_FOUND;
import static com.deu.cengonline.util.ErrorName.COURSE_NOT_FOUND;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

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

	@GetMapping("/course/{course-id}")
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> getAllAssignments(@PathVariable(value = "course-id") Long courseID) {
		Long userID = AuthController.getCurrentUserId();
		Optional<User> user = userRepository.findById(userID);
		User current = user.get();
		Role role = AuthController.getCurrentUserRole(current.getRoles());
		Optional<Course> course = courseRepository.findById(courseID);
		if (!course.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
			return new ResponseEntity<>(response, response.getStatus());
		}
		if (role.getName().equals(RoleName.ROLE_TEACHER)) {
			if (course.get().getTeacher().getId() != userID) {
				Response response = new Response(HttpStatus.NOT_FOUND,
					String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
				return new ResponseEntity<>(response, response.getStatus());
			}
		} else {
			AtomicReference<Object> enrollment = new AtomicReference<>(null);
			current.getEnrollments().forEach(e -> {
				if (e.getId() == courseID) {
					enrollment.set(e);
				}
			});
			if (enrollment.get() == null) {
				Response response = new Response(HttpStatus.NOT_FOUND,
					String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
				return new ResponseEntity<>(response, response.getStatus());
			}
		}

		Set<Assignment> list = course.get().getAssignments();
		List<Assignment> sortedList = new ArrayList<>(list);
		sortedList.sort(comparing(AuditModel::getCreatedAt, reverseOrder()));
		return ResponseEntity.ok(sortedList);
	}

	@GetMapping("/{id}/course/{course-id}")
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')") // get an assignment with id
	public ResponseEntity<?> getAssignmentById(@PathVariable(value = "id") Long assignmentId, @PathVariable(value = "course-id") Long courseID) {
		Long userID = AuthController.getCurrentUserId();
		Optional<User> user = userRepository.findById(userID);
		User current = user.get();
		Role role = AuthController.getCurrentUserRole(current.getRoles());
		Optional<Course> course = courseRepository.findById(courseID);
		Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);

		if (!course.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
			return new ResponseEntity<>(response, response.getStatus());
		}
		if (!assignment.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(ASSIGNMENT_NOT_FOUND), assignmentId));
			return new ResponseEntity<>(response, response.getStatus());
		}
		if (role.getName().equals(RoleName.ROLE_TEACHER)) {
			if (course.get().getTeacher().getId() != userID) {
				Response response = new Response(HttpStatus.NOT_FOUND,
					String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
				return new ResponseEntity<>(response, response.getStatus());
			}
		} else {
			AtomicReference<Object> enrollment = new AtomicReference<>(null);
			current.getEnrollments().forEach(e -> {
				if (e.getId() == courseID) {
					enrollment.set(e);
				}
			});
			if (enrollment.get() == null) {
				Response response = new Response(HttpStatus.NOT_FOUND,
					String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
				return new ResponseEntity<>(response, response.getStatus());
			}
		}
		if (assignment.get().getCourse().getId() != courseID) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(ASSIGNMENT_NOT_FOUND), assignmentId));
			return new ResponseEntity<>(response, response.getStatus());
		}
		return ResponseEntity.ok(assignment.get());
	}

	@PostMapping("/{course-id}")
	@PreAuthorize("hasRole('TEACHER')")  // add an assignment to a course with given id.
	public ResponseEntity<?> addAssignment(@Valid @RequestBody Assignment assignment, @PathVariable(value = "course-id") Long courseID) {
		Long userID = AuthController.getCurrentUserId();
		Optional<Course> course = courseRepository.findById(courseID);

		if (!course.isPresent() || course.get().getTeacher().getId() != userID) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
			return new ResponseEntity<>(response, response.getStatus());
		}

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
		Long userID = AuthController.getCurrentUserId();
		Optional<Assignment> assignment = assignmentRepository.findById(assignmentID);

		if (!assignment.isPresent() || assignment.get().getCourse().getTeacher().getId() != userID) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(ASSIGNMENT_NOT_FOUND), assignmentID));
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
		Long userID = AuthController.getCurrentUserId();
		Optional<Assignment> assignment = assignmentRepository.findById(assignmentID);

		if (!assignment.isPresent() || assignment.get().getCourse().getTeacher().getId() != userID) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(ASSIGNMENT_NOT_FOUND), assignmentID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Assignment assignmentToDelete = assignment.get();
		assignmentRepository.delete(assignmentToDelete);

		Response response = new Response(HttpStatus.OK,
			String.format("The assignment with id(%d) deleted successfully!", assignmentID));
		return new ResponseEntity<>(response, response.getStatus());
	}
}
