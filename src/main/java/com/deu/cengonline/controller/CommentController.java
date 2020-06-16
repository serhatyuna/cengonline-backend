package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.*;

import com.deu.cengonline.repository.*;
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
public class CommentController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PostRepository postRepository;


    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    CourseRepository courseRepository;


    @PostMapping("/{post-id}")
    public ResponseEntity<?> addComment(@Valid @RequestBody Comment comment, @PathVariable(value = "post-id") Long postId) {
        Long userID = AuthController.getCurrentUserId();

        Optional<Post> post = postRepository.findById(postId);
        boolean registeredToCourse = false;
        for (User user:post.get().getCourse().getUsers()) {
            if(user.getId() == userID) {
                registeredToCourse = true;
                break;
            }
        }

        if (!post.isPresent() || (post.get().getCourse().getTeacher().getId() != userID && !registeredToCourse)) {
            Response response = new Response(HttpStatus.NOT_FOUND,
                    String.format(ERRORS.get(POST_NOT_FOUND), post.get().getId()));
            return new ResponseEntity<>(response, response.getStatus());
        }

        Comment newComment = new Comment(comment.getBody());
        Post postEntity = post.get();
        newComment.setPost(postEntity);
        Optional<User> user = userRepository.findById(userID);

        commentRepository.save(newComment);
        return ResponseEntity.ok(newComment);
    }

}
