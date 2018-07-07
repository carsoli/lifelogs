package application.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import application.controller.MainController;
import application.structs.DataSource;
import application.tasks.ParticipantSubMenuTask;
import application.utils.Constants;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ViewUtils {

	private static Scene mScene; 
	private static Stage mStage;
	private static VBox root; 
	private static TilePane imagesTilePaneContainer = new TilePane();  
	private static Menu chooseParticipantSM = null;
	private static Menu createVideosSM = null;
	private static Tab videosTab = null, imagesTab = null; 
	
	public static void setVideosTab(Tab vt) {
		ViewUtils.videosTab = vt;
	}

	public static Menu getCreateVideoSM() {
		return createVideosSM;
	}
	
	public static void setCreateVideoSM(Menu CVSM) {
		createVideosSM = CVSM;
	}
	
	public static Menu getChooseParticipantSM() {
		return chooseParticipantSM;
	}
	
	public static void setChooseParticipantSM(Menu CPSM) {
		chooseParticipantSM = CPSM;
	}
	
	public static Stage getMainStage() {
		return mStage; 
	}
	public static void setMainStage(Stage stage) {
		mStage = stage;
	}
	
	public static ImageView createImageView(final File imgFile) {
        ImageView imageView = null;
        try {			                	
        	final Image image = new Image(new FileInputStream(imgFile), 
        			Constants.IMAGE_SIZE, 0, true, false);
            imageView = new ImageView(image);
            imageView.setFitWidth(Constants.IMAGE_SIZE);

        } catch(FileNotFoundException e1) {
        	System.out.println("Image File not found");
        	e1.printStackTrace();
        } catch(SecurityException e2) {
        	System.out.println("Denied Access to Image File");
        	e2.printStackTrace();
        } catch(Exception e) {
        	e.printStackTrace();
        }
        
		return imageView;	
	}
	
	public static BufferedImage createBufferedImage(final File imgFile) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(imgFile);
		} catch (IOException e) {
			System.out.println("Error reading image file when creating buffered Image");
			e.printStackTrace();
		}
		return image;
	}
	
	public static Tab initializeImagesTab() {
        Tab imagesTab = new TabPaneItem(0, Constants.TAB0_TITLE, false);
		VBox imagesTabVBox = (VBox)(imagesTab.getContent());
				
		ScrollPane imagesScrollPane = new CustomScrollPane(mScene);
        imagesTilePaneContainer = (TilePane)imagesScrollPane.getContent();
        imagesTilePaneContainer.minHeightProperty().bind(mScene.heightProperty());
        imagesTilePaneContainer.setStyle(Constants.BG_BLACK);
      //add the Scroll Pane to the main VBox in Images Tab content
        imagesTabVBox.getChildren().add(imagesScrollPane);
        return imagesTab;
	}
	
	public static Scene getMainScene() {
		return mScene;
	}
	
	public static void initializeScene() {
		root = new VBox();
		
		mScene = new Scene(root);
		
		mStage.setTitle(Constants.MAIN_STAGE_TITLE);
	    /* full screen - support different screen sizes */
        mStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
        mStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
		
		MenuBar topBar = new MenuBar();
		Menu mainMenu = new Menu(Constants.MAIN_MENU_TEXT);
		Menu helpMenu = new Menu(Constants.HELP_MENU_TEXT);
		//MAINMENU_MENU ITEMS:
		//specify main directory menu item
		MenuItem specMDirectoryMI = new MenuItem(Constants.SPECIFY_DIR_MENUITEM_TEXT);
		
		chooseParticipantSM = new Menu(Constants.CHOOSE_PARTICIPANT_MENUITEM_TEXT);
		chooseParticipantSM.setDisable(true); //until directory is specified
		
		createVideosSM = new Menu(Constants.CREATE_VIDEO_MENUITEM_TEXT);
		createVideosSM.setDisable(true); //disable until images location's specified correctly

		mainMenu.getItems().addAll(specMDirectoryMI, chooseParticipantSM, createVideosSM);
		
		topBar.getMenus().addAll(mainMenu, helpMenu); 
		
		/*_Warning:_ DC's not supported on all platforms*/
		final DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle(Constants.DIRECTORY_NAVIGATOR_TITLE);
		specMDirectoryMI.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent actionEvent) {
				File rootFPath = dc.showDialog(mStage); 
				if(rootFPath != null && rootFPath.isDirectory()) {
					chooseParticipantSM.setDisable(false);
					//reset:
					createVideosSM.setDisable(true);
					try {					
						DataSource ds = new DataSource(rootFPath.getAbsolutePath());
						ParticipantSubMenuTask.setMainDataSource(ds);
					}
					catch(SecurityException se) {
						System.out.println("Denied Access to the Chosen Directory");
						se.printStackTrace();
					}
					catch(Exception e) {
						//exception thrown by the DataSource Class
						e.printStackTrace();
					}
					@SuppressWarnings("rawtypes")
					Task createPSMTask = new ParticipantSubMenuTask();
					MainController.addTask(createPSMTask);
					MainController.execute(createPSMTask);
				} 			
				
			}
		});
		
		//TabPane
		TabPane mTabPane = new TabPane(); 
        mTabPane.setStyle(Constants.BG_WHITE);
        mTabPane.setPrefSize(
        	Screen.getPrimary().getVisualBounds().getWidth(),
        	Screen.getPrimary().getVisualBounds().getHeight()); // Default width and height
        mTabPane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        
        //IMAGES TAB; ID: 0
        imagesTab = initializeImagesTab();
        //VIDEOS TAB; ID: 1
        videosTab = VideoPlayer.initializeVideosTab();
        
		mTabPane.getTabs().addAll(imagesTab, videosTab);
        root.getChildren().addAll(topBar, mTabPane);
		
	}
		
	public static TilePane getImagesTilePaneContainer() {
		return imagesTilePaneContainer;
	}
	
	public static void setImagesTilePaneContainer(TilePane tp) {
		imagesTilePaneContainer = tp;
	}
	
	
 }
