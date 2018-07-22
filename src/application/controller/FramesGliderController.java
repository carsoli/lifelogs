package application.controller;

import application.utils.Constants;
import application.view.FramesGlider;
import application.view.ImageToggleButton;
import application.view.ViewUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.util.Duration;

public class FramesGliderController {
	private static ImageToggleButton btnPlayPause = null;
	private static boolean initiallyPlaying = false;
	
	private static Duration initFrameRate = Constants.GLIDER_INIT_RATE;
	//24 switch positions to get to a frame rate = 0.0 ms
	private static Duration accStep = Constants.GLIDER_ACC_STEP;
	//could be the same as accStep(or different)
	private static Duration decStep = Constants.GLIDER_DEC_STEP;
	private static Duration slowestRate = Constants.GLIDER_SLOWEST_RATE; 
	private static Duration fastestRate = Duration.millis(0.0);
	//fastest theoretically-possible rate(0.0 ms pause); further acceleration is ineffective 
	private static boolean minRateReached = false;
	//slowest desired rate; further deceleration is ineffective 
	private static boolean maxRateReached = false;
	
	private static int lastLoadedFrame = 0;
	
	public static EventHandler<ActionEvent> playPauseHandler = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent e) {
			btnPlayPause = FramesGlider.getBtnPlayPause();
			initiallyPlaying = FramesGlider.isInitiallyPlaying();
			if(btnPlayPause == null) {
				System.out.println("playPauseHandler:: Null PlayPauseButton");
				return;
			}
			
			if(btnPlayPause.isSelected()) {
				btnPlayPause.setSelectedBG();
			} else {
				btnPlayPause.setUnselectedBG();
			}
			
			if(initiallyPlaying == btnPlayPause.isSelected()){//XNOR
				FramesGlider.pausePauseTimer();
			} else if(initiallyPlaying && !btnPlayPause.isSelected() || !initiallyPlaying && btnPlayPause.isSelected()) {//XOR
				FramesGlider.playPauseTimer();
			}
			e.consume(); //avoid bubbling
		}
	};
	
	public static EventHandler<ActionEvent> stopHandler = new EventHandler<ActionEvent>(){
		public void handle(ActionEvent e) {
			btnPlayPause = FramesGlider.getBtnPlayPause();
			initiallyPlaying = FramesGlider.isInitiallyPlaying();
			
			if(btnPlayPause == null) {
				System.out.println("stopHandler:: Null PlayPauseButton");
				return;
			}
			FramesGlider.stopPlayingAndReset();
			e.consume();
		}
	};

	//NOT A PRESS AND HOLD CASE; just a fire event when the button is pressed (equivalent to knob rot)
	public static EventHandler<Event> acceleratorReleaseHandler = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			//takes effect in the next cycle 
			if(minRateReached) 
				return;
//				System.out.println("Min Rate Reached");
			Duration currRate = FramesGlider.getRate();
			Duration newRate = currRate.subtract(accStep); //less time
			if(newRate.compareTo(fastestRate) >0) {//newRate>0 
				FramesGlider.setRate(newRate);
			} else {
				minRateReached = true;
			}
			event.consume();
		}
	};

	public static EventHandler<Event> deceleratorReleaseHandler = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			//slowestRate already reached
			if(maxRateReached) 
				return;
			Duration currRate = FramesGlider.getRate();
			Duration newRate = currRate.add(decStep); //increase sleepTime of pauseTransition
			if(newRate.compareTo(slowestRate) < 0){ //newRate < slowestRate 
				FramesGlider.setRate(newRate);
			} else {
				maxRateReached = true;
			}
			event.consume();
		}
		
	};
	
	public static void fireAcceleratorReleaseEvent(){
		Button btnAcc = FramesGlider.getBtnAccelerator();
		if(btnAcc == null) {
			return;
		}

		btnAcc.fireEvent(
			new MouseEvent(MouseEvent.MOUSE_RELEASED,
			btnAcc.getLayoutY() , btnAcc.getLayoutY(), 
			btnAcc.getLayoutX(), btnAcc.getLayoutY(), 
			MouseButton.PRIMARY, 1,
            true, true, true, true, true, 
            true, true, true, true, true, 
            null
        ));
	}
	
	public static void fireDeceleratorReleaseEvent(){
		Button btnAcc = FramesGlider.getBtnDecelerator();
		if(btnAcc == null) {
			return;
		}
		btnAcc.fireEvent(
			new MouseEvent(MouseEvent.MOUSE_RELEASED,
			btnAcc.getLayoutY() , btnAcc.getLayoutY(), 
			btnAcc.getLayoutX(), btnAcc.getLayoutY(), 
			MouseButton.PRIMARY, 1,
            true, true, true, true, true, 
            true, true, true, true, true, 
            null
        ));
	}
	
	public static int getLastLoadedFrame() {
		return lastLoadedFrame;
	}
	
	public static void setLastLoadedFrame(int idx) {
		FramesGliderController.lastLoadedFrame = idx;
	}
	
	public static void decrementLastLoadedFrame() {
		FramesGliderController.lastLoadedFrame--;
	}

	public static void IncremenetLastLoadedFrame() {
		FramesGliderController.lastLoadedFrame++;
	}
	
	public static Image getBlackScreenImage() {
		return new Image(Constants.ASSETS_URL.toString() + Constants.BLACK_SCREEN, 
				ViewUtils.getMainScene().getWidth(),
				ViewUtils.getMainScene().getHeight(),
				false,
				true);
	}

	public static void setMinRateReached(boolean minRateReached) {
		FramesGliderController.minRateReached = minRateReached;
	}
	
	public static void setMaxRateReached(boolean maxRateReached) {
		FramesGliderController.maxRateReached = maxRateReached;
	}
	
	public static Duration getInitFrameRate() {
		return FramesGliderController.initFrameRate;
	}
	
	public static void fireNoOpMouseEvent(){
		TilePane cp = FramesGlider.getControlPane();
		if(cp == null) {
			return;
		}
		cp.fireEvent(
			new MouseEvent(MouseEvent.MOUSE_PRESSED,
			cp.getLayoutY() , cp.getLayoutY(), 
			cp.getLayoutX(), cp.getLayoutY(), 
			MouseButton.PRIMARY, 1,
            true, true, true, true, true, 
            true, true, true, true, true, 
            null
        ));


	}
}

