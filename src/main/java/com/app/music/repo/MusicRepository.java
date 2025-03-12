package com.app.music.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.app.music.entity.MusicEntity;

@Repository
public interface MusicRepository extends MongoRepository<MusicEntity, String> {

}
