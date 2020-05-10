package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.*;
import com.deu.cengonline.repository.AnnouncementRepository;
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
import static com.deu.cengonline.util.ErrorName.ANNOUNCEMENT_NOT_FOUND;
import static com.deu.cengonline.util.ErrorName.COURSE_NOT_FOUND;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	AnnouncementRepository announcementRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtProvider jwtProvider;

	@Autowired
	CourseRepository courseRepository;

	@GetMapping("/course/{course-id}")  // get all announcements of a course with given id.
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> getAllAnnouncementsByCourseID(@PathVariable(value = "course-id") Long courseID) {
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

		Set<Announcement> list = course.get().getAnnouncements();
		List<Announcement> sortedList = new ArrayList<>(list);
		sortedList.sort(comparing(AuditModel::getCreatedAt, reverseOrder()));
		return ResponseEntity.ok(sortedList);
	}

	@GetMapping("/{id}/course/{course-id}")
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')") // get an announcement with id
	public ResponseEntity<?> getAnnouncementById(@PathVariable(value = "id") Long announcementId, @PathVariable(value = "course-id") Long courseID) {
		Long userID = AuthController.getCurrentUserId();
		Optional<User> user = userRepository.findById(userID);
		User current = user.get();
		Role role = AuthController.getCurrentUserRole(current.getRoles());
		Optional<Course> course = courseRepository.findById(courseID);
		Optional<Announcement> announcement = announcementRepository.findById(announcementId);

		if (!course.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
			return new ResponseEntity<>(response, response.getStatus());
		}
		if (!announcement.isPresent()) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(ANNOUNCEMENT_NOT_FOUND), announcementId));
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
		if (announcement.get().getCourse().getId() != courseID) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(ANNOUNCEMENT_NOT_FOUND), announcementId));
			return new ResponseEntity<>(response, response.getStatus());
		}
		return ResponseEntity.ok(announcement.get());
	}

	@PostMapping("/{course-id}")
	@PreAuthorize("hasRole('TEACHER')")  // add an announcement to a course with given id.
	public ResponseEntity<?> addAnnouncement(@Valid @RequestBody Announcement announcement, @PathVariable(value = "course-id") Long courseID) {
		Long userID = AuthController.getCurrentUserId();

		Optional<Course> course = courseRepository.findById(courseID);

		if (!course.isPresent() || course.get().getTeacher().getId() != userID) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Announcement newAnnouncement = new Announcement(announcement.getDescription());
		Course courseEntity = course.get();
		newAnnouncement.setCourse(courseEntity);
		announcementRepository.save(newAnnouncement);
		return ResponseEntity.ok(newAnnouncement);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<?> updateAnnouncement(
		@PathVariable(value = "id") Long announcementID, @Valid @RequestBody Announcement announcementDetail) {
		Long userID = AuthController.getCurrentUserId();
		Optional<Announcement> announcement = announcementRepository.findById(announcementID);

		if (!announcement.isPresent() || announcement.get().getCourse().getTeacher().getId() != userID) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(ANNOUNCEMENT_NOT_FOUND), announcementID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Announcement newAnnouncement = announcement.get();
		newAnnouncement.setDescription(announcementDetail.getDescription());
		announcementRepository.save(newAnnouncement);
		return ResponseEntity.ok(newAnnouncement);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('TEACHER')")
	public ResponseEntity<?> deleteAnnouncement(@PathVariable(value = "id") Long announcementID) {
		Long userID = AuthController.getCurrentUserId();
		Optional<Announcement> announcement = announcementRepository.findById(announcementID);

		if (!announcement.isPresent() || announcement.get().getCourse().getTeacher().getId() != userID) {
			Response response = new Response(HttpStatus.NOT_FOUND,
				String.format(ERRORS.get(ANNOUNCEMENT_NOT_FOUND), announcementID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Announcement announcementToDelete = announcement.get();
		announcementRepository.delete(announcementToDelete);

		Response response = new Response(HttpStatus.OK,
			String.format("The announcement with id(%d) deleted successfully!", announcementID));
		return new ResponseEntity<>(response, response.getStatus());
	}
}
