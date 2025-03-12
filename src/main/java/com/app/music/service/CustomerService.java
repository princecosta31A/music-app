package com.app.music.service;

import com.app.music.customers.Customer;

public interface CustomerService {
	
	String createCustomer(Customer customer);
	String loginCustomer(Customer customer);

}
