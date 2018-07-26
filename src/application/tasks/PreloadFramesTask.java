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
		
		bufferSize = FramesBufferController.initializeFramesBuffer(chosenImages.size(), 
				Constants.FRAMES_BUFFER_SIZE);
		ImageView imgView = null;
		int initLLI = FramesGliderController.getLastLoadedFrame(); //lli before the task begins
		while(FramesGliderController.getLastLoadedFrame() < initLLI + bufferSize) {
			imgView = ViewUtils.createImageView(chosenImages.get(FramesGliderController.getLastLoadedFrame()),
					//TODO: try to add the correct height from the beginning
					//instead of updating it in the updateFrameHbox fn in FramesGlider
					0,ViewUtils.getMainScene().getHeight()); 
			FramesBufferController.enqueueFrame(imgView); //adds to end of arrayList(like queue)
			
			FramesGliderController.IncremenetLastLoadedFrame();
		}

		return null;
	}

}
