package com.app.music.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.music.customers.Customer;
import com.app.music.service.CustomerService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/music/public")
@Slf4j
public class CustomerController {
	private CustomerService customerService;
	
	public CustomerController(CustomerService customerService) {
		// TODO Auto-generated constructor stub

		this.customerService = customerService;
	}
	
	@PostMapping
	public String createCustomer(@RequestBody Customer customer) {
		log.info("Request Recieved To Controller :{}",customer);
		
		String response = customerService.createCustomer(customer);
		log.info("Returing Response From Controller.......:{}",response);
		
		return "Request Recieved :"+response;
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> loginCustomer(@RequestBody Customer customer) {
		 log.info("Request Received to Controller: {}", customer);

		    // Validate input
		    if (customer.getUserName() == null || customer.getPassword() == null) {
		        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Username and password are required"));
		    }

		    try {
	            String accessToken = customerService.loginCustomer(customer);
	            return ResponseEntity.ok(Map.of(
	                "message", "User authenticated successfully",
	                "access_token", accessToken
	            ));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
	        }
	}
}