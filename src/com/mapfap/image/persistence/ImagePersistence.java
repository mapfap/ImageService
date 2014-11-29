package com.mapfap.image.persistence;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.mapfap.image.entity.Image;

/**
 * Persistence for image resource.
 * save, load, delete image to the database using Eclipselink JPA.
 * 
 * @author Sarun Wongtanakarn
 */
public class ImagePersistence {
	
	private final EntityManager manager;

	/**
	 * Construct a new ImagePersistence with injected EntityManager for using.
	 * @param manager EntityManager for accessing JPA services.
	 */
	public ImagePersistence(EntityManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Load image from database with specified ID.
	 * @param id ID of image for matching.
	 * @return image that ID matched.
	 */
	public Image load(String id) {
		return manager.find(Image.class, id);
	}
	
	/**
	 * List all images from database.
	 * @return list of images.
	 */
	public List<Image> listImages() {
		Query query = manager.createQuery("SELECT i FROM Image i");
		@SuppressWarnings("unchecked")
		List<Image> images = query.getResultList();
		return Collections.unmodifiableList(images);
	}

	/**
	 * Save image to database.
	 * @param image image to be saved.
	 * @return true if image is saved successfully; false otherwise.
	 */
	public boolean save(Image image) {
		if ( image == null ) {
			throw new IllegalArgumentException("Can't save a null image");
		}
		EntityTransaction tx = manager.getTransaction();
		try {
			tx.begin();
			manager.persist( image );
			tx.commit();
			return true;
		} catch ( EntityExistsException ex ) {
			handleDatabaseError( tx, ex );
			return false;
		}
	}
	
	/**
	 * Delete image from database with specified ID.
	 * @param id ID of image for matching.
	 * @return true if image is deleted successfully; false otherwise.
	 */
	public boolean delete(String id) {
		EntityTransaction tx = manager.getTransaction();
		Image image = load(id);
		try {
			tx.begin();
			manager.remove(image);
			tx.commit();
			return true;
		} catch ( EntityExistsException ex ) {
			handleDatabaseError( tx, ex );
			return false;
		}
	}
	
	/**
	 * Delete all images from database.
	 */
	public void clearAll() {
		EntityTransaction tx = manager.getTransaction();
		try {
			tx.begin();
			Query query = manager.createQuery("DELETE FROM Image");
			int deletedCount = query.executeUpdate();
			tx.commit();
			System.out.println(deletedCount + " row(s) deleted.");
		} catch ( EntityExistsException ex ) {
			handleDatabaseError( tx, ex );
		}
	}
	
	/**
	 * Handle error from database, try to rollback it if possible.
	 * @param tx current EntityTransaction.
	 * @param ex EntityExistsException that occurs.
	 */
	private void handleDatabaseError(EntityTransaction tx, EntityExistsException ex){
		Logger.getLogger(this.getClass().getName()).warning(ex.getMessage());
		if (tx.isActive()) {
			try { 
				tx.rollback();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
