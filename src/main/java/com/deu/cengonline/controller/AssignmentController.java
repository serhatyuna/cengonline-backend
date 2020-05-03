package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.Announcement;
import com.deu.cengonline.model.Assignment;
import com.deu.cengonline.model.Course;
import com.deu.cengonline.model.User;
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
import java.util.*;


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
    AnnouncementRepository announcementRepository;

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

    @GetMapping("/course/{course-id}")  // get all assignment of a course with given id.
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ResponseEntity<?> getAllAnnouncementsByCourseID(@PathVariable(value = "course-id") Long courseID) {
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
    public ResponseEntity<?> getAnnouncementById(@PathVariable(value = "id") Long assignmentId) {
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);

        if (!assignment.isPresent()) {
            Response response = new Response(HttpStatus.NOT_FOUND, "Assignment is not found!");
            return new ResponseEntity<>(response, response.getStatus());
        }
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/{course-id}")
    @PreAuthorize("hasRole('TEACHER')")  // add an assignment to a course with given id.
    public ResponseEntity<?> addAnnouncement(@Valid @RequestBody Assignment assignment,@PathVariable(value = "course-id") Long courseID) {
        // Get email of logged in user
       /* Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = ((UserPrinciple) principal).getEmail();
        Optional<User> user = userRepository.findByEmail(email); */
        Optional<Course> course = courseRepository.findById(courseID);

        if (!course.isPresent()) {
            Response response = new Response(HttpStatus.NOT_FOUND, "Course is not found to make an announcement!");
            return new ResponseEntity<>(response, response.getStatus());
        }
        Assignment newAssignment = new Assignment(assignment.getTitle(),assignment.getDescription(),assignment.getDueDate());
        Course courseEntity = course.get();
        /*courseEntity.getAnnouncements().add(newAnnouncement);
        courseRepository.save(courseEntity);*/
        newAssignment.setCourse(courseEntity);
        assignmentRepository.save(newAssignment);
        return ResponseEntity.ok(newAssignment);
    }

   /* @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateAnnouncement(
            @PathVariable(value = "id") Long announcementID, @Valid @RequestBody Announcement announcementDetail) {
        Optional<Announcement> announcement = announcementRepository.findById(announcementID);

        if (!announcement.isPresent()) {
            Response response = new Response(HttpStatus.NOT_FOUND,
                    String.format("The announcement with id(%d) does not exist!", announcementID));
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
        Optional<Announcement> announcement = announcementRepository.findById(announcementID);

        if (!announcement.isPresent()) {
            Response response = new Response(HttpStatus.NOT_FOUND,
                    String.format("The announcement with id(%d) does not exist!", announcementID));
            return new ResponseEntity<>(response, response.getStatus());
        }

        Announcement announcementToDelete = announcement.get();
        announcementRepository.delete(announcementToDelete);

        Response response = new Response(HttpStatus.OK,
                String.format("The announcement with id(%d) deleted successfully!", announcementID));
        return new ResponseEntity<>(response, response.getStatus());
    } */
}
