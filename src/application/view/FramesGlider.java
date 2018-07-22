package application.view;

import java.util.ArrayList;

import application.controller.FramesGliderController;
import application.controller.MainController;
import application.controller.FramesBufferController;
import application.tasks.PreloadFramesTask;
import application.utils.Constants;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class FramesGlider {
	//TODO: must be atomic
	private static Duration frameRate = FramesGliderController.getInitFrameRate();
//	private static ArrayList<ImageView> frames = null;
	private static int currBufferPtr = 0;
	private static int stopPtr = -1; 
	private static int totalFrames = 0;
	private static PauseTransition pauseTimer = null;
	private static boolean isInitiallyPlaying = false;
	private static ImageButton btnDecelerator, btnStop, btnAccelerator;
	private static ImageToggleButton btnPlayPause;
	private static HBox frameHBox = null;
	private static Tab framesGliderTab = null;
	private static StackPane framesGliderSP = null; //instead of ScrollPane
	private static VBox framesGliderVBox = null, controlVBox = null;
//	private static VBox framesGliderInnerVB = null; 
	private static TilePane controlPane = null;
	private static Slider slider = null;
	
	private static int lastLoadedIdx = -1;
	private static int bufferSize = 0;
	
	
	public static void disableControlPane() {
		if(controlPane == null)
			return;
		controlPane.setDisable(true);
	}
	
	public static void enableControlPane() {
		if(controlPane == null) 
			return;
		controlPane.setDisable(false);
	}
	
	public static TilePane getControlPane() {
		return FramesGlider.controlPane;
	}
	
	//called in viewUtils when initializing Scene
	public static Tab initializeFrameGliderTab() {
		framesGliderTab = new TabPaneItem(2, Constants.TAB2_TITLE, false);
		//main container in the tab
		framesGliderVBox = (VBox)(framesGliderTab.getContent());
		framesGliderVBox.setStyle(Constants.BG_BLUE);
        
        //2nd main container
        //-----------------
//        ScrollPane framesGliderSP = new ScrollPane();
//        framesGliderSP.setStyle(Constants.BG_BLACK);
//        framesGliderSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); 
//        framesGliderSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); 
//        framesGliderSP.setFitToWidth(true);
  
        //inner-most container: VBox
//    	framesGliderInnerVB = new VBox();
//    	framesGliderInnerVB.setPadding(new Insets(5, 0, 5, 0));
//    	framesGliderInnerVB.setStyle(Constants.BG_BLACK);

        //=============
        //instead of the scroll pane that contains innerVB; we add stack pane, that directly
        //has the frameHBox, and controlVBox
        framesGliderSP = new StackPane();
        framesGliderSP.setAlignment(Pos.BOTTOM_CENTER); //why isn't this applied
        framesGliderSP.prefHeightProperty().bind(framesGliderVBox.heightProperty());
//        framesGliderSP.prefWidthProperty().bind(framesGliderVBox.widthProperty());
        
        //--------------------
        
        //framesContainer
		frameHBox = new HBox();
        frameHBox.getChildren().add(new ImageView()); //initially add an empty image view
        frameHBox.setAlignment(Pos.CENTER); //applied on the child node of frameHBox
        //------------------------
//		framesGliderSP.setContent(framesGliderInnerVB);
//      framesGliderVBox.getChildren().add(framesGliderSP);
//      framesGliderInnerVB.getChildren().add(frameHBox);
        //===================
        framesGliderVBox.getChildren().add(framesGliderSP);
        framesGliderSP.getChildren().add(frameHBox);//initially we only add the frameHBox
        //---------------

        return framesGliderTab;
	}
	
	public static void initializeFrameGlider(ArrayList<ImageView> framesBuffer, boolean autoPlay, boolean isStopped) {
		//total user images' files for the chosenDay
		totalFrames = ViewUtils.getChosenImages().size(); 
		//limited size buffer
		FramesBufferController.setBuffer(framesBuffer);
		
		isInitiallyPlaying = autoPlay;
		pauseTimer = new PauseTransition(frameRate);
		bufferSize = FramesBufferController.getBuffer().size();

		
		pauseTimer.setOnFinished(e -> {
			lastLoadedIdx = FramesGliderController.getLastLoadedFrame();//initially set by bufferTask
			
			if(currBufferPtr == stopPtr) {
			//no need to continue displaying, we already displayed the last image in the previous call; return in the end
				//if you need to replace stopPlayingAndReset() w/ code inside it, don't forget to leave the return statement
				stopPlayingAndReset();
				return; 
			}
					
			updateFrameHBox();
	
			if(stopPtr == -1) {//still has its initial value; we can still replace
				if( lastLoadedIdx == totalFrames) {
					stopPtr = currBufferPtr;
				} else {
					//================ delete only when the loadFrameTask 100% works
					ImageView newImgView = ViewUtils.createImageView(
							ViewUtils.getChosenImages().get(FramesGliderController.getLastLoadedFrame()),
							0,ViewUtils.getMainScene().getHeight());
					FramesBufferController.replaceFrame(currBufferPtr, newImgView);
					//======================
//					loadFrameTask lft = new loadFrameTask(
//							FramesGliderController.getLastLoadedFrame(), 
//							currBufferPtr);
//					FramesBufferController.setCanDisplay(currBufferPtr, false); 
//					lft.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//						@Override
//						public void handle(WorkerStateEvent event) {
//							//the task stores the indices of where to load from(LLI at the time of instantiation) 
//							//and where to load to (currBufferPtr at time of instantiation)
//							FramesBufferController.setCanDisplay(lft.getIndexToLoadTo(), true);
//							FramesBufferController.replaceFrame(lft.getIndexToLoadTo(), lft.getValue());
//						}
//					});
//					MainController.addTask(lft);
//					MainController.execute(lft);
					//====================
					
//					System.out.println("replacing: " +
//							ViewUtils.getChosenImages().get(FramesGliderController.getLastLoadedFrame()).getName());
					FramesGliderController.IncremenetLastLoadedFrame();
				}
			}
			
			currBufferPtr = ((currBufferPtr+1)%bufferSize);
			pauseTimer.playFromStart();
		});
		//ADDED---
		controlVBox = new VBox();
		controlVBox.setStyle(Constants.BG_RED);
		controlVBox.setMaxHeight(Double.MIN_VALUE);
		controlVBox.setAlignment(Pos.TOP_CENTER);
//		controlVBox.setPadding(new Insets(0,0,0,0));
		controlVBox.setSpacing(0);
		//----
		controlPane = initializeGliderControls();
		
		//ADDED-----
		slider = initializeSeekBar();
		controlVBox.getChildren().addAll(slider, controlPane);
		//-------------------------
		
		//---------------------------
//		if(framesGliderInnerVB.getChildren().size() == 2) {
//			//re-initialize controlPane even if it exists b/c playPauseButton
//			//and its related flags change when the video is stopped/ends
//			//remove the old one then add it 
//			//the size is always be AT LEAST 1, b/c frameHBox
//			//is added in viewUtils' initializeScene()
//			framesGliderInnerVB.getChildren().remove(1);
//		} 
//		framesGliderInnerVB.getChildren().add(controlPane);
		//========================= instead
		if(framesGliderSP.getChildren().size() == 2) {
			framesGliderSP.getChildren().remove(1); 
		}
//		framesGliderSP.getChildren().add(controlPane);
		controlVBox.setPrefHeight(Double.MIN_VALUE);
		framesGliderSP.getChildren().add(controlVBox);
		//-----------------------------------
		
		if(isInitiallyPlaying && !isStopped)
			pauseTimer.playFromStart(); //first call
	}
	
	public static void updatePauseTime(Duration newPauseTime) {
		frameRate = newPauseTime;
	}
	
	public static Slider initializeSeekBar() {
		Slider slider = new Slider(0, 100,50);
		return slider;
	}
	
	public static TilePane initializeGliderControls() {
		TilePane controlPane = new TilePane();
		controlPane.setOrientation(Orientation.HORIZONTAL);
		controlPane.setAlignment(Pos.CENTER);
		controlPane.setStyle(Constants.BG_GREEN);//**
		controlPane.setHgap(10);
		controlPane.setMaxHeight(Double.MIN_VALUE); //(smallest height of nodes within)
		//=========
		if(isInitiallyPlaying) {//then the button should be pause if unselected, and play if selected
			btnPlayPause = new ImageToggleButton(Constants.PAUSE_IMG, Constants.PLAY_IMG);
		} else {//then the button should be play when unselected
			btnPlayPause = new ImageToggleButton(Constants.PLAY_IMG, Constants.PAUSE_IMG);
		}
		btnPlayPause.addEventHandler(ActionEvent.ACTION, FramesGliderController.playPauseHandler);
		//=========
		btnAccelerator = new ImageButton(Constants.FF_IMG);
		btnAccelerator.setOnMouseReleased(FramesGliderController.acceleratorReleaseHandler);
		btnAccelerator.setOnTouchReleased(FramesGliderController.acceleratorReleaseHandler);
		//=========
		btnStop = new ImageButton(Constants.STOP_IMG);
		btnStop.addEventHandler(ActionEvent.ANY, FramesGliderController.stopHandler);
		//=========
		btnDecelerator = new ImageButton(Constants.FB_IMG);
		btnDecelerator.setOnMouseReleased(FramesGliderController.deceleratorReleaseHandler);
		btnDecelerator.setOnTouchReleased(FramesGliderController.deceleratorReleaseHandler);

		controlPane.setPadding(new Insets(10, 5, 10, 5));
		controlPane.getChildren().addAll(
				btnDecelerator, 
				btnPlayPause, btnStop,
				btnAccelerator); 
		return controlPane;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void stopPlayingAndReset() {
		pauseTimer.stop();
		pauseTimer.setDuration(FramesGliderController.getInitFrameRate());
		FramesGliderController.setMinRateReached(false);
		FramesGliderController.setMaxRateReached(false);
		
		setCurrBuffPtr(0); 
		setStopPtr(-1);

		if(totalFrames > bufferSize) {
			disableControlPane();
			//YOU only want to reset the last loaded frame, if you plan to load from the beginning
			FramesGliderController.setLastLoadedFrame(0); 
			Task preloadFrames = new PreloadFramesTask();
			MainController.addTask(preloadFrames);
			preloadFrames.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					FramesGlider.isInitiallyPlaying = false; 
					//===RE-INITIALIZE PLAYPAUSEBUTTON 
					ImageToggleButton playPauseB= new ImageToggleButton(Constants.PLAY_IMG, Constants.PAUSE_IMG);
					playPauseB.setSelected(false);
					playPauseB.addEventHandler(ActionEvent.ACTION, FramesGliderController.playPauseHandler);
					int btnPPIdx = FramesGlider.controlPane.getChildren().indexOf(FramesGlider.btnPlayPause);
					FramesGlider.btnPlayPause = playPauseB;
					FramesGlider.controlPane.getChildren().set(
							btnPPIdx, playPauseB);
					enableControlPane();	
				}
			});
			MainController.execute(preloadFrames);
		} else {
			//no need to preload
			disableControlPane();
			FramesGlider.isInitiallyPlaying = false; 
			//===RE-INITIALIZE PLAYPAUSEBUTTON 
			ImageToggleButton playPauseB= new ImageToggleButton(Constants.PLAY_IMG, Constants.PAUSE_IMG);
			playPauseB.setSelected(false);
			playPauseB.addEventHandler(ActionEvent.ACTION, FramesGliderController.playPauseHandler);
			int btnPPIdx = FramesGlider.controlPane.getChildren().indexOf(FramesGlider.btnPlayPause);
			FramesGlider.btnPlayPause = playPauseB;
			FramesGlider.controlPane.getChildren().set(btnPPIdx, playPauseB);
			enableControlPane();
		}
		
		//add an intermediate black screen
		final Image stopImage = FramesGliderController.getBlackScreenImage();
		final ImageView stopImgView = new ImageView(stopImage);
		frameHBox.getChildren().remove(0);
		frameHBox.getChildren().add(stopImgView);

		
	}

	public static ImageButton getBtnDecelerator() {
		return btnDecelerator;
	}

	public static ImageButton getBtnStop() {
		return btnStop;
	}

	public static ImageButton getBtnAccelerator() {
		return btnAccelerator;
	}

	public static ImageToggleButton getBtnPlayPause() {
		return FramesGlider.btnPlayPause;
	}
	
	public static boolean isInitiallyPlaying() {
		return isInitiallyPlaying;
	}
	
	public static PauseTransition getPauseTimer() {
		return pauseTimer;
	}

	public static void setPauseTimer(PauseTransition pauseTimer) {
		FramesGlider.pauseTimer = pauseTimer;
	}

	
	public static void updateFrameHBox() {
		frameHBox.getChildren().remove(0);
		frameHBox.getChildren().add(FramesBufferController.getBuffer().get(currBufferPtr));
	}
	
	public static HBox getFrameHBox() {
		return FramesGlider.frameHBox;
	}

	public static void setCurrBuffPtr(int currBufferPtr) {
		FramesGlider.currBufferPtr = currBufferPtr;
	}
	
	public static int getCurrBufferPtr() {
		return currBufferPtr;
	}

	public static void setStopPtr(int stopPtr) {
		FramesGlider.stopPtr = stopPtr;
	}
	
	public static int getTotalFrames() {
		return FramesGlider.totalFrames;
	}
	
	public static int getBufferSize() {
		return FramesGlider.bufferSize;
	}
	
	public static void stopPauseTimer() {
		FramesGlider.pauseTimer.stop();
	}
	

	public static void playPauseTimer() {
		FramesGlider.pauseTimer.play();
	}
	
	public static void playTimerFromStart(){
		FramesGlider.pauseTimer.playFromStart();
	}
	
	public static void pausePauseTimer() {
		FramesGlider.pauseTimer.pause();
	}
	
	public static void setRate(Duration newRate) {
		pauseTimer.setDuration(newRate);
	}
	
	public static Duration getRate() {
		return pauseTimer.getDuration();
	}


}
