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
import javafx.scene.media.MediaPlayer;


public class VideoPlayerController {
	private static ImageToggleButton btnPlayPause = VideoPlayer.getBtnPlayPause();
	private static boolean initiallyPlaying = VideoPlayer.isInitiallyPlaying();
	private static MediaPlayer mp = VideoPlayer.getMediaPlayer();
	
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
			/*if initially playing & the btn is NOT selected; 
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
//			acc = VideoPlayer.getAccPressedCycles()*accStep;
//			System.out.println("acceleration: "+ acc);
//			if(acc < maxAcc) {
//				mp.setRate(initPlayRate + acc);
//			}
			VideoPlayer.getAccHoldTimer().playFromStart(); //start the timer
		}
	};
	//TODO: handle the case where the user pressed both RIGHT and LEFT Keys 
	//(two mouse presses are not possible but two key presses are)
	public static EventHandler<Event> deceleratorPressHandler = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
//			if(dec>minDec) {
//				dec = VideoPlayer.getDecPressedCycles()*decStep;
//				System.out.println("rate:" + mp.getRate());
//				mp.setRate(initPlayRate - dec);
//			}
			VideoPlayer.getDecHoldTimer().playFromStart();
		}
	};
	
	public static EventHandler<Event> acceleratorReleaseHandler = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			System.out.println("Acc Press Duration:" + VideoPlayer.getAccPressedCycles()*1000 + " ms");
//			mp.setRate(VideoPlayer.getInitPlayRate());
			if(mp != null) {
				mp.setRate(1);
			}
			VideoPlayer.setAccPressedCycles(0);
			VideoPlayer.getAccHoldTimer().stop();
		}
	};
	//TODO:
	public static EventHandler<Event> deceleratorReleaseHandler = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			// TODO Auto-generated method stub
			//100 = pauseTime
			System.out.println("Dec Press Duration: " + VideoPlayer.getDecPressedCycles()*100 + " ms");
//			mp.setRate(initPlayRate);
			mp.setRate(1);
			VideoPlayer.setDecPressedCycles(0);
//			dec = 0;
			VideoPlayer.getDecHoldTimer().stop();
		}
		
	};


	public static void fireAcceleratorPressEvent(){
		Button ff = VideoPlayer.getBtnAccelerator();
		if(ff == null) {
			return; 
		}
//		System.out.println("firing acc press");
		ff.fireEvent(
			new MouseEvent(MouseEvent.MOUSE_PRESSED,
			ff.getLayoutY() , ff.getLayoutY(), 
			ff.getLayoutX(), ff.getLayoutY(), 
			MouseButton.PRIMARY, 1,
            true, true, true, true, true, 
            true, true, true, true, true, 
            null
        ));
	}
	
	public static void fireAcceleratorReleaseEvent(){
		Button ff = VideoPlayer.getBtnAccelerator();
		if(ff == null) {
			return;
		}
//		System.out.println("firing acc release");

		ff.fireEvent(
			new MouseEvent(MouseEvent.MOUSE_RELEASED,
			ff.getLayoutY() , ff.getLayoutY(), 
			ff.getLayoutX(), ff.getLayoutY(), 
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
//		System.out.println("firing dec press");
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
//		System.out.println("firing dec release");
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

}
