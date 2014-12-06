package com.mapfap.image.processing;

/**
 * Abstract Factory Pattern.
 * For the sake of distribution, I decide to use various library for image processing.
 * 
 * @author Sarun Wongtanakarn
 */
public abstract class ImageProcessorFactory {
	
	private static ImageProcessorFactory factory;
	
	/** 
	 * this class shouldn't be instantiated, but constructor must be visible to subclasses.
	 */
	protected ImageProcessorFactory() { }
	
	/**
	 * Get a singleton instance of the ImageProcessorFactory.
	 * @return instance of a concrete ImageProcessorFactory
	 */
	public static ImageProcessorFactory getInstance() {
		if (factory == null) {
			String factoryClass = System.getProperty("ImageProcessor");
			if (factoryClass != null) {
				ClassLoader loader = ImageProcessorFactory.class.getClassLoader();
				try {
					factory = (ImageProcessorFactory) loader.loadClass(factoryClass).newInstance();
					return factory;
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			// No properties assigned...
			System.out.println("[DEBUG] using ImageJ library");
			factory = new com.mapfap.image.processing.imagej.ImageJProcessorFactory();
			
//			factory = new com.mapfap.image.processing.opencv.OpenCVProcessorFactory();
		}
		return factory;
	}
	
	/**
	 * Set DAO factory for.
	 * So it's able to inject the preferred DaoFactory.
	 * @param afactory a new factory to be used as concrete factory class.
	 */
	public static void setImageProcessorFactory(ImageProcessorFactory afactory ) {
		factory = afactory;
	}
	
	/**
	 * Get an instance of a data access object for Contact objects.
	 * Subclasses of the base DaoFactory class must provide a concrete
	 * instance of this method that returns a ContactDao suitable
	 * for their persistence framework.
	 * @return instance of Contact's DAO
	 */
	public abstract ImageProcessor getImageProcessor();
	
}