package com.app.music.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public class CustomMultipartFile implements MultipartFile {
	 private final String fileName;
	    private final byte[] content;

	    public CustomMultipartFile(String fileName, byte[] content) {
	        this.fileName = fileName;
	        this.content = content;
	    }

	    @Override
	    public String getName() {
	        return fileName;
	    }

	    @Override
	    public String getOriginalFilename() {
	        return fileName;
	    }

	    @Override
	    public String getContentType() {
	        return "audio/mpeg"; // or another appropriate MIME type
	    }

	    @Override
	    public boolean isEmpty() {
	        return content.length == 0;
	    }

	    @Override
	    public long getSize() {
	        return content.length;
	    }

	    @Override
	    public byte[] getBytes() throws IOException {
	        return content;
	    }

	    @Override
	    public InputStream getInputStream() throws IOException {
	        return new ByteArrayInputStream(content);
	    }

	    @Override
	    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
	        // Implement if needed
	    }

}
