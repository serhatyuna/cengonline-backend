package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.*;

import com.deu.cengonline.repository.CourseRepository;
import com.deu.cengonline.repository.PostRepository;
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
import static com.deu.cengonline.util.ErrorName.*;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    CourseRepository courseRepository;
    @GetMapping("/course/{course-id}")  // get all posts of a course with given id.
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ResponseEntity<?> getAllPostsByCourseID(@PathVariable(value = "course-id") Long courseID) {
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

        Set<Post> list = course.get().getPosts();
        List<Post> sortedList = new ArrayList<>(list);
        sortedList.sort(comparing(AuditModel::getCreatedAt, reverseOrder()));
        return ResponseEntity.ok(sortedList);
    }

    @GetMapping("/{id}/course/{course-id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')") // get a post with id
    public ResponseEntity<?> getPostById(@PathVariable(value = "id") Long postId, @PathVariable(value = "course-id") Long courseID) {
        Long userID = AuthController.getCurrentUserId();
        Optional<User> user = userRepository.findById(userID);
        User current = user.get();
        Role role = AuthController.getCurrentUserRole(current.getRoles());
        Optional<Course> course = courseRepository.findById(courseID);
        Optional<Post> post = postRepository.findById(postId);

        if (!course.isPresent()) {
            Response response = new Response(HttpStatus.NOT_FOUND,
                    String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
            return new ResponseEntity<>(response, response.getStatus());
        }
        if (!post.isPresent()) {
            Response response = new Response(HttpStatus.NOT_FOUND,
                    String.format(ERRORS.get(POST_NOT_FOUND), postId));
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
        if (post.get().getCourse().getId() != courseID) {
            Response response = new Response(HttpStatus.NOT_FOUND,
                    String.format(ERRORS.get(POST_NOT_FOUND), postId));
            return new ResponseEntity<>(response, response.getStatus());
        }
        return ResponseEntity.ok(post.get());
    }


    @PostMapping("/{course-id}")
    @PreAuthorize("hasRole('TEACHER')")  // add a post to a course with given id.
    public ResponseEntity<?> addPost(@Valid @RequestBody Post post, @PathVariable(value = "course-id") Long courseID) {
        Long userID = AuthController.getCurrentUserId();

        Optional<Course> course = courseRepository.findById(courseID);

        if (!course.isPresent() || course.get().getTeacher().getId() != userID) {
            Response response = new Response(HttpStatus.NOT_FOUND,
                    String.format(ERRORS.get(COURSE_NOT_FOUND), courseID));
            return new ResponseEntity<>(response, response.getStatus());
        }

        Post newPost = new Post(post.getBody());
        Course courseEntity = course.get();
        newPost.setCourse(courseEntity);
        postRepository.save(newPost);
        return ResponseEntity.ok(newPost);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updatePost(
            @PathVariable(value = "id") Long postId, @Valid @RequestBody Post postInput) {
        Long userID = AuthController.getCurrentUserId();
        Optional<Post> post = postRepository.findById(postId);

        if (!post.isPresent() || post.get().getCourse().getTeacher().getId() != userID) {
            Response response = new Response(HttpStatus.NOT_FOUND,
                    String.format(ERRORS.get(POST_NOT_FOUND), postId));
            return new ResponseEntity<>(response, response.getStatus());
        }

        Post newPost = post.get();
        newPost.setBody(postInput.getBody());
        postRepository.save(newPost);
        return ResponseEntity.ok(newPost);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deletePost(@PathVariable(value = "id") Long postId) {
        Long userID = AuthController.getCurrentUserId();
        Optional<Post> post = postRepository.findById(postId);

        if (!post.isPresent() || post.get().getCourse().getTeacher().getId() != userID) {
            Response response = new Response(HttpStatus.NOT_FOUND,
                    String.format(ERRORS.get(POST_NOT_FOUND), postId));
            return new ResponseEntity<>(response, response.getStatus());
        }

        Post postToDelete = post.get();
        postRepository.delete(postToDelete);

        Response response = new Response(HttpStatus.OK,
                String.format("The post with id(%d) deleted successfully!", postId));
        return new ResponseEntity<>(response, response.getStatus());
    }

}
