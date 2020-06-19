package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.Course;
import com.deu.cengonline.model.Role;
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

import java.util.*;

import static com.deu.cengonline.util.ErrorMessage.ERRORS;
import static com.deu.cengonline.util.ErrorName.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
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



	@GetMapping("/")
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> getAllUsers() {
		List<User> users = userRepository.findAll();

		if (users.isEmpty()) {
			Response response = new Response(HttpStatus.NOT_FOUND, ERRORS.get(NO_USER_YET));
			return new ResponseEntity<>(response, response.getStatus());
		}
		return ResponseEntity.ok(users);
	}

	@GetMapping("/current")
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> getCurrentUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Long id = ((UserPrinciple) principal).getId();
		Optional<User> current = userRepository.findById(id);
		if (!current.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND, ERRORS.get(NO_USER_YET));
			return new ResponseEntity<>(response, response.getStatus());
		}
		else
			return ResponseEntity.ok(current.get());
	}

	@PostMapping("/attend-class/{id}")
	@PreAuthorize("hasRole('STUDENT')")
	public ResponseEntity<?> attendIntoClass(@PathVariable(value = "id") Long courseID) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Long id = ((UserPrinciple) principal).getId();
		Optional<Course> course = courseRepository.findById(courseID);

		if (!course.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Optional<User> user = userRepository.findById(id);
		User student = user.get();
		
		boolean alreadyAttended = student
			.getCourses()
			.stream()
			.anyMatch(crs -> crs.getId().equals(courseID)); 

		if (alreadyAttended) {
			Response response = new Response(HttpStatus.BAD_REQUEST, ERRORS.get(ALREADY_ATTENDED));
			return new ResponseEntity<>(response, response.getStatus());
		}
		student.getEnrollments().add(course.get());
		course.get().getUsers().add(student);
		courseRepository.save(course.get());
		userRepository.save(student);
		return ResponseEntity.ok("Student has been entered the class successfully.");
	}

}
