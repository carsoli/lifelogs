package application.structs;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

import application.utils.Util;


/**
 * @author Anshuman 
 * @since May, 2017
 * 
 * @author C. Soliman
 * @since June, 2018
 */
public class Event {
	private File fEvent;
    private String id;
    private String name;
    private String absolutePath;
    private long size;
    private ArrayList<Image> images;

    public Event(File fEvent) throws SecurityException{
    	//TODO: better use another structure, if getId() is used a lot; lower cost search structure
        this.fEvent = fEvent;
    	this.id = Util.generateUUID();
        this.name = fEvent.getName();
        this.absolutePath = fEvent.getAbsolutePath();
        this.size = Util.getFileSize(Paths.get(fEvent.getAbsolutePath()));
        this.images = new ArrayList<Image>();
    }
    
    public Event(String name, String absolutePath, long size, ArrayList<Image> images) {
        this.id = Util.generateUUID();
        this.name = name;
        this.absolutePath = absolutePath;
        this.size = size;
        this.images = images;
    }

    public boolean addImage(Image img){
        return images.add(img);
    }
    
    public File[] getFImages() {
		File[] res = null;
		try {
			res = fEvent.listFiles();
		}
		catch(SecurityException e) {
			System.out.println("Error Getting Events Files for Day:" + this.name);
			e.printStackTrace();
		}
		return res;
    }

    public int getImageCount() {
        return images.size();
    }
    
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public long getSize() {
        return size;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

}
