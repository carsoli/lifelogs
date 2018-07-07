package application.structs;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;

import application.utils.Util;

/**
 *	@author C. Soliman
 *	@since June, 2018 
 */
public class Image {
	private String absolutePath;
	private String name;
	private String format;
	private long size;
	private LocalDate takenOn;
	private LocalTime takenAt;
	
	//exception handling is pushed up the call-stack (Higher-level handling, in DataSource Class)
	public Image(File fImage) throws SecurityException{
		String fileName = fImage.getName();
        //trying not to make it rely on the naming :) 
        Path imgPath = Paths.get(fImage.getAbsolutePath());
     
        this.absolutePath = fImage.getAbsolutePath();
        this.name = fileName; 
        this.format = Util.getFileTypeFromExtension(fileName);
        this.size = Util.getFileSize(imgPath);
        this.takenOn = Util.getFileCreationDate(imgPath);
        this.takenAt = Util.getFileCreationTime(imgPath);
	}
	
	
    public String getAbsolutePath() {
        return absolutePath;
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public long getSize() {
        return size;
    }

    public LocalDate getTakenOn() {
        return takenOn;
    }
    
    public LocalTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalTime takenAt) {
        this.takenAt = takenAt;
    }

}
