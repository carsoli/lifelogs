package application.tasks;

import java.util.ArrayList;

import application.controller.MainController;
import application.structs.Day;
import application.structs.FramesBufferController;
import application.structs.Participant;
import application.view.FramesGlider;
import application.view.ViewUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

@SuppressWarnings("rawtypes")
public class DaysSubMenuTask extends Task{
	private Menu createVideosSM = new Menu();
	private ArrayList<Participant> pArrayList = new ArrayList<Participant>();
	private ArrayList<Day> dArrayList = new ArrayList<Day>();

	public DaysSubMenuTask(Menu createVideosSM, ArrayList<Participant> pArrayList, 
			ArrayList<Day> dArrayList) {
		this.createVideosSM = createVideosSM;
		this.pArrayList = pArrayList;
		this.dArrayList = dArrayList;
	}
	
	@Override
	protected Object call() throws Exception {
		initializeDaysSubMenu(this.createVideosSM, this.pArrayList, this.dArrayList);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static void initializeDaysSubMenu(Menu createVideosSM, 
			ArrayList<Participant> pArrayList, ArrayList<Day> dArrayList) {
		//create a days sub menu based on the choice of participant
		createVideosSM.getItems().clear();
		int chosenP = MainController.getChosenP(); 
		if(chosenP > -1 && pArrayList != null) {
			dArrayList = (pArrayList.get(chosenP)).getDays();
			for (Day d: dArrayList) {
				MenuItem dItem = new MenuItem(d.getName());
				int dayIdx = dArrayList.indexOf(d);
				dItem.setId("" + dayIdx);
				dItem.setDisable(true);//when the day's entirely loaded, it'll be enabled
				//thus allowing to start video creation
				createVideosSM.getItems().add(dItem);
				dItem.setOnAction(e -> {
					//until the curr vid is created, don't change the chosenP bc it's a global variable
					ViewUtils.getChooseParticipantSM().setDisable(true); 
					ViewUtils.getCreateVideoSM().setDisable(true);
					MainController.setChosenD(dayIdx); 
					//============COMMENTED FOR NOW: DONT DELETE****
//					Task createVideoTask = new CreateVideoTask(dayIdx);
//					MainController.addTask(createVideoTask);
//					createVideoTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//						@Override
//						public void handle(WorkerStateEvent event) {
//			                Platform.runLater(new Runnable() {
//		                        @Override
//		                        public void run() {
//			                    	ImagesToVideoConverter.EncodeVideoAndClose();
//			                    	ViewUtils.getChooseParticipantSM().setDisable(false);
//			                    	ViewUtils.getCreateVideoSM().setDisable(false);
//			                    	VideoPlayer.postProcessVideo();
//		                        }
//			                });
//						}
//					});
//					MainController.execute(createVideoTask);
					//=================================
					
					Task preloadFrames = new PreloadFramesTask();
					MainController.addTask(preloadFrames);
					preloadFrames.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
			                Platform.runLater(new Runnable() {//Read Note in loadImagesTask.java
		                        @Override
		                        public void run() {
		                        	//loaded the first 100 images; now can initialize glider
		                        	//TODO: later make the autoPlay and replay Controlled by the UI
		                        	FramesGlider.initializeFrameGlider(
		                        			FramesBufferController.getBuffer(), true, 
//		                        			true, 
		                        			false);
			                    	ViewUtils.getChooseParticipantSM().setDisable(false);
			                    	ViewUtils.getCreateVideoSM().setDisable(false);
		                        }
			                });
						}

					});
					MainController.execute(preloadFrames);
					
				});
			}
		}
	}

}


