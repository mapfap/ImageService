package com.mapfap.image.processing.imagescalr;

import com.mapfap.image.processing.ImageProcessor;
import com.mapfap.image.processing.ImageProcessorFactory;

/**
 * Factory for ImageProcessor using imgscalr library.
 * Pure Java, no dependencies on native library.
 * Some of features such as Gaussian Blur will be disabled. 
 * 
 * @author Sarun Wongtanakarn
 *
 */
public class ImageScalrProcessorFactory extends ImageProcessorFactory {

	/**
	 * @see ImageProcessorFactory#getImageProcessor()
	 */
	@Override
	public ImageProcessor getImageProcessor() {
		return new ImageScalrProcessor();
	}

}
