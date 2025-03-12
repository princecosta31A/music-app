package com.app.music.controller;

import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.music.dto.MusicDTO;
import com.app.music.exception.InvalidFileTypeException;
import com.app.music.pojo.MusicReq;
import com.app.music.pojo.MusicRes;
import com.app.music.service.MusicService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/music")
@Slf4j
public class MusicController {

	private ModelMapper modelMapper;
	private MusicService musicService;

	public MusicController(ModelMapper modelMapper,
			MusicService musicService) {
		this.modelMapper = modelMapper;
		this.musicService = musicService;
	}

	@PostMapping
	public ResponseEntity<MusicRes> createMusic( @ModelAttribute MusicReq musicReq) {

		log.info("Music Recieved to Controller {} :",musicReq);
		// Check if the musicReq object is null or empty
		if (musicReq == null || 
				musicReq.getTitle() == null || musicReq.getTitle().isEmpty() || 
				musicReq.getArtist() == null || musicReq.getArtist().isEmpty() || 
				musicReq.getGenre() == null || musicReq.getGenre().isEmpty() || 
				musicReq.getDate() == null || musicReq.getDate().isEmpty() || 
				musicReq.getMusicFile() == null || musicReq.getMusicFile().isEmpty()) {

			throw new RuntimeException("Music data is incomplete or invalid. Please check the required fields.");
		}


		// Validate the file type

		String mimeType = musicReq.getMusicFile().getContentType();
		if (!isValidMusicFile(mimeType)) {
			log.error("Invalid file type: {}", mimeType);
			throw new InvalidFileTypeException("Only music files are allowed. Provided file type: " + mimeType);
		}

//		// convert to DTO
		MusicDTO musicDTO = modelMapper.map(musicReq, MusicDTO.class);
		log.info("Converted To DTO {} :",musicDTO);

		// Proceed with your file processing logic (e.g., save to MinIO, database, etc.)
		log.info("File is valid. Proceeding with processing...");
		MusicDTO musicStatus = musicService.saveMusic(musicDTO);

		if(musicStatus==null) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		//			// convert  DTO to pojo MusicRes
		MusicRes musicRes = modelMapper.map(musicStatus, MusicRes.class);
		log.info("Response Atfter Converting to MusicReq {} :",musicRes);

		return new ResponseEntity<>(musicRes, HttpStatus.CREATED);

	}

	@GetMapping
	public ResponseEntity<List<MusicRes>> getMusic() {
		log.info("Processing to Get All Music ");

		List<MusicRes> musicRes = musicService.restoreMusic();

		log.info("Recieved All the Music's : {}",musicRes.size());

		if (musicRes.isEmpty()) {
			// Return 204 No Content if the list is empty
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		} else {
			// Return 200 OK with the music list
			return ResponseEntity.ok(musicRes);
		}
	}


	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteMusic(@PathVariable String id) {
		log.info("Processing Music Deletion :{}",id);

		if(id==null || id.isEmpty() || id.isBlank()) {
			throw new RuntimeException("Id Not Found ");
		}

		boolean flag = musicService.removeMusic(id);

		if(!flag) {
			return ResponseEntity
					.status(HttpStatus.NOT_MODIFIED)
					.body("Music with ID " + id + " could not be deleted.");
		}

		return ResponseEntity
				.status(HttpStatus.OK)
				.body("Music with ID " + id + " successfully deleted.");
	}


	@GetMapping("/{id}")
	public ResponseEntity<MusicRes> getMusicById(@PathVariable String id) {
		log.info("Processing Music Id {} To Fetch ",id);

		MusicRes musicRes = musicService.restoreMusicById(id);
		if (musicRes==null) {
			// Return 204 No Content if the list is empty
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		} else {
			// Return 200 OK with the music list
			return ResponseEntity.ok(musicRes);
		}
	}

	@PutMapping
	public ResponseEntity<MusicRes> updateMusicById(@ModelAttribute MusicReq musicReq) {
		log.info("Music Recieved to Controller {} :",musicReq);
		if (musicReq == null || 
				musicReq.getId() == null || musicReq.getId().isEmpty() ||
				musicReq.getTitle() == null || musicReq.getTitle().isEmpty() || 
				musicReq.getArtist() == null || musicReq.getArtist().isEmpty() || 
				musicReq.getGenre() == null || musicReq.getGenre().isEmpty() || 
				musicReq.getDate() == null || musicReq.getDate().isEmpty() || 
				musicReq.getMusicFile() == null || musicReq.getMusicFile().isEmpty()) {

			throw new RuntimeException("Music data is incomplete or invalid. Please check the required fields.");
		}


		// Validate the file type
		String mimeType = musicReq.getMusicFile().getContentType();
		if (!isValidMusicFile(mimeType)) {
			log.error("Invalid file type: {}", mimeType);
			throw new InvalidFileTypeException("Only music files are allowed. Provided file type: " + mimeType);
		}

		// convert to DTO
		MusicDTO musicDTO = modelMapper.map(musicReq, MusicDTO.class);
		log.info("Converted To DTO {} :",musicDTO);

		// Proceed with your file processing logic (e.g., save to MinIO, database, etc.)
		log.info("File is valid. Proceeding with processing...");
		MusicDTO musicStatus = musicService.modifyMusicById(musicDTO);
		
		if(musicStatus==null) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		//			// convert  DTO to pojo MusicRes
		MusicRes musicRes = modelMapper.map(musicStatus, MusicRes.class);
		log.info("Response Atfter Converting to MusicReq {} :",musicRes);

		return new ResponseEntity<>(musicRes, HttpStatus.CREATED);
	}




	// testing uploading file 
	@PostMapping("/upload")
	public ResponseEntity<String> uploadfile(@RequestParam("file") MultipartFile file){
		System.out.println(file.getOriginalFilename());
		System.out.println(file.getSize());
		System.out.println(file.getContentType());
		System.out.println(file.getName());

		String response = musicService.fileUpload(file);

		return ResponseEntity.ok(response);
	}

	private boolean isValidMusicFile(String mimeType) {
		// List of valid MIME types for music files
		List<String> validMimeTypes = Arrays.asList("audio/mpeg", "audio/wav", "audio/ogg", "audio/x-wav");

		return validMimeTypes.contains(mimeType);
	}
	
//	@PreAuthorize("hasRole('admin')") // no need handled by api-gateway
	@GetMapping("/admin")
	public String getAllUserDetails() {
		return "List of All User";
	}


}
