package application.tasks;

import java.io.File;
import java.util.ArrayList;

import application.controller.MainController;
import application.structs.Day;
import application.structs.Event;
import application.structs.Participant;
import application.view.ViewUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
	
	@Override
	protected Object call() throws Exception {
		loadParticipantImages();
		return null;
	}
	
	private static void loadParticipantImages(){
		if(MainController.getChosenP() != -1) {
			Participant chosenParticipant = MainController.getmDataSource().getData().get(MainController.getChosenP());
			if(chosenParticipant.getAllImages().size() >0) {
				//if the same participant is chosen twice, we need to clear all images
				chosenParticipant.getAllImages().clear();
			}
			ArrayList<Day> pDays= chosenParticipant.getDays();
			for(Day d: pDays) {
				ArrayList<Event> dEvents = d.getEvents();
				//if the same day is loaded twice for any reason, 
				//we would add the FImages for it twice(double the size)
				//when calling "d.addFImage(i)" inside the inner-most forLoop
				if(d.getFImages().size() > 0) {
					d.getFImages().clear();
				}
				
				for(Event event :dEvents) {					
					File[] eventImages = event.getFImages();
					for(File i: eventImages) {
						//for GridDisplay, VideoPlayer and FramesGlider
						d.addFImage(i); 
						chosenParticipant.addToAllImages(i);
						Platform.runLater(new Runnable() {
			                public void run() {
			                	//adds an empty HBox initially, which is then replaced when displayingGridImages
			                	ViewUtils.getImagesTilePaneContainer().getChildren().add(new HBox());
			                }
			            });
					}
				}
			}
		}
	}

}
