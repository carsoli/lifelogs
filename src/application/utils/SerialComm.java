package application.utils;

import java.io.IOException;

import application.controller.FramesGliderController;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import static jssc.SerialPort.MASK_RXCHAR;
import jssc.SerialPortEvent;

public class SerialComm {
	private static SerialPort arduinoPort = null;
	private static String portName; 
	
	public static boolean initializeSerialPort() {//returns true if successfully initialized
		boolean success = false;
		if(getPortNames().length == 0) 
			return success;
		
		portName = getPortNames()[0];
		System.out.println("PORT NAME: " + portName);
		SerialPort serialPort = new SerialPort(portName);
		
		 try {
	            serialPort.openPort();
	            serialPort.setParams(SerialPort.BAUDRATE_9600,
	                    SerialPort.DATABITS_8,
	                    SerialPort.STOPBITS_1,
	                    SerialPort.PARITY_NONE);

	            serialPort.setEventsMask(MASK_RXCHAR);
	            serialPort.addEventListener((SerialPortEvent serialPortEvent) -> {
	                if(serialPortEvent.isRXCHAR()){
	                    try {
	                        byte[] b = serialPort.readBytes();
	                        if(b!= null) {
	                        	int value = b[0] & 0xff;    //convert to int
	                        	String st = String.valueOf(value);
	                        	System.out.println("read string: " + st); //ASCII
	                        	//A == 65, D == 68
	                        	switch(st) {
	                        		case "65": //'A'; accelerate
	                    				FramesGliderController.fireAcceleratorReleaseEvent();
//	                    				FramesGliderController.fireNoOpMouseEvent();
	                        			break;
	                        		case "68": //'D'; decelerate
	                    				FramesGliderController.fireDeceleratorReleaseEvent();
//	                    				FramesGliderController.fireNoOpMouseEvent();
	                        			break;
	                        		default:
	                        			break;
	                        	}
	                        	//*** here is where you call mouse press according to value 
	                        }
	                    } catch (SerialPortException ex) {
	        	            System.out.println("SerialPortException: " + ex.toString());
	                    	ex.printStackTrace();
	                    }
	                }
	            });
	            arduinoPort = serialPort;
	            success = true;
	        } catch (SerialPortException ex) {
	            System.out.println("SerialPortException: " + ex.toString());
	            ex.printStackTrace();
	        }
		 return success;
	}
	
	private static String[] getPortNames() {
		String[] portNames = SerialPortList.getPortNames();
		if (portNames.length == 0) {
		    System.out.println("There are no serial-ports. "
		    		+ "An emulator such ad VSPE can be used to create a virtual one");
		    try {
		        System.in.read();
		    } catch (IOException e) {
		    	System.out.println();
		    	e.printStackTrace();
		    }
		}
		return portNames;
	}
	
	public static void disconnectArduino(){
		if(arduinoPort != null){
			try {
				arduinoPort.removeEventListener();
				if(arduinoPort.isOpened()){
					arduinoPort.closePort();
			    }
			} catch (SerialPortException ex) {
				System.out.println("SerialPortException while closing port");
				ex.printStackTrace();
			}
		}
	}
	
}
