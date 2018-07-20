package application.controller;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import application.structs.DataSource;
import application.view.FramesGlider;
import application.view.VideoPlayer;
import application.utils.Constants;
import application.view.ViewUtils;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class MainController {
	private static int chosenP = -1; 
	private static int chosenD = -1; 
	private static DataSource mDataSource = null; 
	private static boolean rightKeyPressed = false, leftKeyPressed = false; 
	
	
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
//		//TO SAVE RAM: this overshadows setOnSucceeded implemented per task i guess
		//TODO: find a way to fix it; maybe set a maximum allowed number of tasks and remove the non-running tasks in the list
//		t.setOnSucceeded(new EventHandler<Event>() {
//			@Override
//			public void handle(Event event) {
//				tasksLog.remove(t);
//			}
//		});
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
				/*HACK: removes focus from the button and allows further event handling*/
				VideoPlayerController.fireNoOpMouseEvent();
				//==============================
				if(FramesGlider.getBtnPlayPause() != null) {
					FramesGlider.getBtnPlayPause().fire();
				}
				FramesGliderController.fireNoOpMouseEvent();
				e.consume();
				break;
			case S: 
				if(VideoPlayer.getBtnStop() != null) {
					VideoPlayer.getBtnStop().fire();
				}
				VideoPlayerController.fireNoOpMouseEvent();
				//==============================
				if(FramesGlider.getBtnStop() != null) {
					FramesGlider.getBtnStop().fire();
				}
				FramesGliderController.fireNoOpMouseEvent();
				e.consume();
				break;
			case RIGHT:
				rightKeyPressed = false;
				VideoPlayerController.fireAcceleratorReleaseEvent();
				VideoPlayerController.fireNoOpMouseEvent();
				//===============================
				//NOT A PRESS AND HOLD
				//WORKS BUT DON"T SPAM IT: SPAM THE MOUSE CLICKS (FASTER & DON'T BREAK)
				FramesGliderController.fireAcceleratorReleaseEvent();
				FramesGliderController.fireNoOpMouseEvent();
				
				e.consume();
				break;
			case LEFT: 
				leftKeyPressed = false;
				VideoPlayerController.fireDeceleratorReleaseEvent();
				VideoPlayerController.fireNoOpMouseEvent();
				//================================
				FramesGliderController.fireDeceleratorReleaseEvent();
				FramesGliderController.fireNoOpMouseEvent();
				
				e.consume();
				break;
			default:
				break;
			}
	};

	public static EventHandler<KeyEvent> keyPressedHandler = e -> {
			switch(e.getCode()) {
				case RIGHT: 
					if(!rightKeyPressed) {
						System.out.println("key pressed");
						rightKeyPressed = true;
						VideoPlayerController.fireAcceleratorPressEvent();
						VideoPlayerController.fireNoOpMouseEvent();
					}
					e.consume(); //MUST CONSUME OUTSIDE;; because otherwise the tabs will switch
					break;
				case LEFT:
					if(!leftKeyPressed) {
						leftKeyPressed = true;
						VideoPlayerController.fireDeceleratorPressEvent();
						VideoPlayerController.fireNoOpMouseEvent();
					}
					e.consume();
					break;
				default: 
					break;
			}

	};

		
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
