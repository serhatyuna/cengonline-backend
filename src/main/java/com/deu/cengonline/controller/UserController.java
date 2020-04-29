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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

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
        List<User> users= userRepository.findAll();
        if (users.isEmpty()) {
            Response response = new Response(HttpStatus.NOT_FOUND, "There is no user yet!");
            return new ResponseEntity<>(response, response.getStatus());
        }
        return ResponseEntity.ok(users);
    }

    @PostMapping("/attend-class/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> attendIntoClass(@PathVariable(value = "id") Long courseID) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = ((UserPrinciple) principal).getId();
        Optional<Course> course = courseRepository.findById(courseID);
        if (!course.isPresent()) {
            Response response = new Response(HttpStatus.NOT_FOUND, "Course is not found!");
            return new ResponseEntity<>(response, response.getStatus());
        }

        Optional<User> user =userRepository.findById(id);
        User student = user.get();
        final boolean alreadyAttended[] = {false};
        student.getCourses().forEach(crs -> {
            if(crs.getId() == courseID)
            {
                alreadyAttended[0] = true;
               return;
            }
        });
        if(!alreadyAttended[0]) {
            Response response = new Response(HttpStatus.BAD_REQUEST, "Already attended the course!");
            return new ResponseEntity<>(response, response.getStatus());
        }
        student.getEnrollments().add(course.get());
        userRepository.save(student);
        return ResponseEntity.ok("student has been entered the class successfully.");
    }

}
