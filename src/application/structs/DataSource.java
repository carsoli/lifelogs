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
		data = new ArrayList<Participant>();
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
	
//	public ArrayList<String> getAllImagesPaths(int participantIndex){
//		ArrayList<String> allImagesPaths = null;
//		ArrayList<Day> pDays= this.data.get(participantIndex).getDays();
//		
//		for(Day d: pDays) {
//			System.out.println("DAY: "+ d.getName());//debugging
//			ArrayList<Event> dEvents = d.getEvents();
//			for(Event e :dEvents) {
//				ArrayList<Image> eventImages = e.getImages();
//				for(Image i: eventImages) {
//					try {
//						allImagesPaths.add(i.getAbsolutePath());
//					}
//					catch( SecurityException imageFileE) {
//						imageFileE.printStackTrace();
//					}
//				}
//			}
//		}
//		return allImagesPaths;
//	}	
}

