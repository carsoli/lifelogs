package application.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;


public class Util {
	
    public static String generateUUID(){ 
    	/*generate random UUIDs*/
        return UUID.randomUUID().toString();
    }

    public static long getFileSize(Path path){
        final AtomicLong size = new AtomicLong(0);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.out.println("skipped: " + file + " (" + exc + ")");
                    // Skip folders that can't be traversed
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    if (exc != null)
                        System.out.println("had trouble traversing: " + dir + " (" + exc + ")");
                    // Ignore errors traversing a folder
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e1) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }
        catch (SecurityException e2) {
        	System.out.println("Security Exception in walkFileTree; Access Denied");
        	e2.printStackTrace();
        }

        return size.get();
    }

    public static String getFileTypeFromExtension(String name) {
        String ext = name.substring(name.lastIndexOf(".") + 1);
        return ext;
    }
    
    private static LocalDateTime getFileCreationDT(Path path) {
    	BasicFileAttributes attr = null;
    	LocalDateTime res = null;
		try {
			attr = Files.readAttributes(path, BasicFileAttributes.class);
			
			FileTime creationTime = attr.creationTime();
			FileTime modificationTime = attr.lastModifiedTime(); 
			if (creationTime.toString() != Constants.EPOCH && modificationTime.toString() != Constants.EPOCH) {				
				FileTime actualCreationTime = creationTime.compareTo(modificationTime)<0?creationTime:modificationTime;
				String timeString = ((actualCreationTime.toString().split("T")[1]).split("Z")[0]).replaceAll(":",""); 
				String dateString = (actualCreationTime.toString().split("T")[0]).replaceAll("-", "");
				DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern(Constants.DT_FORMAT);
				try {		
					res = LocalDateTime.parse(dateString+timeString,dtFormatter);
				}
				catch(DateTimeParseException dte) {
	            	System.out.println("Error Parsing Date/Time String");
	            	dte.printStackTrace();
	            }
			}
		} catch (IOException e) {
			System.out.println("Error Reading Basic file attributes");
			e.printStackTrace();
		}
		return res;
    }  
    
    public static LocalDate getFileCreationDate(Path path) {
    	LocalDateTime dt = getFileCreationDT(path);
        return dt.toLocalDate();
    }
   
    public static LocalTime getFileCreationTime(Path path) {
    	LocalDateTime dt = getFileCreationDT(path);
    	return dt.toLocalTime();	
    }
    
    
    
}
