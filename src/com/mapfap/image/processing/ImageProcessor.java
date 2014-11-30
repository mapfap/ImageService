package com.mapfap.image.processing;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.mapfap.image.resource.ImageResource;

/**
 * Provides abilities to process the image using OpenCV library.
 * 
 * @author Sarun Wongtanakarn
 */
public class ImageProcessor {
	
	/**
	 * Setup the OpenCV library.
	 * NOTE: This requires native dynamic library such as .dll for Windows, .dylib for OSX
	 */
	public ImageProcessor() {
		// TODO: Change the path of OpenCV native library.
		System.load(new File("/usr/local/share/OpenCV/java/libopencv_java2410.dylib").getAbsolutePath());
		// System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // this does not work.
	}
	
	/**
	 * Read the image from given path and apply process follow the given instruction.
	 * @param fileName name of image file to be read.
	 * @param instruction instruction for image processing.
	 * @return name of image result file that was written. 
	 */
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
		String newFileName = ImageResource.FILE_STORAGE + "_" + fileName;
	    Highgui.imwrite(newFileName, result);
	    return newFileName;
	}
}
