package com.app.music.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.app.music.entity.CustomerEntity;

@Repository
public interface CustomerRepo extends MongoRepository<CustomerEntity, String>  {

}
