/**
 * @author Carol Soliman
 * @since July, 2018
 * https://docs.oracle.com/javafx/2/events/processing.htm
 */
package application.controller;


import application.view.ImageToggleButton;
import application.view.VideoPlayer;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.media.MediaPlayer;


public class VideoPlayerController {
	private static ImageToggleButton btnPlayPause = VideoPlayer.getBtnPlayPause();
	private static boolean initiallyPlaying = VideoPlayer.isInitiallyPlaying();
	private static MediaPlayer mp = VideoPlayer.getMediaPlayer();
	//for logging purposes
	private static long accStartTime = 0, decStartTime = 0, accEndTime = 0, decEndTime = 0;

	public static EventHandler<ActionEvent> playPauseHandler = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent e) {
			if(btnPlayPause == null) {
				return;
			}
			if(btnPlayPause.isSelected()) {
				btnPlayPause.setSelectedBG();
			} else {
				btnPlayPause.setUnselectedBG();
			}
			
			if(initiallyPlaying == btnPlayPause.isSelected()){//XNOR
				mp.pause();
			} else if(initiallyPlaying && !btnPlayPause.isSelected() || !initiallyPlaying && btnPlayPause.isSelected()) {//XOR
				mp.play();
			}
//			e.consume(); //avoid bubbling
		}
	};

	public static EventHandler<ActionEvent> stopHandler = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent e) {
			if(btnPlayPause == null) {
				return;
			}
			mp.stop();
			if(initiallyPlaying && !btnPlayPause.isSelected()) {
			/*if initially playing & the button is NOT selected; 
				video was playing when stop was clicked ->
				need to toggle the ImageButton's image
			*/
				btnPlayPause.setSelected(true);
				btnPlayPause.setSelectedBG();
			}
			if(!initiallyPlaying && btnPlayPause.isSelected()) {
				btnPlayPause.setSelected(false);
				btnPlayPause.setUnselectedBG();
			}
			e.consume();
		}
	};

	public static EventHandler<Event> acceleratorPressHandler = new EventHandler<Event>() {
		//this is only called once at the event of Mouse Press
		@Override
		public void handle(Event event) {
			accStartTime = System.currentTimeMillis();
			VideoPlayer.getAccHoldTimer().playFromStart(); //start the timer
		}
	};

	//TODO: handle the case where the user pressed both RIGHT and LEFT Keys 
	//(two mouse presses are not possible but two key presses are)
	//b/c both access setRate ; this is not feasible with the knob
	public static EventHandler<Event> deceleratorPressHandler = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			decStartTime = System.currentTimeMillis();
			VideoPlayer.getDecHoldTimer().playFromStart();
		}
	};
	
	public static EventHandler<Event> acceleratorReleaseHandler = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			accEndTime = System.currentTimeMillis();
			System.out.println("acc press duration: " + (accEndTime - accStartTime) + " ms");
			accEndTime = 0;
			accStartTime = 0;
			
			if(mp != null) {
				mp.setRate(1);
			}
//			VideoPlayer.setAccPressedCycles(0);
			VideoPlayer.setAcc(0);
			VideoPlayer.getAccHoldTimer().stop();
		}
	};

	public static EventHandler<Event> deceleratorReleaseHandler = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			decEndTime = System.currentTimeMillis();
			System.out.println("acc press duration: " + (decEndTime - decStartTime) + " ms");
			decEndTime = 0;
			decStartTime = 0;
			
			if(mp != null) {
				mp.setRate(1);
			}
//			VideoPlayer.setDecPressedCycles(0);
			VideoPlayer.setDec(0);
			VideoPlayer.getDecHoldTimer().stop();
		}
		
	};


	public static void fireAcceleratorPressEvent(){
		Button btnAcc = VideoPlayer.getBtnAccelerator();
		if(btnAcc == null) {
			return; 
		}
		btnAcc.fireEvent(
			new MouseEvent(MouseEvent.MOUSE_PRESSED,
			btnAcc.getLayoutY() , btnAcc.getLayoutY(), 
			btnAcc.getLayoutX(), btnAcc.getLayoutY(), 
			MouseButton.PRIMARY, 1,
            true, true, true, true, true, 
            true, true, true, true, true, 
            null
        ));
	}
	
	public static void fireAcceleratorReleaseEvent(){
		Button btnAcc = VideoPlayer.getBtnAccelerator();
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
	
	public static void fireDeceleratorPressEvent(){
		Button btnDec = VideoPlayer.getBtnDecelerator();
		if(btnDec == null) {
			return; 
		}
		btnDec.fireEvent(
			new MouseEvent(MouseEvent.MOUSE_PRESSED,
			btnDec.getLayoutY() , btnDec.getLayoutY(), 
			btnDec.getLayoutX(), btnDec.getLayoutY(), 
			MouseButton.PRIMARY, 1,
            true, true, true, true, true, 
            true, true, true, true, true, 
            null
        ));
	}

	public static void fireDeceleratorReleaseEvent(){
		Button btnAcc = VideoPlayer.getBtnDecelerator();
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
	
	//INITIALLY IN MAIN CONTROLLER: 
	public static void fireNoOpMouseEvent() {
		TilePane cp = VideoPlayer.getControlPane();
		if(cp == null) {
			return;
		}
	   	System.out.println("NO OP");
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
