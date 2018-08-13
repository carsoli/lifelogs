/**
 * @author Carol Soliman
 * @since July, 2018
 */
package application.view;

import java.util.ArrayList;

import application.controller.FramesGliderController;
import application.controller.MainController;
import application.controller.FramesBufferController;
import application.tasks.PreloadFramesTask;
import application.utils.Constants;
import application.utils.SerialComm;
import javafx.animation.Animation.Status;
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
	private static final int initSliderValue = 0;
	
	
	public static void disableButtonsHBox() {
		if(buttonsHBox == null)
			return;
		buttonsHBox.setDisable(true);
	}
	
	public static Slider getSlider() {
		return slider;
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
		SerialComm.initializeSerialPort();
		totalFrames = ViewUtils.getChosenImages().size(); //All images for the user's chosenDay
		FramesBufferController.setBuffer(framesBuffer); //limited size buffer
		
		isInitiallyPlaying = autoPlay;
		pauseTimer = new PauseTransition(frameRate);
		bufferSize = FramesBufferController.getBuffer().size();

		pauseTimer.setOnFinished(e -> {
			lastLoadedIdx = FramesGliderController.getLastLoadedFrame();//initially set by preloadingTask
			
			if(currBufferPtr == stopPtr) {
			/*
			 * no need to continue displaying, we already displayed the last image in the previous call; 
			 * MUST return after stopping and resetting
			*/
				stopPlayingAndReset();
				return; 
			}
					
			updateFrameHBox();
			
			if(stopPtr == -1) {//still has its initial value; we can still replace
				if( lastLoadedIdx == totalFrames) {
					stopPtr = totalFrames%bufferSize;
				} else {
					ImageView newImgView = ViewUtils.createImageView(
							ViewUtils.getChosenImages().get(FramesGliderController.getLastLoadedFrame()),
							//TODO: try to add the correct height from the beginning
							//instead of updating it in the updateFrameHbox
							0,ViewUtils.getMainScene().getHeight());
//					System.out.println("replacing frame with: " + newImgView);
					FramesBufferController.replaceFrame(currBufferPtr, newImgView);
					FramesGliderController.IncremenetLastLoadedFrame();
				}
			}
			
			currBufferPtr = ((currBufferPtr+1)%bufferSize);
			pauseTimer.playFromStart();
		});

		controlVBox = new VBox();
		controlVBox.setStyle(Constants.BG_TRANSPARENT);
		controlVBox.setMaxHeight(Double.MIN_VALUE);
		controlVBox.setMaxWidth(Double.MIN_VALUE); //smallest node width in it will be the width of it
		
		controlVBox.setAlignment(Pos.TOP_CENTER);
		controlVBox.setPadding(new Insets(0,0,0,0));
		controlVBox.setSpacing(0);
		buttonsHBox = initializeControlButtons();
		sliderPane = initializeSlider();
		controlVBox.getChildren().addAll(sliderPane, buttonsHBox);
		
		if(framesGliderSP.getChildren().size() == 2) {
			framesGliderSP.getChildren().remove(1); //remove controlVBox(slider/buttons)
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

		slider.setMin(0);
		slider.setMax(totalFrames); 
		slider.setValue(initSliderValue); //initially; later update with every lli increment
		slider.setShowTickLabels(true);//numbers
		slider.setShowTickMarks(true);//the vertical dashes
		slider.setMinorTickCount(1);
		initializeSliderListeners();
		
		sliderPane.getChildren().add(slider);
		return sliderPane;
	}
	
	public static void initializeSliderListeners() {
		//ORDER MATTERS
		slider.setOnMousePressed(FramesGliderController.seekPressHandler);
		slider.setOnMouseReleased(FramesGliderController.seekReleaseHandler);
		
		slider.valueProperty().addListener((obs, oldValue, newValue) -> {
			//called whenever the value changes (on mouse press)
			//called programmatically when the slider value is updated by setCurrSliderValue; ex:updateHFrame

//			System.out.println("OLD VALUE:  " + oldValue + ", NEW VALUE: " + newValue);
			int oldValueAsInt = oldValue.intValue();
			int newValueAsInt = newValue.intValue();

			FramesGliderController.setLastDisplayedIndexBeforePressing(oldValueAsInt);
			if(pauseTimer.getStatus() == Status.PAUSED) {
				//gets here if the user DRAGS (presses and starts changing value without releasing)
				//if paused: 1)user pressed the mouse on slider/ 2)the video is actually Paused
				//3) Both together
				if(FramesGliderController.getLastDisplayedIndexBeforeDragging() == -1 ) {
					if(oldValueAsInt == totalFrames) {
						System.out.println("reseting value before Dragging; oldValueAsInt == totalFrames");

						stopPlayingAndReset();
						//this is done so if the video ends(displays last frame), and before calling
						//stopPlayingAndReset, the user starts dragging the glider, the last index would be zero
						//and any dragging will be as if he is seeking forward
						//NEVER DO THIS OUTSIDE OF THIS CONDITION (BEFORE PRESSING); it's handled by basecase1
						FramesGliderController.setLastDisplayedIndexBeforeDragging(0);
						return; 
					} else {
						FramesGliderController.setLastDisplayedIndexBeforeDragging(oldValueAsInt);
					}
				}
				updateFrameHBox(newValueAsInt); //dummy display (no actual change of buffer pointers)
			} 

		});
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
		bufferSize = FramesBufferController.getBuffer().size();

		setCurrBuffPtr(0); 
		setCurrSliderValue(initSliderValue);//reset slider value
		setStopPtr(-1);

		if(totalFrames > bufferSize) {
			FramesGlider.getSlider().setDisable(true); 
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
					FramesGlider.buttonsHBox.getChildren().set(btnPPIdx, playPauseB);
					enableButtonsHBox();	
					FramesGlider.getSlider().setDisable(false); 
				}
			});
			MainController.execute(preloadFrames);
		} else {
			//no need to load
			FramesGlider.isInitiallyPlaying = false; 
			//===RE-INITIALIZE PLAYPAUSEBUTTON 
			ImageToggleButton playPauseB= new ImageToggleButton(Constants.PLAY_IMG, Constants.PAUSE_IMG);
			playPauseB.setSelected(false);
			playPauseB.addEventHandler(ActionEvent.ACTION, FramesGliderController.playPauseHandler);
			int btnPPIdx = FramesGlider.buttonsHBox.getChildren().indexOf(FramesGlider.btnPlayPause);
			FramesGlider.btnPlayPause = playPauseB;
			FramesGlider.buttonsHBox.getChildren().set(btnPPIdx, playPauseB);
			enableButtonsHBox();
			FramesGlider.getSlider().setDisable(false); 
		}
		
		//add an intermediate black screen
		displayBlackFrame();
	}
	
	private static void displayBlackFrame() {
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

	public static void setPauseTimer(PauseTransition pauseTimer) {
		FramesGlider.pauseTimer = pauseTimer;
	}

	public static void updateFrameHBox(int frameIdx) {
	//this signature's only used when we just display the frame the user is attempting to seek
		if(frameIdx == totalFrames) {
			displayBlackFrame();
			return;
		}
		frameHBox.getChildren().remove(0);
		ImageView iv = ViewUtils.createImageView(
				ViewUtils.getChosenImages().get(frameIdx),
				0,framesGliderSP.getHeight());
//		iv.fitHeightProperty().bind(framesGliderSP.heightProperty());
		frameHBox.getChildren().add(iv);
		
	}
	
	public static void updateFrameHBox() {
		frameHBox.getChildren().remove(0);
		ImageView iv = FramesBufferController.getBuffer().get(currBufferPtr);
		iv.fitHeightProperty().bind(framesGliderSP.heightProperty());
		frameHBox.getChildren().add(iv);
		//display frame 0; update sliderPtr to 1. display frame 1; update it to 2;
		//current slider value is always pointing to the number of the frame that will
		//be displayed in the next call of this fn
		setCurrSliderValue(new Double(FramesGlider.getCurrSliderValue() ).intValue()+1); 
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
	
	public static int getStopPtr() {
		return FramesGlider.stopPtr;
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

	public static double getCurrSliderValue() {
		return slider.getValue();
	}

	public static void setCurrSliderValue(double currSliderValue) {
	//the slider value always represents the frame to be displayed next
		slider.setValue(currSliderValue);
	}


}
