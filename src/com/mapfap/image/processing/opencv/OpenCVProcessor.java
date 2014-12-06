package com.mapfap.image.processing.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.mapfap.image.processing.ImageProcessor;
import com.mapfap.image.processing.ProcessInstruction;
import com.mapfap.image.resource.ImageResource;

/**
 * ImageProcessor using OpenCV library.
 * Robust, Fast and full of features but hard to deploy.
 * You need to compile OpenCV to native library.
 * 
 * @author Sarun Wongtanakarn
 *
 */
public class OpenCVProcessor implements ImageProcessor {

	/**
	 * @see ImageProcessor#process(String, ProcessInstruction)
	 */
	@Override
	public String process(String fileName, ProcessInstruction instruction) {
		int colorMode = (instruction.isGrayscale()) ? Highgui.CV_LOAD_IMAGE_GRAYSCALE : Highgui.CV_LOAD_IMAGE_COLOR;
		Mat original = Highgui.imread(ImageResource.FILE_STORAGE + fileName, colorMode);
		
		Mat result = new Mat();
		
		Size size = new Size(Math.abs(instruction.getWidth()), Math.abs(instruction.getHeight()));
		Imgproc.resize(original, result, size);
		
		if (instruction.isGaussian()) {
			Imgproc.GaussianBlur(result, result ,new Size(45, 45), 0);
		}
		if (instruction.getWidth() < 0) {
			Core.flip(result, result, 1);
		}
		
		if (instruction.getHeight() < 0) {
			Core.flip(result, result, 0);
		}
		
		if (instruction.getBrightness() != null) {			
			result.convertTo(result, -1, instruction.getBrightness(), 0);
		}
		String outputFileName = ImageResource.FILE_STORAGE + "_" + fileName;
		Highgui.imwrite(outputFileName, result);
	    return outputFileName;
	}

}
