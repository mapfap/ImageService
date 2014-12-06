package com.mapfap.image.processing;


/**
 * Provides abilities to process the image using any image processing library.
 * 
 * @author Sarun Wongtanakarn
 */
public interface ImageProcessor {
	
	/**
	 * Read the image from given path and apply process follow the given instruction.
	 * @param fileName name of image file to be read.
	 * @param instruction instruction for image processing.
	 * @return name of image result file that was written. 
	 */
	public String process(String fileName, ProcessInstruction instruction);
}
