package application.tasks;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import application.controller.MainController;
import application.structs.DataSource;
import application.structs.Day;
import application.structs.Participant;
import application.utils.Constants;
import application.utils.ImagesToVideoConverter;
import application.view.ViewUtils;
import javafx.concurrent.Task;

@SuppressWarnings("rawtypes")
public class CreateVideoTask extends Task{
	private int dayIdx; 
	
	public CreateVideoTask(int dayIdx) {
		this.dayIdx = dayIdx; 
	}
	
	@Override
	protected Object call() throws Exception {
		createDayVideo(this.dayIdx);
		return null;
	}

	private void createDayVideo(int dayIdx) { 
		this.dayIdx = dayIdx;
		//TODO: the file name varies according to P & D 
		//the user may choose multiple Days, and we can execute both tasks, 
		//however play only the last chosen D? 
		//or maybe generate a grid of videos
		int chosenPIndex = MainController.getChosenP();
		DataSource mDataSource = MainController.getmDataSource();
		Participant chosenParticipant = mDataSource.getData().get(chosenPIndex);
		Day chosenDay = chosenParticipant.getDays().get(dayIdx);
		String outputFileName =  chosenParticipant.getParticipantName() + "-" 
				+ chosenDay.getName() + ".mp4"; 
		//WHAT!
		ImagesToVideoConverter.initializeConvertor(outputFileName, 
			Constants.FORMAT_NAME, Constants.CODEC_NAME, Constants.FPS, 
			Constants.DEFAULT_IMAGE_WIDTH, 
			Constants.DEFAULT_IMAGE_HEIGHT);
		
		long frameIndex = 0;
		ArrayList<File> fImages = chosenDay.getFImages();//all images of this day 
		BufferedImage buffImg = null;
		for(File img: fImages) {//all images for this day
			buffImg = ViewUtils.createBufferedImage(img);
			ImagesToVideoConverter.encodeImageFile(buffImg, frameIndex);
			frameIndex++;
			/*Failed (humble breaks)*/
//			Task encodingTask = new EncodeFrameTask(frameIndex, img);
//			MainController.addTask(encodingTask);
//			MainController.execute(encodingTask);
//			frameIndex++;
			
		}
	
	}

}
