package application.structs;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import application.utils.Util;

/**
 * @author C. Soliman
 * @since June, 2018
 */

public class Day {
	private String id; //needed for the UI 
	private File fDay;
	private String absolutePath;
	private String name; 
	private ArrayList<Event> events;
	private ArrayList<File> fImages; //all images across all events of that day
	
	public Day(File fDay) throws SecurityException{
		this.setId(Util.generateUUID());
		this.fDay = fDay;
		this.absolutePath = fDay.getAbsolutePath();
		this.name = fDay.getName(); 
		this.events = new ArrayList<Event>();
		this.fImages = new ArrayList<File>();
	}
	
	public boolean addEvent(Event e) {
		return events.add(e);
	}
	
	public File[] getFEvents() {
		File[] res = null;
		try {
			res = fDay.listFiles(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory();
				}
			});
		}
		catch(SecurityException e) {
			System.out.println("Error Getting Events Files for Day:" + this.name);
			e.printStackTrace();
		}
		return res;
	}
	
	
	public int getEventCount(){
		return events.size();
	}
	
	public String getName() {
		return name;
	}
	
	public String getAbsolutePath() {
		return absolutePath;
	}

	public String getId() {
		return id;
	}

	public ArrayList<Event> getEvents() {
		return this.events;
		
	}
	private void setId(String id) {
		this.id = id;
	}

	public ArrayList<File> getFImages() {
		return this.fImages;
	}

	public void setFImages(ArrayList<File> list){
		this.fImages = list;
	}
	
	public void addFImage(File img) { 
		//NOTE**: this is called by loadImagesTask
		//this should be CLEARED when necessary 
		this.fImages.add(img);
	}
	
	
}
