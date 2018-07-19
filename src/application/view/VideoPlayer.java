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
import application.structs.Participant;
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
	private static ImageButton btnDecelerator, 
	btnStop, btnAccelerator;
	
	//wait for (pauseTime) then call the Timer again (similar to Anim/Timer in openCV)
	private static final Duration pauseTime = Duration.millis(500);
	
	private static PauseTransition accHoldTimer = null, decHoldTimer = null;
//	private static int accPressedCycles = 0, decPressedCycles = 0; 
	private static final double initPlayRate = 1.0, maxRate = 8, minRate = 0.125; 
	private static final double maxAcc = maxRate - initPlayRate;
	private static final double minDec = minRate - initPlayRate; 
	
	//takes {x} cycles to reach max desired rate (max and 10 are set according to preference
	private static final double accStep = (maxRate-initPlayRate)/10; //+ve slope
	//takes {x} cycles to reach min rate
	private static final double decStep = (minRate - initPlayRate)/10; //-ve slope  
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
        //main container in the tab
        VBox videoTabVBox = (VBox)videosTab.getContent(); 
        //2nd main container
        ScrollPane videoSP = new CustomScrollPane(ViewUtils.getMainScene()); 
		videoTabVBox.getChildren().add(videoSP);
		//3rd container: useless
        videoTP = (TilePane)videoSP.getContent(); 
        videoTP.setPadding(new Insets(0, 0, 0, 0)); //override the styling in the class
        //4th container
		videoVBox = new VBox();
        videoVBox.setStyle(Constants.BG_BLACK);
        videoVBox.setAlignment(Pos.TOP_CENTER);
		videoVBox.prefHeightProperty().bind(ViewUtils.getMainScene().heightProperty());

		return videosTab;
	}

	public static void postProcessVideo() {
		Participant chosenParticipant = MainController.getmDataSource().getData().get(MainController.getChosenP());
		String inputFileName = chosenParticipant.getParticipantName() + 
				chosenParticipant.getDays().get(MainController.getChosenD()).getName()
				+ ".mp4";
				
		inputFilePath = new File(inputFileName).getAbsolutePath();
		outputFilePath = inputFilePath.replace(".mp4", "scaledDown.mp4");

		ProgressListener listener = new ProgressListener() {
		    @Override
		    public void onProgress(FFmpegProgress progress) {
//		        System.out.println("progress?");
		    }
		};
		System.out.println("Post Processing: Scaling Video...");
		FFmpeg.atPath(Constants.FFMPEG_BIN)
		        .addInput(UrlInput.fromPath( Paths.get(inputFilePath) ))
		        .addOutput(UrlOutput.toPath( Paths.get(outputFilePath) ))
		        .addArguments("-vf", "scale=1440:1080")
		        .setOverwriteOutput(true)
		        .setProgressListener(listener)
		        .execute();
		System.out.println("done with post processing");
    	Tab videosTab = updateVideosTab();
    	ViewUtils.setVideosTab(videosTab);
	}
	
	public static Tab updateVideosTab() {
		String videoPath = "", videoURI = "";
//		String videoFileName = Constants.VIDEO_STATIC_URL;

		try {
//        	videoPath = new File(videoFileName).getAbsolutePath();
        	videoPath = new File(outputFilePath).getAbsolutePath();
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
			acc += accStep;
			System.out.println("acceleration: "+ acc);
			System.out.println("new rate: "+ (initPlayRate + acc));
			if(acc < maxAcc) {
				System.out.println("did NOT exceed max acc");
				System.out.println("==============================");
				mp.setRate((initPlayRate + acc));
			}
			accHoldTimer.playFromStart();
		});	
		//=======================
		decHoldTimer = new PauseTransition(pauseTime);
		decHoldTimer.setOnFinished(e -> {
			dec+= decStep;
			System.out.println("dec: "+ dec);
			System.out.println("new rate:" + (initPlayRate + dec));

			if(dec > minDec) {//comparing negative magnitudes
				System.out.println("did NOT exceed min dec");
				System.out.println("==============================");
				mp.setRate((initPlayRate + dec));
			}
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
//		btnAccelerator.setOnKeyPressed(MainController.keyPressedHandler);
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
				btnDecelerator, 
				btnPlayPause, btnStop,
				btnAccelerator); 
    	return controlPane;
	}

	public static ImageToggleButton getBtnPlayPause() {
		return btnPlayPause;
	}
	
	public static ImageButton getBtnStop() {
		return btnStop;
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
	
	public static void setAcc(double acc) {
		VideoPlayer.acc = acc;
	}
	
	public static void setDec(double dec) {
		VideoPlayer.dec = dec;
	}
	
}

