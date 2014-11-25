package com.mapfap.image.entity;

import java.sql.Timestamp;
import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.codec.digest.DigestUtils;

@Entity
public class Image {
	
	@Id
	private String id;
	private String fileName;
	private Timestamp createdTime;

	public Image() {
		
	}
	
	public Image(String fileName) {
		this.fileName = fileName;
		this.createdTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
		this.id = DigestUtils.md5Hex(fileName + ":" + createdTime.getTime());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public Timestamp getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Timestamp createdTime) {
		this.createdTime = createdTime;
	}
	
}
