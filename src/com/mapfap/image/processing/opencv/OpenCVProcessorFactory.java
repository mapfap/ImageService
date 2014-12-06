package com.mapfap.image.processing.opencv;

import java.io.File;

import com.mapfap.image.processing.ImageProcessor;
import com.mapfap.image.processing.ImageProcessorFactory;

/**
 * Factory for ImageProcessor using OpenCV library.
 * Robust, Fast and full of features but hard to deploy.
 * You need to compile OpenCV to native library.
 * 
 * @author Sarun Wongtanakarn
 *
 */
public class OpenCVProcessorFactory extends ImageProcessorFactory {

	// For Mac.
	public static final String OPENCV_PATH_MAC = "/usr/local/share/OpenCV/java/libopencv_java2410.dylib";

	// For  Linux.
	public static final String LINUX_LIB = "/usr/local/lib/";
	public static final String[] OPENCV_PATH_LINUX = new String[] { 
	"libopencv_calib3d.so", "libopencv_contrib.so", "libopencv_core.so", "libopencv_features2d.so", "libopencv_flann.so", "libopencv_gpu.so",
			"libopencv_highgui.so", "libopencv_imgproc.so", "libopencv_legacy.so", "libopencv_ml.so", "libopencv_nonfree.so",
			"libopencv_objdetect.so", "libopencv_ocl.so", "libopencv_photo.so", "libopencv_stitching.so", "libopencv_superres.so", "libopencv_ts.a",
			"libopencv_video.so", "libopencv_videostab.so" };

	/**
	 * @see ImageProcessorFactory#getImageProcessor()
	 */
	@Override
	public ImageProcessor getImageProcessor() {
		checkOS();
		return new OpenCVProcessor();
	}

	/**
	 * Check current OS and load native library.
	 */
	private static void checkOS() {
		String os = System.getProperty("os.name");
		if (os.contains("Mac")) {
			System.load(new File(OPENCV_PATH_MAC).getAbsolutePath());
			System.out.println("[Debug] Load library -> " + OPENCV_PATH_MAC);
		} else {
			for (String path : OPENCV_PATH_LINUX) {
				System.load(new File(LINUX_LIB + path).getAbsolutePath());
				System.out.println("[Debug] Load library -> " + LINUX_LIB + path);
			}
		}
	}

}
