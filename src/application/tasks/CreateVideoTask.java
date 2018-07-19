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
		int chosenPIndex = MainController.getChosenP();
		DataSource mDataSource = MainController.getmDataSource();
		Participant chosenParticipant = mDataSource.getData().get(chosenPIndex);
		Day chosenDay = chosenParticipant.getDays().get(dayIdx);
		//instead of passing an argument:
//		Day chosenDay = chosenParticipant.getDays().get(MainController.getChosenD());
		String outputFileName =  chosenParticipant.getParticipantName() + chosenDay.getName() + ".mp4"; 
		
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
		}
	
	}

}
