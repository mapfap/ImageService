package com.mapfap.image.processing.imagej;

import com.mapfap.image.processing.ImageProcessor;
import com.mapfap.image.processing.ImageProcessorFactory;

/**
 * Factory for ImageProcessor using ImageJ library.
 * Pure Java, no dependencies on native library.
 * Some of features such as Gaussian Blur will be disabled. 
 * 
 * @author Sarun Wongtanakarn
 *
 */
public class ImageJProcessorFactory extends ImageProcessorFactory {

	/**
	 * @see ImageProcessorFactory#getImageProcessor()
	 */
	@Override
	public ImageProcessor getImageProcessor() {
		return new ImageJProcessor();
	}

}
