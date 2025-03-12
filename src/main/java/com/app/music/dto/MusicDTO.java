package com.app.music.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicDTO {
	private String id;
	private String title;
	private String artist;
	private String genre;
	private String date;
	private String fileUrl;
	private MultipartFile musicFile;
	
}
	