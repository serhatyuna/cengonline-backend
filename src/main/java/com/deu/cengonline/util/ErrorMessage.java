package com.deu.cengonline.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ErrorMessage {
	public static final Map<ErrorName, String> ERRORS;

	private static final String COURSE_NOT_FOUND_MESSAGE = "The course with id(%d) does not exist!";
	private static final String USER_NOT_FOUND_MESSAGE = "The user with id(%d) does not exist!";
	private static final String ANNOUNCEMENT_NOT_FOUND_MESSAGE = "The announcement with id(%d) does not exist!";
	private static final String ASSIGNMENT_NOT_FOUND_MESSAGE = "The assignment with id(%d) does not exist!";
	private static final String ROLE_NOT_FOUND_MESSAGE = "Not sufficient roles!";
	private static final String SUBMISSION_NOT_FOUND_MESSAGE = "The submission with id(%d) does not exist!";
	private static final String FAILED_LOGIN_MESSAGE = "Username or password does not match!";
	private static final String EMAIL_IN_USE_MESSAGE = "Email is already in use!";
	private static final String MESSAGE_TO_YOURSELF_MESSAGE = "You cannot send a message to yourself!";
	private static final String NO_USER_YET_MESSAGE = "There is no user yet!";
	private static final String ALREADY_ATTENDED_MESSAGE = "Already attended the course!";
	private static final String NOT_ALLOWED_GET_MESSAGE = "You are not allowed to do this action!";
	private static final String ALREADY_SUBMITTED_MESSAGE = "You are not allowed to do this action!";

	static {
		Map<ErrorName, String> tempMap = new HashMap<>();
		tempMap.put(ErrorName.COURSE_NOT_FOUND, COURSE_NOT_FOUND_MESSAGE);
		tempMap.put(ErrorName.USER_NOT_FOUND, USER_NOT_FOUND_MESSAGE);
		tempMap.put(ErrorName.ANNOUNCEMENT_NOT_FOUND, ANNOUNCEMENT_NOT_FOUND_MESSAGE);
		tempMap.put(ErrorName.ASSIGNMENT_NOT_FOUND, ASSIGNMENT_NOT_FOUND_MESSAGE);
		tempMap.put(ErrorName.ROLE_NOT_FOUND, ROLE_NOT_FOUND_MESSAGE);
		tempMap.put(ErrorName.SUBMISSION_NOT_FOUND, SUBMISSION_NOT_FOUND_MESSAGE);
		tempMap.put(ErrorName.FAILED_LOGIN, FAILED_LOGIN_MESSAGE);
		tempMap.put(ErrorName.EMAIL_IN_USE, EMAIL_IN_USE_MESSAGE);
		tempMap.put(ErrorName.MESSAGE_TO_YOURSELF, MESSAGE_TO_YOURSELF_MESSAGE);
		tempMap.put(ErrorName.NO_USER_YET, NO_USER_YET_MESSAGE);
		tempMap.put(ErrorName.ALREADY_ATTENDED, ALREADY_ATTENDED_MESSAGE);
		tempMap.put(ErrorName.NOT_ALLOWED_GET, NOT_ALLOWED_GET_MESSAGE);
		tempMap.put(ErrorName.ALREADY_SUBMITTED, ALREADY_SUBMITTED_MESSAGE);

		ERRORS = Collections.unmodifiableMap(tempMap);
	}
}