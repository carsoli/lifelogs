package application.controller;

import java.util.ArrayList;

import javafx.scene.image.ImageView;

public class FramesBufferController {
	private static ArrayList<ImageView> buffer;
	private static ArrayList<Boolean> canDisplayBuffer;//initial value are of Boolean IS NULL (NOT FALSE)
	
	public static int initializeFramesBuffer(int fImagesSize, int bufferSize) {
		//if the total images is less than buffer size, 
		//then make bufferSize smaller
		if(fImagesSize < bufferSize)
			bufferSize = fImagesSize;
		
		buffer = new ArrayList<ImageView>(bufferSize);
		FramesBufferController.setBuffer(buffer);
		return bufferSize;
	}
	
	public static void setCanDisplay(int idx, boolean canDisplay) {
		canDisplayBuffer.set(idx, canDisplay);
	}
	
	public static void setBuffer(ArrayList<ImageView> b) {
		FramesBufferController.buffer = b;
	}
	
	public static ArrayList<ImageView> getBuffer() {
		return FramesBufferController.buffer;
	}
	
	public static boolean enqueueFrame(ImageView i) {
		return buffer.add(i);
	}
	
	public static ImageView replaceFrame(int idx, ImageView newFrame) {
		return buffer.set(idx, newFrame);
	}

	
}
