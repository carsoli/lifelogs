package application.tasks;

import java.io.File;
import java.util.ArrayList;

import application.controller.MainController;
import application.structs.DataSource;
import application.structs.Day;
import application.structs.Event;
import application.view.ViewUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

@SuppressWarnings("rawtypes")
public class LoadImagesTask extends Task {
	private DataSource mDataSource;
	
	public LoadImagesTask(DataSource mDataSource) {
		this.mDataSource = mDataSource;
	}
	
	@Override
	protected Object call() throws Exception {
		loadParticipantImages(this.mDataSource);
		return null;
	}
	
	private static ArrayList<String> loadParticipantImages(DataSource mDataSource){
		ArrayList<String> pathsArrayList = null;
		if(MainController.getChosenP() != -1) {
			System.out.println("CHOSEN PARTICIPANT ID: "+ MainController.getChosenP());
			ArrayList<Day> pDays= mDataSource.getData().get(MainController.getChosenP()).getDays();
			for(Day d: pDays) {
				ArrayList<Event> dEvents = d.getEvents();
				for(Event event :dEvents) {
					File[] eventImages = event.getFImages();
					for(File i: eventImages) {
						final ImageView imageView = ViewUtils.createImageView(i);
						d.addFImage(i);
//						when a day is selected, all we gotta do, is loop that list, and
						//create the buffimgs in the create video task
//						BufferedImage buffImg = ViewUtils.createBufferedImage(i);
//						ImagesToVideoConvertor.encodeImageFile(buffImg, frameIndex);
				        final HBox imageHBox = new HBox();
						Platform.runLater(new Runnable() {
			                public void run() {
			                	//every image's loading runs on a separate background worker thread.
			                	//will continue to add the images even if a new directory is chosen
			                	imageHBox.getChildren().add(imageView);
			                	ViewUtils.getImagesTilePaneContainer().getChildren().add(imageHBox);
			                }
			            });
//						frameIndex++;
					}
				}
				int dayIdx = pDays.indexOf(d);
				MenuItem currDay = ViewUtils.getCreateVideoSM().getItems().get(dayIdx);
				currDay.setDisable(false);
			}
		}
			
		return pathsArrayList;
	}

}
