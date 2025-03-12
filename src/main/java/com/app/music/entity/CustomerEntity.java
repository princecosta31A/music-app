package com.app.music.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity {
	
	@Id
	private String id;
	private String firstName;
	private String lastName;
	private String email;
	private String userName;
	private String password;

}
