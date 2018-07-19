package application;

import java.util.ArrayList;
import application.controller.MainController;
import application.view.ViewUtils;
import javafx.application.Application;
import javafx.concurrent.Task;

import javafx.stage.Stage;

/**
 * @author Carol Soliman
 * @since June, 2018
 * */

public class Main extends Application {

	static ArrayList<String> participantImagesPathList = null;
    
	@Override
 	public void start(Stage primaryStage) throws Exception {
		ViewUtils.setMainStage(primaryStage); 
		ViewUtils.initializeScene();
		ViewUtils.getMainStage().setScene(ViewUtils.getMainScene());
		MainController.installKeyEventHandlers();
		ViewUtils.getMainStage().show();
	}
	
	@Override
	public void stop() throws Exception{
		super.stop();//javafx thread
		stopGracefully();
	}
	
	public void stopGracefully() {//to stop Executor(Service) gracefully:
		/*
		 1) All active threads upon creation in the ThreadFactory constructor 
			are set to be Daemon threads; to allow the JVM to close
		 2) We clear the task queue log; where we keep track of any task created 
		*/
		for(@SuppressWarnings("rawtypes") Task task: MainController.getRunningTasks()) {
			//TODO: 
			if (task.isRunning())
				task.cancel();
//			MainController.cancelTask(MainController.getRunningTasks().indexOf(task));
		}
	}
	
	public static void main(String[] args) {
		MainController.initializeThreadPool(); 
		launch(args);
	}

}
