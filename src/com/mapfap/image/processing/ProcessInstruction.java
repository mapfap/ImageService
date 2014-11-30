package com.mapfap.image.processing;

/**
 * Instruction for image processing operation.
 * Holds options available in this application such as brightness, grayscale and gaussian.
 * Instance of this class is working with ImageProcessor.
 * 
 * @author Sarun Wongtanakarn
 */
public class ProcessInstruction {
	
	private int width;
	private int height;
	private Double brightness;
	private boolean gaussian;
	private boolean grayscale;
	
	public ProcessInstruction() {
		
	}
	
	public ProcessInstruction(int width, int height, Double brightness, boolean gaussian, boolean grayscale) {
		this.width = width;
		this.height = height;
		this.brightness = brightness;
		this.gaussian = gaussian;
		this.grayscale = grayscale;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Double getBrightness() {
		return brightness;
	}

	public void setBrightness(Double brightness) {
		this.brightness = brightness;
	}

	public boolean isGaussian() {
		return gaussian;
	}

	public void setGaussian(boolean gaussian) {
		this.gaussian = gaussian;
	}

	public boolean isGrayscale() {
		return grayscale;
	}

	public void setGrayscale(boolean grayscale) {
		this.grayscale = grayscale;
	}

}
