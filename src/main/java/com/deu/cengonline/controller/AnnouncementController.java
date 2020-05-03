package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.Announcement;
import com.deu.cengonline.model.Assignment;
import com.deu.cengonline.model.Course;
import com.deu.cengonline.model.User;
import com.deu.cengonline.repository.AnnouncementRepository;
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


    @GetMapping()
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ResponseEntity<?> getAllAnnouncements() {
        List<Announcement> list = announcementRepository.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/course/{course-id}")  // get all announcements of a course with given id.
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ResponseEntity<?> getAllAnnouncementsByCourseID(@PathVariable(value = "course-id") Long courseID) {
        Optional<Course> course = courseRepository.findById(courseID);

        if (!course.isPresent()) {
            Response response = new Response(HttpStatus.NOT_FOUND, "Course is not found!");
            return new ResponseEntity<>(response, response.getStatus());
        }
        Set<Announcement> list = course.get().getAnnouncements();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')") // get an announcement with id
    public ResponseEntity<?> getAnnouncementById(@PathVariable(value = "id") Long announcementId) {
        Optional<Announcement> announcement = announcementRepository.findById(announcementId);

        if (!announcement.isPresent()) {
            Response response = new Response(HttpStatus.NOT_FOUND, "Announcement is not found!");
            return new ResponseEntity<>(response, response.getStatus());
        }

        return ResponseEntity.ok(announcement);
    }

    @PostMapping("/{course-id}")
    @PreAuthorize("hasRole('TEACHER')")  // add an announcement to a course with given id.
    public ResponseEntity<?> addAnnouncement(@Valid @RequestBody Announcement announcement,@PathVariable(value = "course-id") Long courseID) {
        // Get email of logged in user
       /* Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = ((UserPrinciple) principal).getEmail();
        Optional<User> user = userRepository.findByEmail(email); */
        Optional<Course> course = courseRepository.findById(courseID);

        if (!course.isPresent()) {
            Response response = new Response(HttpStatus.NOT_FOUND, "Course is not found to make an announcement!");
            return new ResponseEntity<>(response, response.getStatus());
        }
        Announcement newAnnouncement = new Announcement(announcement.getDescription());
        Course courseEntity = course.get();
        /*courseEntity.getAnnouncements().add(newAnnouncement);
        courseRepository.save(courseEntity);*/
        newAnnouncement.setCourse(courseEntity);
        announcementRepository.save(newAnnouncement);
        return ResponseEntity.ok(newAnnouncement);
    }

    @PutMapping("/{id}")
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
    }
}
