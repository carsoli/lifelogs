package application.controller;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import application.structs.DataSource;
import application.view.VideoPlayer;
import application.utils.Constants;
import application.view.ViewUtils;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;

public class MainController {
	private static int chosenP = -1; //TODO: ATOMIC INT
	private static int chosenD = -1; 
	private static DataSource mDataSource = null; 
	
	@SuppressWarnings("rawtypes")
	private static ArrayList<Task> tasksLog = new ArrayList<Task>();
	private static Executor threadPool; //ExecutorService; threadPool, extends Executor

	public static Executor initializeThreadPool() {
		try {
			threadPool = Executors.newFixedThreadPool(Constants.MAX_THREADS, runnable -> {
				/*ThreadFactory() implements one method: "newThread" 
				 * Thread newThread (Runnable r)
				 *Implementations of newThread() may also initialize:
						priority, name, daemon status, ThreadGroup, etc.*/
				
				//TODO: need to find a way to cancel every call of runLater in the loadImagesTask
				//since canceling the task using the log does not suffice
				//given that one task has n working threads to load images
				//where n = number of images
				Thread t = new Thread(runnable);
				/*
				 * A daemon thread does NOT prevent the JVM from exiting when the program finishes, 
			         	but the thread is still running. ex: Garbage Collector.
				 * setDaemon(boolean): changes the Thread daemon properties BEFORE the thread starts.
				 */
				try {					
					t.setDaemon(true);
				}
				catch(IllegalThreadStateException e1) {
					System.out.println("Can't call the instance method setDaemon on a Thread Object that's already Alive");
					e1.printStackTrace();
				}
				catch(SecurityException e2){
					System.out.println("Access Denied to the thread that's running the threadFactory method");
					e2.printStackTrace();
				}
				
				return t; 
			});
		}
		catch(IllegalArgumentException e1) {
			System.out.println("CONSTANT: MAX_THREADS <= 0 ");
			e1.printStackTrace();
		}
		catch(NullPointerException e2) {
			System.out.println("VARIABLE: threadPool is null");
			e2.printStackTrace();
		}
		return threadPool;
	}

	public static void execute(@SuppressWarnings("rawtypes") Task t) {
		threadPool.execute(t);
	}
	
	public static void addTask(@SuppressWarnings("rawtypes") Task t) {
		tasksLog.add(t);
	}
	
	public static boolean cancelTask(int index) {
		return tasksLog.get(index).cancel();
	}
	
	@SuppressWarnings("rawtypes")
	public static ArrayList<Task> getRunningTasks(){
		ArrayList<Task> res = new ArrayList<Task>();
		for(Task t: tasksLog) {
			if(t.isRunning()) 
				res.add(t);
		}
		return res; 
	}

	public static void setChosenP(int chosenP) {
		MainController.chosenP = chosenP;
	}

	public static int getChosenP() {
		return chosenP;
	}

	public static EventHandler<KeyEvent> keyReleasedHandler = e -> {
			System.out.println("key released");
			switch (e.getCode()) {
			//fire() triggers ActionEvent
			case SPACE:
				if(VideoPlayer.getBtnPlayPause() != null) {
					VideoPlayer.getBtnPlayPause().fire();
				}
				fireNoOpMouseEvent();//HACK: to unfocus on the button and allow further event handling;
				e.consume();
				break;
			case S: 
				if(VideoPlayer.getBtnStop() != null) {
					VideoPlayer.getBtnStop().fire();
				}
				fireNoOpMouseEvent();
				e.consume();
				break;
			case RIGHT:
				VideoPlayerController.fireAcceleratorReleaseEvent();
				fireNoOpMouseEvent();
				e.consume();
				break;
			case LEFT: 
				VideoPlayerController.fireDeceleratorReleaseEvent();
				fireNoOpMouseEvent();
				e.consume();
				break;
//			case VOLUME_UP:
//				VideoPlayer.getBtnVolumeUp().fire();
//			case VOLUME_DOWN:
//				VideoPlayer.getBtnVolumeDown().fire();
			default:
				break;
			}
	};
	
	public static EventHandler<KeyEvent> keyPressedHandler = e -> {
		System.out.println("key pressed");
			switch(e.getCode()) {
				case RIGHT: 
					VideoPlayerController.fireAcceleratorPressEvent();
					fireNoOpMouseEvent();
					e.consume();
					break;
				case LEFT:
					VideoPlayerController.fireDeceleratorPressEvent();
					fireNoOpMouseEvent();
					e.consume();
					break;
				default: 
					break;
			}

	};

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
	
	public static void installKeyEventHandlers() {
        //KEY PRESS IS DETECTED ANYWHERE IN THE inner-most container of videoPlayer class DOESNT WORK 
		//TODO TRY TO LIMIT IT TO VideoVBox (inner most container of videoplayer)
//		VideoPlayer.getVideoVBox().setOnKeyReleased(MainController.keyReleasedHandler);
		ViewUtils.getMainScene().addEventFilter(KeyEvent.KEY_RELEASED, MainController.keyReleasedHandler);
		ViewUtils.getMainScene().addEventFilter(KeyEvent.KEY_PRESSED, MainController.keyPressedHandler);
	}
	
	public static int getChosenD() {
		return MainController.chosenD;
	}
	
	public static void setChosenD(int dayIdx) {
		MainController.chosenD = dayIdx;
	}

	public static DataSource getmDataSource() {
		return mDataSource;
	}

	public static void setmDataSource(DataSource mDS) {
		MainController.mDataSource = mDS;
	}

	
}
