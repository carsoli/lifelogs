package application.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import application.controller.MainController;
import application.utils.Constants;
import application.view.ViewUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

@SuppressWarnings("rawtypes")
public class DisplayGridImagesTask extends Task{
	
	@Override
	protected Object call() throws Exception {
		//pass this to constructor call
		displayParticipantImages();
		return null;
	}
	
	private static void displayParticipantImages() {
		sortParticipantImages();
		//addToAllImages in LoadImagesTask adds in random order
		//sortParticipantsImages updates its order 
		ArrayList<File> pImages = MainController.getmDataSource().getData()
				.get(MainController.getChosenP()).getAllImages(); 
				
		System.out.println("DisplayGridImagesTask:: size of children of tilePane:" + 
				ViewUtils.getImagesTilePaneContainer().getChildren().size() );
		for(File i: pImages) {
			final ImageView gridImageView = ViewUtils.createImageView(i, 
					Constants.IMAGE_SIZE, 0);
			int sortedImageIndex = pImages.indexOf(i); 
			final HBox imageHBox = new HBox();
			Platform.runLater(new Runnable() {
                public void run() {
                	imageHBox.getChildren().add(gridImageView);
                	ViewUtils.getImagesTilePaneContainer().getChildren().set(sortedImageIndex, imageHBox);
                }
            });
		}
	}
	
	private static void sortParticipantImages() {
		ArrayList<File> pImages = MainController.getmDataSource().getData()
				.get(MainController.getChosenP()).getAllImages();

		Collections.sort(pImages, new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					long n1 = extractNumber(f1.getName());
		            long n2 = extractNumber(f2.getName());
		            return (int)(n1 - n2);
				}
				
				private long extractNumber(String name) {
					long i = 0;
		            try {
		            //DATASET FORMAT: {date}_{time stamp}_{ID_for_this_data&timeStamp}.JPG
		            	int startIdx = name.indexOf('_')+1;
		                int endIdx = name.lastIndexOf("_");
		            	String timeStamp = name.substring(startIdx, endIdx); 
		            	int dateEndIdx = name.indexOf('_');//dateEndIdx is the index of the first underscore
		            	String date = name.substring(0, dateEndIdx); //starts at 0, stops before underscore; exclusive
		            	String number = date + timeStamp;
		            	i = Long.parseLong(number);
		            } catch(IndexOutOfBoundsException e1) {
		            	System.out.println("can't extract date OR timeStamp subString");
		            	e1.printStackTrace();
		            }
		            catch(NumberFormatException e2) {
		            	// if filename does not match the format, default to 0
		            	i = 0; 
		            	System.out.println("Error Parsing ImageID");
		            	e2.printStackTrace();
		            }
		            return i;
		        }
			});
		MainController.getmDataSource().getData().get(MainController.getChosenP()).setAllImages(pImages);
	}
	
}
