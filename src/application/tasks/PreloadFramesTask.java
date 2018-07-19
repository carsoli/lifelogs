package application.tasks;

import java.io.File;
import java.util.ArrayList;

import application.controller.FramesGliderController;
import application.structs.FramesBufferController;
import application.utils.Constants;
import application.view.ViewUtils;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;

@SuppressWarnings("rawtypes")
public class PreloadFramesTask extends Task{
	private int bufferSize = Constants.FRAMES_BUFFER_SIZE; 
	
	@Override
	protected Object call() throws Exception {
		ViewUtils.sortChosenImages();//this sorts the chosenImages and uses the output to setFImages()
		ArrayList<File> chosenImages = ViewUtils.getChosenImages();
		bufferSize = FramesBufferController.init(chosenImages.size(), bufferSize);
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
