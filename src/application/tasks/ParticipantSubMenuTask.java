package application.tasks;

import java.util.ArrayList;

import application.controller.MainController;
import application.structs.Participant;
import application.view.ViewUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

@SuppressWarnings("rawtypes")
public class ParticipantSubMenuTask extends Task{
	//to track current user choices for directory, participant, and day
//	private static ArrayList<Participant> pArrayList = null;
//	private static ArrayList<Day> dArrayList = null;
	
	@Override
	protected Object call() throws Exception {
		initializeParticipantsSubMenu();
        return null; 
	}

	
	@SuppressWarnings( "unchecked" )
	private static void initializeParticipantsSubMenu() {
		ArrayList<Participant> pArrayList = MainController.getmDataSource().getData();
		
		if(pArrayList != null) {
			ViewUtils.getChooseParticipantSM().getItems().clear(); //in case a new directory is chosen
			for (final Participant p: pArrayList) {
				MenuItem pItem = new MenuItem(p.getParticipantName());
				
				pItem.setId( "" + pArrayList.indexOf(p) );
				/**
				 * setting the id of the menu item in case we need to 
				 * be that order of that participant in the ArrayList
				 */
				ViewUtils.getChooseParticipantSM().getItems().add(pItem);
				pItem.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent e) {
						//no - load the participant images, then enable video creation menu
//						ViewUtils.getCreateVideoSM().setDisable(false); 

						//prevent loading different participants until the first is done
						ViewUtils.getChooseParticipantSM().setDisable(true);
						ViewUtils.getCreateVideoSM().setDisable(true);

						ViewUtils.getImagesTilePaneContainer().getChildren().clear();
						Task loadImagesTask = new LoadImagesTask();	
						
						Task displayGridImagesTask = new DisplayGridImagesTask();
						displayGridImagesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
							public void handle(WorkerStateEvent event) {
								Platform.runLater(() -> {
									ViewUtils.getChooseParticipantSM().setDisable(false);
									ViewUtils.getCreateVideoSM().setDisable(false);
								});
							}
						});
						loadImagesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
							 public void handle(WorkerStateEvent event) {
								MainController.addTask(displayGridImagesTask);
								MainController.execute(displayGridImagesTask);
							 }
					    });       
						int participantIdx = pArrayList.indexOf(p);
						MainController.setChosenP(participantIdx);

						Task DSMTask = new DaysSubMenuTask(pArrayList);
						DSMTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
							@Override
							public void handle(WorkerStateEvent event) {
								MainController.addTask(loadImagesTask);
								MainController.execute(loadImagesTask);								
							}
						});
						MainController.addTask(DSMTask);
						MainController.execute(DSMTask);
						//ORDER OF EXECUTION
						//DSM; updates createVideoSubMenu, then starts:
						//loadImagesTask; which basically adds to the dataStructures of Day.FImages, Participant.AllImages
						//then starts displayGridImagesTask; which replaces the empty HBoxes with the images
						//then when done; enables both menus; chooseParticipant, createVideo
					}
				});
			}
		}
	}

    
}
