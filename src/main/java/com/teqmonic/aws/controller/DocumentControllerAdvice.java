package com.teqmonic.aws.controller;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DocumentControllerAdvice {

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Void> handleNoSuchElementException() {
		return ResponseEntity.notFound().build();
	}
}
