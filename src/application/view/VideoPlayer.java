package application.view;

import java.io.File;
import java.nio.file.Paths;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.github.kokorin.jaffree.ffmpeg.ProgressListener;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;

import application.controller.MainController;
import application.controller.VideoPlayerController;
import application.utils.Constants;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class VideoPlayer {
	private static MediaPlayer mp = null;
	private static TilePane controlPane = null;
	private static MediaView mv = null;
	private static ImageButton btnVolumeDown, btnDecelerator, 
	btnStop, btnAccelerator, btnVolumeUp;
	
	//wait for (pauseTime) then call the Timer again (similar to Anim/Timer in openCV)
	private static final Duration pauseTime = Duration.millis(200);
	
	private static PauseTransition accHoldTimer = null, decHoldTimer = null;
	private static int accPressedCycles = 0, decPressedCycles = 0; 
	//range of MediaPlayer.Rate is 0..8 (double); 0..1 (decelerator), 1..8 (accelerator)
	private static final double initPlayRate = 1.0, maxRate = 8, minDec = 0.1;
	//takes {x} cycles to reach max desired rate (max and 10 are set according to preference
	private static final double accStep = (maxRate-initPlayRate+1)/10; //5-sec-press
	//takes {x} cycles to reach min rate
	private static final double decStep = (initPlayRate-minDec+1)/10; 
	private static double acc = 0, dec = 0;

	
	
	private static ImageToggleButton btnPlayPause;
	private static TilePane videoTP = null; 
	private static VBox videoVBox = null; 
	private static Tab videosTab = null;
	private static String inputFilePath = null;
	private static String outputFilePath = null;
	private static boolean initiallyPlaying = false; //if autoplay is not true
	
	public static Tab initializeVideosTab() {
        videosTab = new TabPaneItem(1, Constants.TAB1_TITLE, false);
        VBox videoTabVBox = (VBox)videosTab.getContent(); //main container in the tab

        ScrollPane videoSP = new CustomScrollPane(ViewUtils.getMainScene()); //2nd main container
		videoTabVBox.getChildren().add(videoSP);
		
        videoTP = (TilePane)videoSP.getContent(); //3rd container: useless
        videoTP.setPadding(new Insets(0, 0, 0, 0)); //override the styling in the class

		videoVBox = new VBox();//4th container
        videoVBox.setStyle(Constants.BG_BLACK);
        videoVBox.setAlignment(Pos.TOP_CENTER);
		videoVBox.prefWidthProperty().bind(ViewUtils.getMainScene().widthProperty());
		videoVBox.prefHeightProperty().bind(ViewUtils.getMainScene().heightProperty());

		return videosTab;
	}

	public static void postProcessVideo() {
//		String inputFileName = MainController.getmDataSource().
//				getData().get(MainController.getChosenP()).
//				getParticipantName() + MainController.getChosenD() + ".mp4";
//		
//		inputFilePath = new File(inputFileName).getAbsolutePath();
//		outputFilePath = inputFilePath.replace(".mp4", "scaledDown.mp4");
//
//		ProgressListener listener = new ProgressListener() {
//		    @Override
//		    public void onProgress(FFmpegProgress progress) {
////		        System.out.println("progress?");
//		    }
//		};
//		System.out.println("Post Processing: Scaling Video...");
//		FFmpeg.atPath(Constants.FFMPEG_BIN)
//		        .addInput(UrlInput.fromPath( Paths.get(inputFilePath) ))
//		        .addOutput(UrlOutput.toPath( Paths.get(outputFilePath) ))
//		        .addArguments("-vf", "scale=1440:1080")
//		        .setOverwriteOutput(true)
//		        .setProgressListener(listener)
//		        .execute();
//		System.out.println("done with post processing");
    	Tab videosTab = updateVideosTab();
    	ViewUtils.setVideosTab(videosTab);
	}
	
	public static Tab updateVideosTab() {
		String videoPath = "", videoURI = "";
		String videoFileName = Constants.VIDEO_STATIC_URL;

		try {
        	videoPath = new File(videoFileName).getAbsolutePath();
//        	videoPath = new File(outputFilePath).getAbsolutePath();
        	videoURI = new File(videoPath).toURI().toString();
        	Media video = new Media(videoURI);
        	System.out.println("checking video src:" + video.getSource());
        	mp = new MediaPlayer(video);
        	mp.setAutoPlay(false);
        	mv = new MediaView(mp);
            mv.fitHeightProperty().bind(ViewUtils.getMainScene().heightProperty());
            mv.setPreserveRatio(true);
            mv.setSmooth(true);
    		/*
    		 * infinite loop implementation bc the built-in cycle is buggy
            * */
            mp.setOnEndOfMedia(new Runnable() {
				public void run() { 
				   mp.seek(Duration.ZERO);
				   mp.play(); 
				   
				}
			}); 
            
            //ERROR HANDLING: TESTING
            video.setOnError(new Runnable(){
            	public void run(){
            		// Handle asynchronous error in Media object.
            		printMessage("Media File:", video.getError());
            	}
            });
            mp.setOnError(new Runnable(){
                public void run(){
                    // Handle asynchronous error in Player object.
                    printMessage("Player:", mp.getError());
                }
            });
            mv.setOnError(new EventHandler <MediaErrorEvent>(){
				@Override
				public void handle(MediaErrorEvent event) {
					printMessage("Media View:", event.getMediaError());
				}
            });

            //CONTROLS
        	controlPane = initializeVideoControls(mp);
        	//TODO : FIX THIS SHITTTTT CLEAR DOESNT WORK
        	videoTP.getChildren().clear();//to avoid duplicate children
        	videoTP.getChildren().add(videoVBox);
        	videoVBox.getChildren().addAll(mv, controlPane);
        } catch(NullPointerException e1) {
        	System.out.println("Null Media Object");
        	e1.printStackTrace();
        } catch (MediaException e2) {
        	System.out.println("Synchronus erros within MediaPlayer constructor");
        	e2.printStackTrace();
        } catch(IllegalArgumentException e3) {
        	System.out.println("Illegal URI/URL");
        	e3.printStackTrace();
        } catch(UnsupportedOperationException e4) {
        	System.out.println("Protocol specified for the source:"+ videoURI +", is not supported");
        	e4.printStackTrace();
        } 
		return videosTab;
	}
	
	protected static void printMessage(String src, MediaException error) {
		MediaException.Type errorType = error.getType();
	    String errorMessage = error.getMessage();
	    System.out.println(src + " Type:" + errorType + ", error mesage:" + errorMessage + "\n");
	}

	private static void initializeHoldTimers() {
		accHoldTimer = new PauseTransition(pauseTime);
		/*
		 * the lag is inevitable:
		 * if i call the pause transition every 100 ms for instance, 
		 * the execution thread of the function (setOnFinished) will be executed
		 * over and over, not allowing for the setRate to execute with the new rate to be visible
		 * */
		accHoldTimer.setOnFinished(e -> {
			//after every PauseTime:
			//increment pressed cycles, add acceleration, update rate, pause again(playFromStart())
			accPressedCycles++; //gets reset
			acc = accPressedCycles*accStep;
			System.out.println("pressed cycles:" + accPressedCycles);
			System.out.println("acceleration: "+ acc);
			System.out.println("new rate: "+ (initPlayRate + acc));
			
			if(acc > maxRate) {
				System.out.println("============exceeded max acc===============");
//				return;
			}

			mp.setRate((initPlayRate + acc));
			//set rate does not take effect
			
			accHoldTimer.playFromStart();
		});	
		
		decHoldTimer = new PauseTransition(pauseTime);
		decHoldTimer.setOnFinished(e -> {
			decPressedCycles++;
			decHoldTimer.playFromStart();
		});	
	}
	
	private static TilePane initializeVideoControls(final MediaPlayer mp){
		TilePane controlPane = new TilePane();
		initializeHoldTimers();
		controlPane.setOrientation(Orientation.HORIZONTAL);
		controlPane.setAlignment(Pos.CENTER);
		controlPane.setStyle(Constants.BG_TRANSPARENT);
		controlPane.setHgap(10);
		controlPane.setMaxHeight(Double.MIN_VALUE); //(smallest height of nodes within)
		//=========
		initiallyPlaying = mp.isAutoPlay(); //if autoplay is true then the video is initially playing
		if(initiallyPlaying) {//then the button should be pause if unselected, and play if selected
			btnPlayPause = new ImageToggleButton(Constants.PAUSE_IMG, Constants.PLAY_IMG);
		} else {//then the button should be play when unselected
			btnPlayPause = new ImageToggleButton(Constants.PLAY_IMG, Constants.PAUSE_IMG);
		}
		btnPlayPause.addEventHandler(ActionEvent.ACTION, VideoPlayerController.playPauseHandler);
		//=========
		btnAccelerator = new ImageButton(Constants.FF_IMG);
		btnAccelerator.setOnMousePressed(VideoPlayerController.acceleratorPressHandler);
		btnAccelerator.setOnMouseReleased(VideoPlayerController.acceleratorReleaseHandler);
		btnAccelerator.setOnTouchPressed(VideoPlayerController.acceleratorPressHandler);
		btnAccelerator.setOnTouchReleased(VideoPlayerController.acceleratorReleaseHandler);
		/*	
		 	this checks if a key is pressed while the Button is selected
		 	we need the handler to be called anywhere in the video container
		 	thus, we add it to the scene (installKeyHandlers())
		 */
//		btnFastForward.setOnKeyPressed(MainController.keyPressedHandler);
		//=========
		btnStop = new ImageButton(Constants.STOP_IMG);
		btnStop.addEventHandler(ActionEvent.ANY, VideoPlayerController.stopHandler);
		//=========
		btnDecelerator = new ImageButton(Constants.FB_IMG);
		btnDecelerator.setOnMousePressed(VideoPlayerController.deceleratorPressHandler);
		btnDecelerator.setOnMouseReleased(VideoPlayerController.deceleratorReleaseHandler);
		btnDecelerator.setOnTouchPressed(VideoPlayerController.deceleratorPressHandler);
		btnDecelerator.setOnTouchReleased(VideoPlayerController.deceleratorReleaseHandler);

		controlPane.setPadding(new Insets(10, 5, 10, 5));
		controlPane.getChildren().addAll(
//				btnVolumeDown, 
				btnDecelerator, 
				btnPlayPause, btnStop,
				btnAccelerator); 
//				btnVolumeUp);
    	return controlPane;
	}

	public static ImageToggleButton getBtnPlayPause() {
		return btnPlayPause;
	}
	
	public static ImageButton getBtnStop() {
		return btnStop;
	}
	
	public static ImageButton getBtnVolumeUp() {
		return btnVolumeUp;
	}
	
	public static ImageButton getBtnVolumeDown() {
		return btnVolumeDown;
	}

	public static void setBtnPlayPause(ImageToggleButton btnPlayPause) {
		VideoPlayer.btnPlayPause = btnPlayPause;
	}
	
	public static ImageButton getBtnAccelerator() {
		return btnAccelerator;
	}

	public static ImageButton getBtnDecelerator() {
		return btnDecelerator;
	}
	
	public static MediaPlayer getMediaPlayer() {
		return mp;
	}
	
	public static TilePane getControlPane() {
		return controlPane;
	}
	
	public static VBox getVideoVBox() {
		return videoVBox;
	}
	
	public static boolean isInitiallyPlaying() {
		return initiallyPlaying;
	}
	
	public static PauseTransition getAccHoldTimer() {
		return accHoldTimer;
	}
	
	public static PauseTransition getDecHoldTimer() {
		return decHoldTimer;
	}
	
	public static int getAccPressedCycles() {
		return accPressedCycles;
	}
	
	public static void setAccPressedCycles(int cycles) {
		VideoPlayer.accPressedCycles = cycles;
	}

	public static int getDecPressedCycles() {
		return decPressedCycles;
	}
	
	public static void setDecPressedCycles(int cycles) {
		VideoPlayer.decPressedCycles = cycles;
	}

}

//public static EventHandler<Event> playPauseHandler = new EventHandler<Event>() {
//public void handle(Event e) {
//	System.out.println("HANDLE PLAY PAUSE");
//	if(btnPlayPause.isSelected()) {
//		btnPlayPause.setSelectedBG();
//	} else {
//		btnPlayPause.setUnselectedBG();
//	}
//	
//	if(initiallyPlaying == btnPlayPause.isSelected()){//XNOR
//		mp.pause();
//	} else if(initiallyPlaying && !btnPlayPause.isSelected() || !initiallyPlaying && btnPlayPause.isSelected()) {//XOR
//		mp.play();
//	}
//	e.consume();
//}
//};
//
//public static EventHandler<Event> stopHandler = new EventHandler<Event>() {
//public void handle(Event e) {
//mp.stop();
//if(initiallyPlaying && !btnPlayPause.isSelected()) {
///*if initially playing & the btn is NOT selected; 
//	video was playing when stop was clicked ->
//	need to toggle the ImageButton's image
//*/
//	btnPlayPause.setSelected(true);
//	btnPlayPause.setSelectedBG();
//}
//if(!initiallyPlaying && btnPlayPause.isSelected()) {
//	btnPlayPause.setSelected(false);
//	btnPlayPause.setUnselectedBG();
//}
//e.consume();//to avoid the event from going to the parent node
//}
//};


//================ put it in the part after btnStop adding handlers
//btnVolumeUp = new ImageButton(Constants.VOLUMEUP_IMG);
//EventHandler<Event> volumeUpHandler = new EventHandler<Event>() {
//	public void handle(Event e) {
//		double currVolume = mp.getVolume();
//		if(currVolume <= 1.0)
//			mp.setVolume(currVolume+=0.1);
//	}
//};
//btnVolumeUp.setOnMouseReleased(volumeUpHandler);
//btnVolumeUp.setOnTouchReleased(volumeUpHandler);
//btnVolumeUp.setOnKeyReleased(MainController.keyReleasedHandler); 

//btnVolumeDown = new ImageButton(Constants.VOLUMEDOWN_IMG);
//EventHandler<Event> volumeDownHandler = new EventHandler<Event>() {
//	public void handle(Event e) {
//		double currVolume = mp.getVolume();
//		if(currVolume >= 0)
//			mp.setVolume(currVolume-=0.1);
//	}
//};
//btnVolumeDown.setOnMouseReleased(volumeDownHandler);
//btnVolumeDown.setOnTouchReleased(volumeDownHandler);
//btnVolumeDown.setOnKeyReleased(MainController.keyReleasedHandler); 
