package application.structs;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
/**
 * 
 * @author	C. Soliman
 * @since	June, 2018
 *
 */

/**
 * Data Hierarchy:
 * DataSource/Participants/Days/Events/Images
 * 
 * Naming Conventions:
 * o+ClassName is an Instance of that Class
 * f+ClassName is a FileObject (either a directory or a file) that has Objects of this Class 
 */

public final class DataSource {
	private ArrayList<Participant> data;//All Participants' Data 
	
	public DataSource(String path) throws SecurityException {
		this.data = new ArrayList<Participant>();
		//Absolute Path; thus listFiles instance methods returns absolute paths, as well
		File root = new File(path);
		File[] participants = root.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
		
		for(File fParticipant: participants) {
			Participant oParticipant = new Participant(fParticipant);
			File[] fDays = oParticipant.getFDays(); 
			
			for(File fDay: fDays) {
				if(fDay.isDirectory()) {
					Day oDay = new Day(fDay);
					File[] fEvents = oDay.getFEvents();
					
					for (File fEvent: fEvents) {
						if(fEvent.isDirectory()) {
							Event oEvent = new Event(fEvent);
							File[] fImages = oEvent.getFImages();
							
							for(File fImage: fImages) {
								Image oImage = new Image(fImage);
								oEvent.addImage(oImage); //appends to the ArrayList of Images
							}
							oDay.addEvent(oEvent);
						}
					}
					oParticipant.addDay(oDay);
				}
			}
			data.add(oParticipant);
		}
	}
	
	public ArrayList<Participant> getData(){
		return data;
	}
	
}

