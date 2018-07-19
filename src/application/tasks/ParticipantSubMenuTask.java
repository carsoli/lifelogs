package application.tasks;

import java.util.ArrayList;

import application.controller.MainController;
import application.structs.DataSource;
import application.structs.Day;
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
	private static DataSource mDataSource = null;
	private static ArrayList<Participant> pArrayList = null;
	private static ArrayList<Day> dArrayList = null;
	
	
	@Override
	protected Object call() throws Exception {
		initializeParticipantsSubMenu();
        return null; 
	}

	public static void setMainDataSource(DataSource ds) {
		mDataSource = ds; 
	}
	
	@SuppressWarnings( "unchecked" )
	private static void initializeParticipantsSubMenu() {
		pArrayList = mDataSource.getData();
		
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
						ViewUtils.getCreateVideoSM().setDisable(false);

						ViewUtils.getImagesTilePaneContainer().getChildren().clear();
						MainController.setmDataSource(mDataSource);
						Task loadImagesTask = new LoadImagesTask(mDataSource);	
						MainController.addTask(loadImagesTask);
						//prevent loading different images until the first is done (can't do it with threads)
						ViewUtils.getChooseParticipantSM().setDisable(true);
						loadImagesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
							 public void handle(WorkerStateEvent event) {
					                Platform.runLater(new Runnable() {
		                                @Override
		                                public void run() {
		                                	ViewUtils.getChooseParticipantSM().setDisable(false);
		                                }
		                            });
					            }
					        });       
						MainController.execute(loadImagesTask);
						//the execute call creates a new worker thread using the threadFactory method
						//specified in the ExecutorService "threadPool"
						int participantIdx = pArrayList.indexOf(p);
						MainController.setChosenP(participantIdx);
						ViewUtils.getCreateVideoSM().setDisable(false);
						Task DSMTask = new DaysSubMenuTask(ViewUtils.getCreateVideoSM(), pArrayList, dArrayList);
						MainController.addTask(DSMTask);
						MainController.execute(DSMTask);
					}
				});
			}
		}
	}

    
}
