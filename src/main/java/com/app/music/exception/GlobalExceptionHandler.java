package com.app.music.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	  @ExceptionHandler(MongoDBIdNotFoundException.class)
	    public ResponseEntity<String> handleMongoDBIdNotFoundException(MongoDBIdNotFoundException ex) {
	        return ResponseEntity
	                .status(HttpStatus.NOT_FOUND)
	                .body("Error: " + ex.getMessage());
	    }

	
	  @ExceptionHandler(MongoDBSaveException.class)
	    public ResponseEntity<String> handleMongoDBSaveException(MongoDBSaveException ex) {
	        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	 @ExceptionHandler(InvalidFileTypeException.class)
	    public ResponseEntity<String> handleInvalidFileTypeException(InvalidFileTypeException ex) {
	        return ResponseEntity
	                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	                .body(ex.getMessage());
	    }

	    @ExceptionHandler(Exception.class)
	    public ResponseEntity<String> handleGenericException(Exception ex) {
	        return ResponseEntity
	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("An error occurred: " + ex.getMessage());
	    }
}
