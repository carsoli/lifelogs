package application.tasks;

import java.io.File;
import java.util.ArrayList;

import application.controller.MainController;
import application.structs.DataSource;
import application.structs.Day;
import application.structs.Event;
import application.utils.Constants;
import application.view.ViewUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;


/*
 * NOTE:
 * Generally, Tasks should not interact directly with the UI. 
 * Doing so creates a tight coupling between a specific Task implementation and a specific part of your UI. 
 * However, when you do want to create such a coupling, you must ensure that you use Platform.runLater so 
 * that any modifications of the scene graph occur on the FX Application Thread.
 * */
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
			ArrayList<Day> pDays= mDataSource.getData().get(MainController.getChosenP()).getDays();
			for(Day d: pDays) {
				ArrayList<Event> dEvents = d.getEvents();
				for(Event event :dEvents) {
					File[] eventImages = event.getFImages();
					
					for(File i: eventImages) {
						//for VideoPlayer
						final ImageView gridImageView = ViewUtils.createImageView(i, 
								Constants.IMAGE_SIZE, 0);
						d.addFImage(i);

						final HBox imageHBox = new HBox();
						Platform.runLater(new Runnable() {
			                public void run() {
			                	//TODO: every image's loading runs on a separate background worker thread.
			                	//will continue to add the images even if a new directory is chosen; fix this
			                	//==cancel the task of loading or prevent further loading 
			                	//in this case, also fic it by another way other than UI disabling the loading menu item
			                	//TODO: order is messed bc the list is not ordered in the first place 
			                	//and the Platform.RunLater does not execute in FIFO
			                	//NOTE: do not remove the runLater (check the note at the top)
			                	imageHBox.getChildren().add(gridImageView);
			                	ViewUtils.getImagesTilePaneContainer().getChildren().add(imageHBox);
			                }
			            });
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
