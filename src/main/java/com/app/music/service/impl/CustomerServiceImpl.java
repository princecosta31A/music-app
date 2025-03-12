package com.app.music.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.naming.ServiceUnavailableException;

import org.apache.http.auth.AuthenticationException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.app.music.customers.Customer;
import com.app.music.entity.CustomerEntity;
import com.app.music.repo.CustomerRepo;
import com.app.music.service.CustomerService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {


	@Value("${keycloak.server-url}")
	private String keycloakServerUrl;

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.client-id}")
	private String clientId;

	@Value("${keycloak.client-secret}")
	private String clientSecret;

	private CustomerRepo customerRepo;
	private RestTemplate restTemplate;
	private ModelMapper modelMapper;

	public CustomerServiceImpl(CustomerRepo customerRepo,
			RestTemplate restTemplate,
			ModelMapper modelMapper) {
		// TODO Auto-generated constructor stub
		this.customerRepo = customerRepo;
		this.restTemplate  = restTemplate;
		this.modelMapper = modelMapper;
	}


	/**
	 * Create a customer by first registering in Keycloak and then storing it in MongoDB.
	 */
	public String createCustomer(Customer customer) {
		log.info("Processing customer registration...");

		// Convert Customer to CustomerEntity
		CustomerEntity customerEntity = modelMapper.map(customer, CustomerEntity.class);
		log.info("Converted to CustomerEntity :{}",customerEntity);

		// Step 1: Register in Keycloak
		boolean keycloakSuccess = registerUserInKeycloak(customer);
		if (!keycloakSuccess) {
			log.error("Failed to register user in Keycloak");
			return "Failed to register user in Keycloak";
		}

		// Step 2: Save in MongoDB
		try {
			customerRepo.save(customerEntity);
			log.info("Successfully saved customer in MongoDB.");
			return "Customer registered successfully!";
		} catch (DataAccessException e) {
			log.error("Error saving customer in MongoDB: {}", e.getMessage());
			return "Failed to save customer in MongoDB";
		}
	}

	/**
	 * Registers a user in Keycloak.
	 */
	private boolean registerUserInKeycloak(Customer customer) {
		String keycloakUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users";
		log.info("Reagistering User to KeyCloak........ :{}",keycloakUrl);

		// Prepare user data for Keycloak
		Map<String, Object> user = new HashMap<>();
		user.put("username", customer.getUserName());
		user.put("email", customer.getEmail());
		user.put("firstName", customer.getFirstName());
		user.put("lastName", customer.getLastName());
		user.put("enabled", true);

		// Add credentials (password)
		Map<String, Object> credentials = new HashMap<>();
		credentials.put("type", "password");
		credentials.put("value", customer.getPassword());
		credentials.put("temporary", false);
		user.put("credentials", new Map[]{credentials});

		// Prepare headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(getAdminAccessToken()); // Get access token

		// Log headers to verify token presence
		System.out.println("Authorization Header: " + headers.getFirst(HttpHeaders.AUTHORIZATION));

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(user, headers);

		System.out.println("Making Call to KeyCloak For Registering User.......");
		// Send request to Keycloak
		ResponseEntity<String> response = restTemplate.exchange(keycloakUrl, HttpMethod.POST, request, String.class);

		System.out.println("Respnse........."+response);
		return response.getStatusCode() == HttpStatus.CREATED;
	}

	/**
	 * Gets an admin access token from Keycloak.
	 */
	private String getAdminAccessToken() {
		String tokenUrl = keycloakServerUrl + "/realms/master/protocol/openid-connect/token";
		log.info("Reached To Getting AccessToken :{}",tokenUrl);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		String requestBody = "client_id=" + clientId +
				"&client_secret=" + clientSecret +
				"&grant_type=client_credentials";

		HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
		ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
		System.out.println("Response Access Token :"+response.getBody().get("access_token").toString());

		return response.getBody().get("access_token").toString();
	}


	@Override
	public String loginCustomer(Customer customer) {
		log.info("Processing Customer Verification: {}", customer.getUserName());

		// Validate input
		if (customer.getUserName() == null || customer.getPassword() == null) {
			throw new IllegalArgumentException("Username and password are required");
		}

		// Prepare request payload
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
		requestBody.add("client_id", clientId);
		requestBody.add("client_secret", clientSecret);
		requestBody.add("grant_type", "password");
		requestBody.add("username", customer.getUserName());
		requestBody.add("password", customer.getPassword());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

		try {
			ResponseEntity<Map> response = restTemplate.exchange(
					keycloakServerUrl + "/realms/master/protocol/openid-connect/token",
					HttpMethod.POST, request, Map.class);

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				log.info("Login successful for user: {}", customer.getUserName());
				return response.getBody().get("access_token").toString();
			}
			else {
				return "SomeThing Went Wrong";
			}
		} catch (HttpClientErrorException.Unauthorized e) {
			log.error("Invalid username or password: {}", e.getResponseBodyAsString());
			throw new RuntimeException("Invalid username or password");
		} catch (HttpClientErrorException.BadRequest e) {
			log.error("Bad request error: {}", e.getResponseBodyAsString());
			throw new RuntimeException("Invalid request format");
		} catch (HttpServerErrorException e) {
			log.error("Keycloak Service is down: {}", e.getResponseBodyAsString());
			throw new RuntimeException("Authentication service is currently unavailable. Try again later");
		} catch (Exception e) {
			log.error("Unexpected error during login: {}", e.getMessage());
			throw new RuntimeException("An unexpected error occurred");
		}
		
}
}
