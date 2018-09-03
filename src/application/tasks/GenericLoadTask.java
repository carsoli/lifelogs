package application.tasks;

import java.io.File;
import java.util.ArrayList;

import application.view.ViewUtils;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;

@SuppressWarnings("rawtypes")
public class GenericLoadTask extends Task{
	private int startIdx; 
	private int endIdx; 
	private int size; 
	
	public GenericLoadTask(int s, int e, int bufferSize) { 
			//,List<ImageView> buff){
		this.startIdx = s; 
		this.endIdx = e; 
		this.size = bufferSize;
	}
	
	@Override
	protected ArrayList<ImageView> call() throws Exception {
		ViewUtils.sortChosenImages();//this sorts the chosenImages and uses the output to setFImages()
		ArrayList<File> chosenImages = ViewUtils.getChosenImages();
		//TODO: filling with null values is redundant; 
		//but add(index,object) doesn't work randomly unless all are initialized to null
		//instead; make buffer a hashmap that adds frames with the index as key 
		ArrayList<ImageView> resultList = new ArrayList<ImageView>(this.size);
		for(int i=0; i<this.size; i++) {
			resultList.add(i, null);
		}
		
		ImageView imgView = null;
		for(int c= this.startIdx; c<= this.endIdx; c++) {
			imgView = ViewUtils.createImageView(
					chosenImages.get(c),
					//TODO: try to add the correct height from the beginning
					//instead of updating it in the updateFrameHbox fn in FramesGlider
					0,ViewUtils.getMainScene().getHeight()); 
			//add the images to where they would be loaded normally, and the rest is null
			try {
				resultList.set((c%this.size),imgView); 
			} catch (Exception e) {
				System.out.println("failed to add image idx: " + c%this.size + " to newBuffer");
				e.printStackTrace();
			}
		}
		
		return resultList;
	}
	
}
