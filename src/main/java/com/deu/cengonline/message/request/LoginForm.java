package com.deu.cengonline.message.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class LoginForm {
	@NotBlank
	@Size(min = 3, max = 100)
	private String email;

	@NotBlank
	@Size(min = 6, max = 100)
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}