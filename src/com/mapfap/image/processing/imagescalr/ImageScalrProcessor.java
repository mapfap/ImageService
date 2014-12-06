package com.mapfap.image.processing.imagescalr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import com.mapfap.image.processing.ImageProcessor;
import com.mapfap.image.processing.ProcessInstruction;
import com.mapfap.image.resource.ImageResource;

/**
 * ImageProcessor using imgscalr library.
 * Pure Java, no dependencies on native library.
 * Some of features such as Gaussian Blur will be disabled. 
 * 
 * @author Sarun Wongtanakarn
 *
 */
public class ImageScalrProcessor implements ImageProcessor {

	/**
	 * @see ImageProcessor#process(String, ProcessInstruction)
	 */
	@Override
	public String process(String fileName, ProcessInstruction instruction) {
		try {
			BufferedImage original = ImageIO.read(new File(ImageResource.FILE_STORAGE + fileName));
			int width = Math.abs(instruction.getWidth());
			int height = Math.abs(instruction.getHeight());
			
			BufferedImage result;
			if (instruction.isGrayscale()) {
				result = Scalr.resize(original, Scalr.Method.SPEED, Scalr.Mode.FIT_EXACT, width, height, Scalr.OP_GRAYSCALE);
			} else {				
				result = Scalr.resize(original, Scalr.Method.SPEED, Scalr.Mode.FIT_EXACT, width, height);
			}
			
			String outputFileName = ImageResource.FILE_STORAGE + "_" + fileName;
			File outputFile = new File(outputFileName);
		    ImageIO.write(result, "png", outputFile);
		    return outputFileName;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
