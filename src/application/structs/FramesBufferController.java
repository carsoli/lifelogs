package application.structs;

import java.util.ArrayList;

import javafx.scene.image.ImageView;

//MOVE THIS TO CONTROLLERS PKG
public class FramesBufferController {
	private static ArrayList<ImageView> buffer;
	private static ArrayList<Boolean> canDisplayBuffer;//initial value are of Boolean IS NULL (NOT FALSE)
	
	public static int initializeFramesBuffer(int fImagesSize, int bufferSize) {
		//if the total images is less than buffer size, 
		//then make bufferSize smaller
		if(fImagesSize < bufferSize)
			bufferSize = fImagesSize;
		
//		System.out.println("initializeFramesBuffer:: BufferSize: " + bufferSize );
		
		buffer = new ArrayList<ImageView>(bufferSize);
		//==========================failed attempt:
//		canDisplayBuffer = new ArrayList<Boolean>(bufferSize);
//		System.out.println("canDisplayBuffer.size()" + canDisplayBuffer.size());
		//initializes them with true
//		Collections.fill(canDisplayBuffer, Boolean.TRUE);
//		FramesBufferController.setCanDisplayBuffer(canDisplayBuffer);
		
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

//	public static ArrayList<Boolean> getCanDisplayBuffer() {
//		return canDisplayBuffer;
//	}
//
//	public static void setCanDisplayBuffer(ArrayList<Boolean> canDisplayBuffer) {
//		FramesBufferController.canDisplayBuffer = canDisplayBuffer;
//	}
	
}
