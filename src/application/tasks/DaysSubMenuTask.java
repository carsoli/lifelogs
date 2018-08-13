package application.tasks;

import java.util.ArrayList;

import application.controller.FramesGliderController;
import application.controller.MainController;
import application.structs.Day;
import application.controller.FramesBufferController;
import application.structs.Participant;
import application.utils.Constants;
import application.utils.ImagesToVideoConverter;
import application.utils.SerialComm;
import application.view.FramesGlider;
import application.view.VideoPlayer;
import application.view.ViewUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

@SuppressWarnings("rawtypes")
public class DaysSubMenuTask extends Task{
	private ArrayList<Participant> pArrayList = new ArrayList<Participant>();

	public DaysSubMenuTask(ArrayList<Participant> pArrayList) {
		this.pArrayList = pArrayList;
	}
	
	@Override
	protected Object call() throws Exception {
		initializeDaysSubMenu(
				this.pArrayList);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static void initializeDaysSubMenu(
			ArrayList<Participant> pArrayList) {
		
		Menu createVideosSM = ViewUtils.getCreateVideoSM();
		
		//create a days sub menu based on the choice of participant
		createVideosSM.getItems().clear(); //reset in case it was already filled
		int chosenP = MainController.getChosenP(); 
		if(chosenP > -1 && pArrayList != null) {
			ArrayList<Day> dArrayList = (pArrayList.get(chosenP)).getDays();
			for (Day d: dArrayList) {
				MenuItem dItem = new MenuItem(d.getName());
				int dayIdx = dArrayList.indexOf(d);
				dItem.setId("" + dayIdx);
//				dItem.setDisable(true);//NO; disable when loading is taking place, in setOnAction
				//thus allowing to start video creation
				createVideosSM.getItems().add(dItem);
				dItem.setOnAction(e -> {
					//close Arduino port; reopened during framesGliderInitialization call
					SerialComm.disconnectArduino();
					//until the curr vid is created, don't change the chosenP bc it's a global variable
					ViewUtils.getChooseParticipantSM().setDisable(true); 
					//disable until the chosen day's video is created
					ViewUtils.getCreateVideoSM().setDisable(true);
					MainController.setChosenD(dayIdx); 
					
					//============COMMENT WHEN YOU DON'T NEED THE VIDEOS TAB
					Task createVideoTask = new CreateVideoTask(dayIdx);
					MainController.addTask(createVideoTask);
					createVideoTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
			                Platform.runLater(new Runnable() {
		                        @Override
		                        public void run() {
			                    	ImagesToVideoConverter.EncodeVideoAndClose();
			                    	ViewUtils.getChooseParticipantSM().setDisable(false);
			                    	ViewUtils.getCreateVideoSM().setDisable(false);
			                    	VideoPlayer.postProcessVideo();
		                        }
			                });
						}
					});
					MainController.execute(createVideoTask);
					//=================================
					//whenever we choose a different day, we need to reset LLI
					//this way the preloadFramesTask can load things correctly
					FramesGliderController.setLastLoadedFrame(0);

					//and curr buff ptr because we decide to load while one video is still playing,
					//curr buff ptr would be out of bound
					//TODO: this should actually move to onSucceeded, not reset before loading while video is playing
					FramesGlider.setCurrBuffPtr(0); 
					
					long startTime = System.nanoTime();
					Task preloadFrames = new PreloadFramesTask();
					MainController.addTask(preloadFrames);
					preloadFrames.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
			                Platform.runLater(new Runnable() {
		                        @Override
		                        public void run() {
		                        	FramesGlider.initializeFrameGlider(
		                        			FramesBufferController.getBuffer(), Constants.GLIDER_AUTOPLAY, 
		                        			false);
			                    	ViewUtils.getChooseParticipantSM().setDisable(false);
			                    	ViewUtils.getCreateVideoSM().setDisable(false);
			    					long endTime   = System.nanoTime();
			    					long totalTime = endTime - startTime;
			    					System.out.println("duration of preloading task execution: " + totalTime);
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