package com.mapfap.image.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileManager {
	
	private static final String FILE_STORAGE = "images/";
	private static FileManager instance;
	
	private FileManager() {
		// Singleton
	}
	
	public static FileManager getInstance() {
		if (instance == null) {
			instance = new FileManager();
		}
		return instance;
	}
	
	public void createFileStorage() {
		File theDir = new File(FILE_STORAGE);

		  if (!theDir.exists()) {
		    System.out.println("[Debug] Creating directory: " + FILE_STORAGE);
		    boolean result = false;

		    try {
		        theDir.mkdir();
		        result = true;
		     } catch (SecurityException e){
		        e.printStackTrace();
		     }        
		     if (result) {    
		       System.out.println("[Debug] Directory created");
		     }
		  }
	}
	
	public String getFilePath(String fileName) {
		return FILE_STORAGE + fileName;
	}

	public File getFile(String fileName) {
		return new File(getFilePath(fileName));
	}

	public FileOutputStream getFileOutputStream(String fileName) {
		try {
			return new FileOutputStream(getFilePath(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
