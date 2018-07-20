package application.tasks;

import java.io.File;
import java.util.ArrayList;

import application.controller.FramesGliderController;
import application.controller.FramesBufferController;
import application.utils.Constants;
import application.view.ViewUtils;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;

@SuppressWarnings("rawtypes")
public class PreloadFramesTask extends Task{
	private int bufferSize; 
	
	@Override
	protected Object call() throws Exception {
		ViewUtils.sortChosenImages();//this sorts the chosenImages and uses the output to setFImages()
		ArrayList<File> chosenImages = ViewUtils.getChosenImages();
		
//		System.out.println("size of chosen images: "+ chosenImages.size());
		
		bufferSize = FramesBufferController.initializeFramesBuffer(chosenImages.size(), 
				Constants.FRAMES_BUFFER_SIZE);
		ImageView imgView = null;
	
		while(FramesGliderController.getLastLoadedFrame() < bufferSize) {
			imgView = ViewUtils.createImageView(chosenImages.get(FramesGliderController.getLastLoadedFrame()),
					0,ViewUtils.getMainScene().getHeight());
			FramesBufferController.enqueueFrame(imgView); //adds to end of arrayList(like queue)
//			System.out.println("enqueuing: " + chosenImages.get(FramesGliderController.getLastLoadedFrame()).getName());
			FramesGliderController.IncremenetLastLoadedFrame();
		}

		return null;
	}

}
