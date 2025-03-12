package com.app.music.customers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
	private String id;
	private String firstName;
	private String lastName;
	private String email;
	private String userName;
	private String password;
}
