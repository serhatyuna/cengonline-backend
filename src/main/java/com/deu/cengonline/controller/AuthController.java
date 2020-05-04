package com.deu.cengonline.controller;

import com.deu.cengonline.message.request.LoginForm;
import com.deu.cengonline.message.request.SignUpForm;
import com.deu.cengonline.message.response.JwtResponse;
import com.deu.cengonline.message.response.Response;
import com.deu.cengonline.model.Role;
import com.deu.cengonline.model.RoleName;
import com.deu.cengonline.model.User;
import com.deu.cengonline.repository.RoleRepository;
import com.deu.cengonline.repository.UserRepository;
import com.deu.cengonline.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

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

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {
		try {
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					loginRequest.getEmail(),
					loginRequest.getPassword()
				)
			);

			SecurityContextHolder.getContext().setAuthentication(authentication);

			String jwt = jwtProvider.generateJwtToken(authentication);
			return ResponseEntity.ok(new JwtResponse(jwt));
		} catch (BadCredentialsException e) {
			Response response = new Response(HttpStatus.BAD_REQUEST, "Username or password does not match!", e);
			return new ResponseEntity<>(response, response.getStatus());
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {
		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			Response response = new Response(HttpStatus.BAD_REQUEST, "Email is already in use!");
			return new ResponseEntity<>(response, response.getStatus());
		}

		// Creating user's account
		User user = new User(signUpRequest.getName(), signUpRequest.getSurname(),
			signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();
		if (!strRoles.contains("teacher") && !strRoles.contains("student")) {
			Response response = new Response(HttpStatus.BAD_REQUEST, "Not sufficient roles");
			return new ResponseEntity<>(response, response.getStatus());
		}
		strRoles.forEach(role -> {
			switch (role) {
				case "teacher":
					Role pmRole = roleRepository.findByName(RoleName.ROLE_TEACHER)
						.orElseThrow(() -> new RuntimeException("User Role not found."));
					roles.add(pmRole);
					break;
				case "student":
					Role adminRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
						.orElseThrow(() -> new RuntimeException("User Role not found."));
					roles.add(adminRole);
					break;
			}
		});

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok().body("User registered successfully!");
	}
}