package com.mapfap.image.persistence;

import java.util.logging.Logger;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.mapfap.image.entity.Image;

public class ImagePersistence {
	
	private final EntityManager manager;

	/**
	 * Construct a new ImagePersistence with injected EntityManager for using.
	 * @param manager EntityManager for accessing JPA services.
	 */
	public ImagePersistence(EntityManager manager) {
		this.manager = manager;
	}
	
	public Image load(String hashedPath) {
		return manager.find(Image.class, hashedPath);
	}

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
