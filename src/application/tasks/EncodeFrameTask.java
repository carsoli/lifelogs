//package application.tasks;
//
//import java.awt.image.BufferedImage;
//import java.io.File;
//
//import application.utils.ImagesToVideoConverter;
//import application.view.ViewUtils;
//import javafx.concurrent.Task;
//
//@SuppressWarnings("rawtypes")
//public class EncodeFrameTask extends Task {
//	private long frameIndex;
//	private File image; 
//	
//
//	public EncodeFrameTask(long idx, File img) {
//		this.frameIndex = idx; 
//		this.image = img; 
//	}
//	
//	@Override
//	protected Object call() throws Exception {
//		BufferedImage buffImg = null;
//		buffImg = ViewUtils.createBufferedImage(this.image);
//		ImagesToVideoConverter.encodeImageFile(buffImg, this.frameIndex);
//		return null;
//	}
//	
//}
