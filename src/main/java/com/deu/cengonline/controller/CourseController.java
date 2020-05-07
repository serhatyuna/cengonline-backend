package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.Course;
import com.deu.cengonline.model.Role;
import com.deu.cengonline.model.RoleName;
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
import java.util.concurrent.atomic.AtomicReference;

import static com.deu.cengonline.util.ErrorMessage.ERRORS;
import static com.deu.cengonline.util.ErrorName.COURSE_NOT_FOUND;
import static com.deu.cengonline.util.ErrorName.USER_NOT_FOUND;

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
		Long userID = AuthController.getCurrentUserId();
		Optional<User> user = userRepository.findById(userID);
		User current = user.get();
		Set<Role> roles = current.getRoles();
		Role role = AuthController.getCurrentUserRole(roles);
		if(role.getName().equals(RoleName.ROLE_TEACHER)){
			Set<Course> list = current.getCourses();
			return ResponseEntity.ok(list);
		}
		else {
			return ResponseEntity.ok((current.getEnrollments()));
		}

	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> getCourseById(@PathVariable(value = "id") Long courseID) {
		Long userID = AuthController.getCurrentUserId();
		Optional<User> user = userRepository.findById(userID);
		User current = user.get();
		Role role = AuthController.getCurrentUserRole(current.getRoles());
		if(role.getName().equals(RoleName.ROLE_TEACHER)) {
			Optional<Course> course = courseRepository.findById(courseID);
			if(!course.isPresent() || course.get().getTeacher().getId() != current.getId()) {
				Response response = new Response(HttpStatus.NOT_FOUND,
						String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
				return new ResponseEntity<>(response, response.getStatus());
			}
			return ResponseEntity.ok(course.get());
		}
		else {
			AtomicReference<Object> enrollment = new AtomicReference<>(null);
			current.getEnrollments().forEach(e -> {
				if(e.getId() == courseID) {
					enrollment.set(e);
				}
			});
			if (enrollment.get() == null) {
				Response response = new Response(HttpStatus.NOT_FOUND,
						String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
				return new ResponseEntity<>(response, response.getStatus());
			}
			return ResponseEntity.ok(enrollment.get());
		}

	}

	@PostMapping()
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<?> addCourse(@Valid @RequestBody Course course) {
		// Get email of logged in user
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String email = ((UserPrinciple) principal).getEmail();
		Long userID = ((UserPrinciple) principal).getId();
		Optional<User> user = userRepository.findByEmail(email);

		if (!user.isPresent()) {
			Response response = new Response(HttpStatus.BAD_REQUEST,
				String.format(ERRORS.get(USER_NOT_FOUND), userID));
			return new ResponseEntity<>(response, response.getStatus());
		}
		Course newCourse = new Course(course.getTitle(), course.getTerm());
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
				String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
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
				String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Course courseToDelete = course.get();
		courseRepository.delete(courseToDelete);

		Response response = new Response(HttpStatus.OK,
			String.format("Course with id(%d) deleted successfully!", courseID));
		return new ResponseEntity<>(response, response.getStatus());
	}

}