package application.structs;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/** 
 *	@author C. Soliman
 *	@since June, 2018
 */

public class Participant {
	//any ID is enforced through the index of the participant in the arrayList maintained
	//by the higher-order class in the hierarchy
	private String participantName; 
	private File[] fDays; 
	private ArrayList<Day> days; 
	
	
	public Participant(File fParticipant) throws SecurityException {
		this.participantName = fParticipant.getName(); //no need for a setter
		this.fDays = setFDays(fParticipant);
		this.days = new ArrayList<Day>();
	}
	
	private File[] setFDays(File fParticipants) {//filter out files that are not directories
		return fParticipants.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
	}
	
	
	public String getParticipantName() {
		return participantName; 
	}
	
	public File[] getFDays() {
		return fDays;
	}
	

	public ArrayList<Day> getDays() {
	    return days;
    }
    
    public int getDaysCount() {
    	return days.size();
    }
    
    public void setDays(ArrayList<Day> days) {
    	this.days = days;
    }
    
    public boolean addDay(Day day) {
    	return days.add(day);
    }
    
}
