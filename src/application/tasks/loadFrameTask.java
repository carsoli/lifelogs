package application.tasks;

import application.view.ViewUtils;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;

public class loadFrameTask extends Task<ImageView>{
	//loads one frame per call (not pre-loading all frames to buffer)
	
	private int indexToLoadFrom = 0; 
	private int indexToLoadTo = 0;
	
	public loadFrameTask(int loadFrom, int loadTo) {
		this.setIndexToLoadFrom(loadFrom); //points to the array of user images
		this.setIndexToLoadTo(loadTo); //points to the framesBuffer
	}
	
	
	@Override
	protected ImageView call() throws Exception {
		return ViewUtils.createImageView(
				ViewUtils.getChosenImages().get(this.getIndexToLoadFrom()),
				0,ViewUtils.getMainScene().getHeight());
	}

	public int getIndexToLoadFrom() {
		return indexToLoadFrom;
	}

	public void setIndexToLoadFrom(int indexToLoadFrom) {
		this.indexToLoadFrom = indexToLoadFrom;
	}


	public int getIndexToLoadTo() {
		return indexToLoadTo;
	}


	public void setIndexToLoadTo(int indexToLoadTo) {
		this.indexToLoadTo = indexToLoadTo;
	}
	
}
