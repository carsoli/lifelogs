package application.utils;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.util.Duration;

/**
 * @author Carol Soliman
 * @since June, 2018
 * */

public final class Constants {
	/*
	 * Active threads consume system resources, especially memory. When there are more runnable threads 
	 * than available processors, threads sit idle. Having many idle threads can tie up a lot of memory, 
	 * putting pressure on the garbage collector, and having many threads competing for the CPUs can 
	 * impose other performance costs as well. If you have enough threads to keep all the CPUs busy, 
	 * creating more threads won't help and may even hurt.
	 * */
	public static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
	public static final String MAIN_STAGE_TITLE = "LIFELOGS";
	public static final String MAIN_MENU_TEXT = "Menu";
	public static final String HELP_MENU_TEXT = "Help";
	public static final String DIRECTORY_NAVIGATOR_TITLE = "Find Images Directory";
	public static final String SPECIFY_DIR_MENUITEM_TEXT = "Specify Directory";
	public static final String CHOOSE_PARTICIPANT_MENUITEM_TEXT = "Choose Participant";
	public static final String CREATE_VIDEO_MENUITEM_TEXT = "Create Video";

	public static final String EPOCH = "1970-01-01T00:00:00Z";
	public static final String DT_FORMAT = "yyyyMMddHHmmss";
	public static final String TAB0_TITLE = "Images";
	public static final String TAB1_TITLE = "Videos";
	public static final String TAB2_TITLE = "Frames Glider";

	public static final double  IMAGE_SIZE = 150.0;

//    public static final String BG_WHITE = "-fx-background-color: white;";
    public static final String BG_BLACK = "-fx-background-color: black;";
    public static final String BG_RED = "-fx-background-color: red;";
    public static final String BG_GREEN = "-fx-background-color: green;";
    public static final String BG_BLUE = "-fx-background-color: blue;";
    
    public static final String BG_TRANSPARENT = "-fx-background-color: transparent;";
    
    public static final URL ASSETS_URL = Constants.class.getClassLoader().getResource("assets/");
    public static final URL OUTPUT_URL = Constants.class.getClassLoader().getResource("output/");
    
    public static final String PAUSE_IMG = "pause.png";
    public static final String PLAY_IMG = "play.png";
    public static final String FB_IMG = "fastbackward.png";
    public static final String FF_IMG = "fastforward.png";
    public static final String STOP_IMG = "stop.png";
    public static final String VOLUMEUP_IMG = "volumeup.png";
    public static final String VOLUMEDOWN_IMG = "volumedown.png";
    public static final String BLACK_SCREEN = "blackScreen.png";
    
    /*covnertor constants*/
	public static final String FORMAT_NAME = null; //if unspecified, default's used
	public static final String CODEC_NAME = null; //if unspecified, default's used
	public static final int DEFAULT_IMAGE_WIDTH = 2592;
	public static final int DEFAULT_IMAGE_HEIGHT = 1944;
	/*Video: PostProcessing*/
	public static final Path FFMPEG_BIN = Paths.get("C:\\Users\\Carol\\FFmpeg\\bin");
	public static final String FFMPEG_EXE = "C:\\Users\\Carol\\FFmpeg\\bin\\ffmpeg.exe";
	public static final boolean VIDEO_AUTOPLAY = false;
	/*Also, FramesGlider constants*/
	public static final int FPS = 10;  
	public static boolean GLIDER_AUTOPLAY = false; 
	public static final int FRAMES_BUFFER_SIZE = 5;
	public static final Duration GLIDER_INIT_RATE = Duration.millis((1/((double)Constants.FPS))*1000);
	public static final Duration GLIDER_DEC_STEP = Duration.millis(((1/((double)Constants.FPS))*1000)/24*3);
	//24 is the number of switches in the knob
	public static final Duration GLIDER_ACC_STEP = Duration.millis(((1/((double)Constants.FPS))*1000)/24);
	public static final Duration GLIDER_SLOWEST_RATE = Duration.millis((1/((double)Constants.FPS))*1000*30); 
	
}
