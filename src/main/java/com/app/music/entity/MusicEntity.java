package com.app.music.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "music")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicEntity {
	@Id
	private String id;
	private String title;
	private String artist;
	private String genre;
	private String date;
	private String fileName; // Base64-encoded music file string


}
