package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.Announcement;
import com.deu.cengonline.model.Assignment;
import com.deu.cengonline.model.Course;
import com.deu.cengonline.model.User;
import com.deu.cengonline.repository.CourseRepository;
import com.deu.cengonline.repository.RoleRepository;
import com.deu.cengonline.repository.UserRepository;
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
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/courses")
public class CourseController {

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

	@GetMapping()
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> getAllCourses() {
		List<Course> list = courseRepository.findAll();
		return ResponseEntity.ok(list);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> getCourseById(@PathVariable(value = "id") Long courseID) {
		Optional<Course> course = courseRepository.findById(courseID);

		if (!course.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND, "Course is not found!");
			return new ResponseEntity<>(response, response.getStatus());
		}

		return ResponseEntity.ok(course);
	}

	@PostMapping()
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<?> addCourse(@Valid @RequestBody Course course) {
		// Get email of logged in user
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String email = ((UserPrinciple) principal).getEmail();
		Optional<User> user = userRepository.findByEmail(email);

		if (!user.isPresent()) {
			Response response = new Response(HttpStatus.BAD_REQUEST,
				"The logged in account is not a teacher!");
			return new ResponseEntity<>(response, response.getStatus());
		}
		Course newCourse = new Course(course.getTitle(),course.getTerm());
		User teacher = user.get();
		newCourse.setTeacher(teacher);
		courseRepository.save(newCourse);
		return ResponseEntity.ok(newCourse);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<?> updateCourse(
		@PathVariable(value = "id") Long courseID, @Valid @RequestBody Course courseDetails) {
		Optional<Course> course = courseRepository.findById(courseID);

		if (!course.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format("The course with id(%d) does not exist!", courseID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Course newCourse = course.get();
		newCourse.setTitle(courseDetails.getTitle());
		newCourse.setTerm(courseDetails.getTerm());
		newCourse.setUpdatedAt(new Date());
		courseRepository.save(newCourse);
		return ResponseEntity.ok(newCourse);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteCourse(@PathVariable(value = "id") Long courseID) {
		Optional<Course> course = courseRepository.findById(courseID);

		if (!course.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format("The course with id(%d) does not exist!", courseID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Course courseToDelete = course.get();
		courseRepository.delete(courseToDelete);

		Response response = new Response(HttpStatus.OK,
			String.format("Course with id(%d) deleted successfully!", courseID));
		return new ResponseEntity<>(response, response.getStatus());
	}

	/*
	@GetMapping("/{id}/announcements") // get all announcements of the the course with given id.
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> announcements(@PathVariable(value = "id") Long courseID) {
		Optional<Course> course = courseRepository.findById(courseID);

		if (!course.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND, "Course is not found!");
			return new ResponseEntity<>(response, response.getStatus());
		}
		Set<Announcement> announcementSet = course.get().getAnnouncements();
		return ResponseEntity.ok(announcementSet);
	}

	@GetMapping("/{id}/assignments")  // get all assignments of the the course with given id.
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> assign(@PathVariable(value = "id") Long courseID) {
		Optional<Course> course = courseRepository.findById(courseID);

		if (!course.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND, "Course is not found!");
			return new ResponseEntity<>(response, response.getStatus());
		}

		Set<Assignment> assignmentSet = course.get().getAssignments();
		return ResponseEntity.ok(assignmentSet);
	} */
}