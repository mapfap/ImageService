package com.mapfap.image.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Facade controller to manage file. 
 * @author Sarun Wongtanakarn
 *
 */
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
	
	/**
	 * Create a directory for storing file.
	 */
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
	
	/**
	 * Get full path of given file name.
	 * @param fileName name of file to find.
	 * @return full path of given file name.
	 */
	public String getFilePath(String fileName) {
		return FILE_STORAGE + fileName;
	}

	/**
	 * Get File from given file name.
	 * @param fileName name of file to find.
	 * @return File from given file name.
	 */
	public File getFile(String fileName) {
		return new File(getFilePath(fileName));
	}

	/**
	 * Get File as a stream from given file name.
	 * @param fileName name of file to find.
	 * @return File File as a stream from given file name.
	 */
	public FileOutputStream getFileOutputStream(String fileName) {
		try {
			return new FileOutputStream(getFilePath(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
