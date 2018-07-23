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
	private static int currBufferPtr = 0;
	private static int stopPtr = -1; 
	private static int totalFrames = 0;
	private static PauseTransition pauseTimer = null;
	private static boolean isInitiallyPlaying = false;
	private static ImageButton btnDecelerator, btnStop, btnAccelerator;
	private static ImageToggleButton btnPlayPause;
	private static HBox frameHBox = null;
	private static Tab framesGliderTab = null;
	private static StackPane framesGliderSP = null; 
	private static VBox framesGliderVBox = null, controlVBox = null;
	private static HBox buttonsHBox = null;
	private static TilePane sliderPane = null;
	private static Slider slider = null;
	
	private static int lastLoadedIdx = -1;
	private static int bufferSize = 0;
	
	
	public static void disableButtonsHBox() {
		if(buttonsHBox == null)
			return;
		buttonsHBox.setDisable(true);
	}
	
	public static void enableButtonsHBox() {
		if(buttonsHBox == null) 
			return;
		buttonsHBox.setDisable(false);
	}
	
	public static HBox getButtonsHBox() {
		return FramesGlider.buttonsHBox;
	}
	
	//called in viewUtils when initializing Scene
	public static Tab initializeFrameGliderTab() {
		framesGliderTab = new TabPaneItem(2, Constants.TAB2_TITLE, false);

		framesGliderVBox = (VBox)(framesGliderTab.getContent());
		framesGliderVBox.setStyle(Constants.BG_BLACK);
        
        framesGliderSP = new StackPane();
        framesGliderSP.setAlignment(Pos.BOTTOM_CENTER);
        framesGliderVBox.getChildren().add(framesGliderSP);
        framesGliderSP.prefHeightProperty().bind(framesGliderVBox.heightProperty());

        frameHBox = new HBox();
        frameHBox.getChildren().add(new ImageView()); //initially add an empty image view
        frameHBox.setAlignment(Pos.CENTER); //applied on the child node of frameHBox

        framesGliderSP.getChildren().add(frameHBox);//initially we only add the frameHBox
        frameHBox.maxHeightProperty().bind(framesGliderSP.heightProperty()); //this is done with every update of frame

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
			/*
			 * no need to continue displaying, we already displayed the last image in the previous call; 
			 * MUST Return after stopping and resetting
			*/
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

		controlVBox = new VBox();
		controlVBox.setStyle(Constants.BG_TRANSPARENT);
		controlVBox.setMaxHeight(Double.MIN_VALUE);
//		controlVBox.setPrefHeight(Double.MIN_VALUE); //we set the maxHeight anyways
		controlVBox.setMaxWidth(Double.MIN_VALUE); //smallest node width in it will be the width of it
		
		controlVBox.setAlignment(Pos.TOP_CENTER);
		controlVBox.setPadding(new Insets(0,0,0,0));
		controlVBox.setSpacing(0);
		buttonsHBox = initializeControlButtons();
		sliderPane = initializeSlider();
		controlVBox.getChildren().addAll(sliderPane, buttonsHBox);
		
		if(framesGliderSP.getChildren().size() == 2) {
			framesGliderSP.getChildren().remove(1); 
		}
		framesGliderSP.getChildren().add(controlVBox);
		
		if(isInitiallyPlaying && !isStopped)
			pauseTimer.playFromStart(); //first call
	}
	
	public static void updatePauseTime(Duration newPauseTime) {
		frameRate = newPauseTime;
	}
	
	public static TilePane initializeSlider() {
		sliderPane = new TilePane();
		sliderPane.setStyle(Constants.BG_TRANSPARENT);
		sliderPane.setAlignment(Pos.CENTER);
		sliderPane.setPadding(new Insets(5, 0, 5, 0));
		sliderPane.maxWidthProperty().bind(controlVBox.widthProperty());;
		
		slider = new Slider();
		slider.prefWidthProperty().bind(buttonsHBox.widthProperty());
		//** DO THE MAGIC HERE
		slider.setMin(0);
		slider.setMax(totalFrames);
		slider.setValue(0); //initially; later update with event listeners
//		slider.setShowTickLabels(showTickLabels);//numbers
//		slider.setShowTickMarks(showTickMarks);//the vertical dashes
//		slider.setMajorTickUnit(majorTickUnit);
//		slider.setMinorTickCount(minorTickUnit);
////		//setBlockIncrement(x) defines the distance that 
////		//the thumb moves when a user clicks on the track 
//		slider.setBlockIncrement(blockIncrement); //each click can represent {blockIncrement} frames to skip
		
		sliderPane.getChildren().add(slider);
		return sliderPane;
	}
	
	public static HBox initializeControlButtons() {
		HBox buttonsPane = new HBox();
		buttonsPane.setAlignment(Pos.CENTER);
		buttonsPane.setStyle(Constants.BG_TRANSPARENT);
		buttonsPane.setSpacing(10.0);
		buttonsPane.setMaxHeight(Double.MIN_VALUE); //(smallest height of nodes within)
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
		
		
		buttonsPane.setPadding(new Insets(10, 5, 10, 5));
		buttonsPane.getChildren().addAll(
				btnDecelerator, 
				btnPlayPause, btnStop,
				btnAccelerator); 
		return buttonsPane;
		
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
			disableButtonsHBox();
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
					int btnPPIdx = FramesGlider.buttonsHBox.getChildren().indexOf(FramesGlider.btnPlayPause);
					FramesGlider.btnPlayPause = playPauseB;
					FramesGlider.buttonsHBox.getChildren().set(
							btnPPIdx, playPauseB);
					enableButtonsHBox();	
				}
			});
			MainController.execute(preloadFrames);
		} else {
			//no need to preload
			disableButtonsHBox();
			FramesGlider.isInitiallyPlaying = false; 
			//===RE-INITIALIZE PLAYPAUSEBUTTON 
			ImageToggleButton playPauseB= new ImageToggleButton(Constants.PLAY_IMG, Constants.PAUSE_IMG);
			playPauseB.setSelected(false);
			playPauseB.addEventHandler(ActionEvent.ACTION, FramesGliderController.playPauseHandler);
			int btnPPIdx = FramesGlider.buttonsHBox.getChildren().indexOf(FramesGlider.btnPlayPause);
			FramesGlider.btnPlayPause = playPauseB;
			FramesGlider.buttonsHBox.getChildren().set(btnPPIdx, playPauseB);
			enableButtonsHBox();
		}
		
		//add an intermediate black screen
		final Image stopImage = FramesGliderController.getBlackScreenImage();
		final ImageView stopImgView = new ImageView(stopImage);
		stopImgView.fitHeightProperty().bind(framesGliderSP.heightProperty());
		
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
		ImageView iv = FramesBufferController.getBuffer().get(currBufferPtr);
		iv.fitHeightProperty().bind(framesGliderSP.heightProperty());
		frameHBox.getChildren().add(iv);
	}
	
	public static StackPane getFramesGliderSP() {
		return framesGliderSP;
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
