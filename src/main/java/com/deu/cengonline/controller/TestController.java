package com.deu.cengonline.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class TestController {
	@GetMapping("/api/test/student")
	@PreAuthorize("hasRole('STUDENT')")
	public String studentAccess() {
		return ">>> Student Contents!";
	}

	@GetMapping("/api/test/teacher")
	@PreAuthorize("hasRole('TEACHER')")
	public String teacherAccess() {
		return ">>> Teacher Contents";
	}
}