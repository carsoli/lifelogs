package application.utils;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Carol Soliman
 * @since June, 2018
 * */

public final class Constants {
	public static final int MAX_THREADS = 50;
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

	public static final double  IMAGE_SIZE = 150.0;

    public static final String BG_WHITE = "-fx-background-color: white;";
    public static final String BG_BLACK = "-fx-background-color: black;";
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
    /*covnertor constants*/
    public static final String FILE_NAME = "output.mp4";
	public static final String FORMAT_NAME = null; //if unspecified defaults
	public static final String CODEC_NAME = null; //if unspecified defaults
	public static final int FPS = 10; //frames per second
	public static final int IMAGE_RESIZE_FACTOR = 2;
	public static final int DEFAULT_IMAGE_WIDTH = 2592;
	public static final int DEFAULT_IMAGE_HEIGHT = 1944;
	
	public static final String VIDEO_STATIC_URL = "C:\\Users\\Carol\\eclipse-workspace\\lifelogs-videos\\PLarge0scaledDown.mp4";
//	public static final String VIDEO_STATIC_URL = 
//			"C:\\Users\\Carol\\Desktop\\videos\\testing_samples\\scaledDown.mp4";
	public static final Path FFMPEG_BIN = Paths.get("C:\\Users\\Carol\\FFmpeg\\bin");
	public static final String FFMPEG_EXE = "C:\\Users\\Carol\\FFmpeg\\bin\\ffmpeg.exe";
	
}
