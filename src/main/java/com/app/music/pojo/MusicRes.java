package com.app.music.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicRes {
	private String id;
	private String title;
	private String artist;
	private String genre;
	private String date;
	private String fileUrl; // Base64-encoded music file string
	
	
	
}
