package com.app.music.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.app.music.dto.MusicDTO;
import com.app.music.pojo.MusicRes;

public interface MusicService {

	MusicDTO saveMusic(MusicDTO musicDTO); // create
	List<MusicRes> restoreMusic(); // read
	String fileUpload(MultipartFile file); // testing
	boolean removeMusic(String id); // delete
	MusicRes restoreMusicById(String id); // read
	MusicDTO modifyMusicById(MusicDTO musicDTO); // update

}
