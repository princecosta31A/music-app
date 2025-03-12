package com.app.music.service.impl;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.music.dto.MusicDTO;
import com.app.music.entity.MusicEntity;
import com.app.music.exception.MongoDBIdNotFoundException;
import com.app.music.exception.MongoDBSaveException;
import com.app.music.pojo.MusicRes;
import com.app.music.repo.MusicRepository;
import com.app.music.service.MusicService;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class MusicServiceImpl implements MusicService {


	@Value("${minio.bucket-name}")
	private String bucketName;

	@Value("${minio_url}")
	private String minioUrl;


	private MusicRepository musicRepository;
	private MinioClient minioClient;

	public MusicServiceImpl(MusicRepository musicRepository,
			MinioClient minioClient) {
		this.musicRepository = musicRepository;
		this.minioClient = minioClient;
	}


	@Override
	public MusicDTO saveMusic(MusicDTO musicDTO) {
		log.info("Request Recieved in Service :{}",musicDTO);

		// logic to save media file into minio
		try {
			//	             Ensure the bucket exists
			if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
				minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
				log.debug("Bucket created successfully: {}",  bucketName);
			} else {
				log.debug("Bucket already exists: {}",bucketName);
			}
			// Upload the file
			String fileName = uploadToMinio(musicDTO.getMusicFile());

			// Save metadata to MongoDB
			MusicEntity musicEntity = new MusicEntity(
					null,
					musicDTO.getTitle(),
					musicDTO.getArtist(),
					musicDTO.getGenre(),
					musicDTO.getDate(),
					fileName
					);

			// Save the converted MusicDAO to the database
			MusicEntity savedMusic = musicRepository.save(musicEntity);

			if (savedMusic != null && savedMusic.getId() != null) {
				log.info("Data Saved Successfully :{}",savedMusic.getId());
				musicDTO.setId(musicEntity.getId());
				musicDTO.setFileUrl(generateFileUrl(fileName));
				log.info("File uploaded successfully: {}", fileName);
				return musicDTO;

			} else {
				log.error("Failed to save music data. in database..");
				throw new MongoDBSaveException("Error occurred while saving data to MongoDB");
			}

		} catch (Exception e) {
			throw new RuntimeException("Error uploading file to MinIO: " + e.getMessage());
		}  
	}


	@Override
	public List<MusicRes> restoreMusic() {
		log.info("Getting All Music At Service from Database ");


		List<MusicEntity> musicEntities = musicRepository.findAll();

		if(musicEntities==null || musicEntities.isEmpty()) {
			throw new RuntimeException("No Music Record Found");
		}
		log.debug("Fetched {} music records", musicEntities.size());


		// Convert MusicEntity objects to MusicDTO objects
		List<MusicRes> musicRes = musicEntities.stream().map(entity -> {
			MusicRes res = new MusicRes();
			res.setId(entity.getId());
			res.setTitle(entity.getTitle());
			res.setArtist(entity.getArtist());
			res.setGenre(entity.getGenre());
			res.setDate(entity.getDate());

			// Retrieve music file from MinIO
			try {
				InputStream musicStream = minioClient.getObject(
						GetObjectArgs.builder()
						.bucket(bucketName)
						.object(entity.getFileName())
						.build()
						);

				// Set file URL (this could be the path or URL to the file in MinIO)
				String fileUrl = generateFileUrl(entity.getFileName()); // Implement this method to generate the URL
				res.setFileUrl(fileUrl);
			} catch (Exception e) {
				log.error("Error fetching file from MinIO: {}", e.getMessage());
			}
			return res;
		}).collect(Collectors.toList());

		log.debug("Converted {} entities to DTOs", musicRes.size());
		return musicRes;
	}


	@Override
	public boolean removeMusic(String id) {
		log.info("Processing Deletion From Database :{}",id);



		// Find the music entity in the database
		MusicEntity musicEntity = musicRepository.findById(id).orElse(null);

		if (musicEntity == null) {
			log.warn("Music with ID {} not found in database.", id);
			throw new MongoDBIdNotFoundException("ID " + id + " does not exist in the database.");
		}
		try {

			// Delete the music file from MinIO
			String fileName = musicEntity.getFileName();
			if (fileName != null && !fileName.isEmpty()) {
				deleteFromMinio(fileName);

				// Delete the music entity from MongoDB
				musicRepository.deleteById(id);
				log.info("Successfully deleted music with ID {} from database and MinIO.", id);
				return true;

			}

		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException(e.getMessage());
		}

		return false;
	}


	@Override
	public MusicRes restoreMusicById(String id) {
		log.info("Fetching Music Details From Database :{}",id);

		// Find the music entity in the database
		MusicEntity musicEntity = musicRepository.findById(id).orElse(null);

		if (musicEntity == null) {
			log.warn("Music with ID {} not found in database.", id);
			throw new MongoDBIdNotFoundException("ID " + id + " does not exist in the database.");
		}


		try {
			// Fetch the song file from MinIO
			String fileName = musicEntity.getFileName();
			if (fileName == null || fileName.isEmpty()) {
				throw new RuntimeException("File name is missing for music ID " + id);
			}

			// Create and return the MusicRes object
			MusicRes res = new MusicRes();
			res.setId(musicEntity.getId());
			res.setTitle(musicEntity.getTitle());
			res.setArtist(musicEntity.getArtist());
			res.setGenre(musicEntity.getGenre());
			res.setDate(musicEntity.getDate());

			// Retrieve music file from MinIO
			InputStream musicStream = minioClient.getObject(
					GetObjectArgs.builder()
					.bucket(bucketName)
					.object(musicEntity.getFileName())
					.build()
					);

			// Set file URL (this could be the path or URL to the file in MinIO)
			String fileUrl = generateFileUrl(musicEntity.getFileName()); // Implement this method to generate the URL
			res.setFileUrl(fileUrl);
			return res;



		} catch (Exception e) {
			log.error("Error while restoring music by ID {}: {}", id, e.getMessage(), e);
			throw new RuntimeException("Failed to restore music for ID " + id + ". Please try again later.");
		}
	}


	@Override
	public MusicDTO modifyMusicById(MusicDTO musicDTO) {
		log.info("Request Recieved in Service :{}",musicDTO);

		// Find the music entity in the database
		MusicEntity musicEntity = musicRepository.findById(musicDTO.getId()).orElse(null);

		if (musicEntity == null) {
			log.warn("Music with ID {} not found in database.", musicDTO.getId());
			throw new MongoDBIdNotFoundException("ID " + musicDTO.getId() + " does not exist in the database.");
		}

		// Update the music entity with new details
		musicEntity.setTitle(musicDTO.getTitle());
		musicEntity.setArtist(musicDTO.getArtist());
		musicEntity.setGenre(musicDTO.getGenre());
		musicEntity.setDate(musicDTO.getDate());

		// If a new music file is provided, handle file replacement
		if (musicDTO.getMusicFile() != null && !musicDTO.getMusicFile().isEmpty()) {
			try {
				// Delete the old file from MinIO
				String oldFileName = musicEntity.getFileName();
				if (oldFileName != null && !oldFileName.isEmpty()) {
					deleteFromMinio(oldFileName);
				}

				// Upload the new music file to MinIO
				String newFileName = uploadToMinio(musicDTO.getMusicFile());

				// Update the file name in the music entity
				musicEntity.setFileName(newFileName);

			} catch (Exception e) {
				log.error("Error updating music file in MinIO: {}", e.getMessage(), e);
				throw new RuntimeException("Failed to update music file. Please try again later.");
			}
		}

		// Save the updated music entity to MongoDB
		MusicEntity updatedMusicEntity = musicRepository.save(musicEntity);

		// Convert the updated entity to MusicDTO to return
		return new MusicDTO(
				updatedMusicEntity.getId(),
				updatedMusicEntity.getTitle(),
				updatedMusicEntity.getArtist(),
				updatedMusicEntity.getGenre(),
				updatedMusicEntity.getDate(),
				generateFileUrl(updatedMusicEntity.getFileName()),
				null  // As we're not returning the file itself in DTO
				);
	}
	
	
	private String generateFileUrl(String fileName) {
		// Construct the URL to the file in MinIO (adjust based on your setup)
		return minioUrl + fileName;
	}
	
	private String uploadToMinio(MultipartFile musicFile) throws Exception {
		try {
			// Generate a unique file name
			String fileName = UUID.randomUUID() + "_" + musicFile.
					getOriginalFilename();
			// Upload the file to MinIO
			minioClient.putObject(
					PutObjectArgs.builder()
					.bucket(bucketName)
					.object(fileName)
					.stream(musicFile.getInputStream(), musicFile.getSize(), -1)
					.contentType(musicFile.getContentType())
					.build()
					);

			log.info("File uploaded successfully to MinIO with filename: {}", fileName);
			return fileName;
		} catch (Exception e) {
			log.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
			throw new Exception("Error uploading file to MinIO");
		}
	}

	private void deleteFromMinio(String fileName) throws Exception {
		try {
			log.info("Deleting file {} from MinIO bucket {}", fileName, bucketName);

			// Delete the object from MinIO
			minioClient.removeObject(
					RemoveObjectArgs.builder()
					.bucket(bucketName)
					.object(fileName)
					.build()
					);

			log.info("File {} successfully deleted from MinIO bucket {}", fileName, bucketName);
		} catch (Exception e) {
			log.error("Failed to delete file {} from MinIO bucket {}: {}", fileName, bucketName, e.getMessage(), e);
			throw new RuntimeException("Failed to delete file from MinIO bucket") ;
		}
	}

	@Override
	public String fileUpload(MultipartFile file) {
		// TODO Auto-generated method stub
		System.out.println("Getting file to Service Layer :"+file.getOriginalFilename());
		try {
			//            Ensure the bucket exists
			if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
				minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
				log.debug("Bucket created successfully: {}",  bucketName);
			} else {
				log.debug("Bucket already exists: {}",bucketName);
			}

			// Generate a unique file name
			String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

			// Upload the file
			minioClient.putObject(
					PutObjectArgs.builder()
					.bucket(bucketName)
					.object(fileName)
					.stream(file.getInputStream(), file.getSize(), -1)
					.contentType(file.getContentType())
					.build()
					);
			System.out.println("File is Uploaded");
			return "File uploaded successfully: " + fileName;

		} catch (Exception e) {
			throw new RuntimeException("Error uploading file to MinIO: " + e.getMessage());
		}
	}

}