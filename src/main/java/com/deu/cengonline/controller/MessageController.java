package com.deu.cengonline.controller;

import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.Message;
import com.deu.cengonline.model.User;
import com.deu.cengonline.repository.MessageRepository;
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
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messages")
public class MessageController {
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
	MessageRepository messageRepository;

	@GetMapping("/{receiverID}")  // get all messages between two people.
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
	public ResponseEntity<?> getAllMessagesBetweenTwo(@PathVariable(value = "receiverID") Long receiverID) {
		// Get logged in user
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String email = ((UserPrinciple) principal).getEmail();

		Optional<User> loggedInUser = userRepository.findByEmail(email);
		Optional<User> receiverUser = userRepository.findById(receiverID);

		if (!receiverUser.isPresent()) {
			Response response = new Response(HttpStatus.BAD_REQUEST,
				String.format("The user with id(%d) does not exist!", receiverID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		Long loggedInUserID = loggedInUser.get().getId();

		List<Message> messages =
			messageRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByCreatedAt(
				loggedInUserID, receiverID, loggedInUserID, receiverID);

		return ResponseEntity.ok(messages);
	}

	// TODO: Kendisine mesaj atamasın.
	@PostMapping("/{receiverID}")
	@PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")  // send a message to given user id.
	public ResponseEntity<?> sendMessage(@Valid @RequestBody Message message, @PathVariable(value = "receiverID") Long receiverID) {
		// Get logged in user
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String email = ((UserPrinciple) principal).getEmail();

		Optional<User> loggedInUserOpt = userRepository.findByEmail(email);
		Optional<User> receiverUserOpt = userRepository.findById(receiverID);

		if (!receiverUserOpt.isPresent()) {
			Response response = new Response(HttpStatus.BAD_REQUEST,
				String.format("The user with id(%d) does not exist!", receiverID));
			return new ResponseEntity<>(response, response.getStatus());
		}

		User loggedInUser = loggedInUserOpt.get();
		User receiverUser = receiverUserOpt.get();

		String newContent = message.getContent();
		Message newMessage = new Message(newContent);
		newMessage.setSender(loggedInUser);
		newMessage.setReceiver(receiverUser);
		messageRepository.save(newMessage);

		return ResponseEntity.ok(newMessage);
	}

}
