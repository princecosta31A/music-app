package com.app.music.pojo;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicReq {
	private String id;
	private String title;
	private String artist;
	private String genre;
	private String date;
	private MultipartFile musicFile;
	

}
