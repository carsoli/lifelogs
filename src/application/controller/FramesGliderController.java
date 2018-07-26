package application.controller;

import java.util.ArrayList;

import application.tasks.GenericLoadTask;
import application.utils.Constants;
import application.view.FramesGlider;
import application.view.ImageToggleButton;
import application.view.ViewUtils;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class FramesGliderController {
	private static ImageToggleButton btnPlayPause = null;
	private static boolean initiallyPlaying = false;
	
	private static Duration initFrameRate = Constants.GLIDER_INIT_RATE;
	//24 switch positions to get to a frame rate = 0.0 ms
	private static Duration accStep = Constants.GLIDER_ACC_STEP;
	//could be the same as accStep(or different)
	private static Duration decStep = Constants.GLIDER_DEC_STEP;
	private static Duration slowestRate = Constants.GLIDER_SLOWEST_RATE; 
	private static Duration fastestRate = Duration.millis(0.0);
	//fastest theoretically-possible rate(0.0 ms pause); further acceleration is ineffective 
	private static boolean minRateReached = false;
	//slowest desired rate; further deceleration is ineffective 
	private static boolean maxRateReached = false;
	
	private static int lastLoadedFrame = 0;
	//actual index in the array of chosen Images that was displayed before slider was pressed
	private static int lastDisplayedIndexBeforeDragging = -1; //index in actual images of user
	
	
	public static EventHandler<ActionEvent> playPauseHandler = new EventHandler<ActionEvent>() {
		public void handle(ActionEvent e) {
			btnPlayPause = FramesGlider.getBtnPlayPause();
			initiallyPlaying = FramesGlider.isInitiallyPlaying();
			if(btnPlayPause == null) {
				System.out.println("playPauseHandler:: Null PlayPauseButton");
				return;
			}
			
			if(btnPlayPause.isSelected()) {
				btnPlayPause.setSelectedBG();
			} else {
				btnPlayPause.setUnselectedBG();
			}
			
			if(initiallyPlaying == btnPlayPause.isSelected()){//XNOR
				FramesGlider.pausePauseTimer();
			} else if(initiallyPlaying && !btnPlayPause.isSelected() || !initiallyPlaying && btnPlayPause.isSelected()) {//XOR
				FramesGlider.playPauseTimer();
			}
			e.consume(); //avoid bubbling
		}
	};
	
	public static EventHandler<ActionEvent> stopHandler = new EventHandler<ActionEvent>(){
		public void handle(ActionEvent e) {
			btnPlayPause = FramesGlider.getBtnPlayPause();
			initiallyPlaying = FramesGlider.isInitiallyPlaying();
			
			if(btnPlayPause == null) {
				System.out.println("stopHandler:: Null PlayPauseButton");
				return;
			}
			FramesGlider.stopPlayingAndReset();
			e.consume();
		}
	};

	//NOT A PRESS AND HOLD CASE; just a fire event when the button is pressed (equivalent to knob rot)
	public static EventHandler<Event> acceleratorReleaseHandler = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			//takes effect in the next cycle 
			if(minRateReached) 
				return;
//				System.out.println("Min Rate Reached");
			Duration currRate = FramesGlider.getRate();
			Duration newRate = currRate.subtract(accStep); //less time
			if(newRate.compareTo(fastestRate) >0) {//newRate>0 
				FramesGlider.setRate(newRate);
			} else {
				minRateReached = true;
			}
			event.consume();
		}
	};

	public static EventHandler<Event> deceleratorReleaseHandler = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			//slowestRate already reached
			if(maxRateReached) 
				return;
			Duration currRate = FramesGlider.getRate();
			Duration newRate = currRate.add(decStep); //increase sleepTime of pauseTransition
			if(newRate.compareTo(slowestRate) < 0){ //newRate < slowestRate 
				FramesGlider.setRate(newRate);
			} else {
				maxRateReached = true;
			}
			event.consume();
		}
		
	};
	
	public static EventHandler<Event> seekPressHandler = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			FramesGlider.pausePauseTimer();
			event.consume();
		}
	};

	
	public static EventHandler<Event> seekReleaseHandler = new EventHandler<Event>() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void handle(Event event) {
			int currSliderV = new Double(FramesGlider.getCurrSliderValue() ).intValue();
			int stopPtr = FramesGlider.getStopPtr();
			int totalFrames = FramesGlider.getTotalFrames();
			int bufferSize = FramesGlider.getBufferSize();
			
			System.out.println("Seek Release Handler=======");
			System.out.println("currSliderV: "+ currSliderV);
			System.out.println("totalFrames: " + totalFrames);
			System.out.println("stopPtr: "+ stopPtr);
			
			//==============BASE CASES: {2,3,4}: NO loading needed
			//1) Seek to the end of the video; stopPlayingAndReset 
			if(currSliderV == FramesGlider.getTotalFrames() ) { // || currSliderV == 0) {
				System.out.println("base case 1====================");
				FramesGlider.stopPlayingAndReset();
				setLastDisplayedIndexBeforeDragging(-1);
				return;
			}
			//2) the user eventually slid to the frame value he had before using the slider
			//do nothing; just resume playing and return 
			if(lastDisplayedIndexBeforeDragging == currSliderV) {
				System.out.println("base case: 2====================");
				setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
				FramesGlider.playPauseTimer();
				return;
			}
			
			//3) buffer fits all Images
			if(bufferSize == totalFrames) {
				System.out.println("base case: 3===================");
				FramesGlider.setCurrBuffPtr(currSliderV);
				setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
				FramesGlider.playPauseTimer();
				return;
			}
			
			//4) we already loaded the needed frames, and were only displaying them before the slider action started
			//AND we are seeking fw OR bw within them (RANGE::FROM: {total-buffer size} TO: {total} 
			if(stopPtr != -1) {
				if(currSliderV >= (totalFrames - bufferSize) && currSliderV < totalFrames) {
					System.out.println("base case: 4=================");
					//seeking within the range of already loaded frames 
					//TODO; do we need to handle case where stopPtr = 0? exact multiple case
					int nThFrame = currSliderV%bufferSize;
					FramesGlider.setCurrBuffPtr(nThFrame);
					//NO NEED TO SET STOPPTR OR LLI
					setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
					FramesGlider.playPauseTimer();
					return;
				}
			}
			
			//REMINDER: lastDisplayedIdx represents the frame idx to be loaded NEXT
			//sliderValue represents the frame idx to be loaded NEXT
			//====================NON-BASE CASES: loading needed(partially or fully)
			//1)BW SEEKING
			if(currSliderV < lastDisplayedIndexBeforeDragging) {
				//number of images we need to load
				int diff = lastDisplayedIndexBeforeDragging - currSliderV;
				System.out.println("BW SEEK: DIFF:" + diff);
				if(diff < bufferSize) {
					System.out.println("NB-C1: ===");
					
					int startLoadingIndex = currSliderV; 
					
					//if the slider value reached the max, and user dragged it, we stopPlayingAndReset
					//and we set lastDisplayedIdx to 0; so any change by the user would probably be seek 
					//forward in this case and endLoadingIndex would never be out of bound in the following line
					int endLoadingIndex = (lastDisplayedIndexBeforeDragging-1);
					//-1 b/c LDI represents the frame to be displayed next which means the next frame is already loaded
					
					Task loadingTask = new GenericLoadTask(startLoadingIndex, 
							endLoadingIndex, bufferSize);
//					System.out.println( "startIdx: " + startLoadingIndex + " /endIdx: " + endLoadingIndex);
			
					loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							ArrayList<ImageView> newBuffer = (ArrayList<ImageView>) loadingTask.getValue();
							int currPtr = ((endLoadingIndex+1)%bufferSize);
							ArrayList<ImageView> oldBuffer = FramesBufferController.getBuffer();
							while(currPtr!= (startLoadingIndex%bufferSize)) {
								
								//loop from after end to start index (on all the null values of the newBuffer)
								newBuffer.set(currPtr, oldBuffer.get(currPtr));
								currPtr = ((currPtr+1)%bufferSize);
							}
							
							FramesBufferController.setBuffer((ArrayList<ImageView>) newBuffer);
							FramesGlider.setCurrBuffPtr((startLoadingIndex%bufferSize));
							//if it had a value and we needed to keep it, we'd be in the first base case
							FramesGlider.setStopPtr(-1); 
							//last loaded frame had a value before resetting the buffer
							//we replaced N images from the old buffer where N == diff
							//thus, ignoring them from the buffer that already existed, and now we need to set
							//lastloaded to lastloaded - diff; to be able to replace correctly the next time
							//the buffer is accessed
							int newLLI = (FramesGliderController.getLastLoadedFrame() - diff);
							System.out.println("newLLI: " + newLLI);
							FramesGliderController.setLastLoadedFrame(newLLI);
//							System.out.println("last loaded index: " + FramesGliderController.getLastLoadedFrame());
//							System.out.println("currbufferPtr: " + (startLoadingIndex%bufferSize));
//							System.out.println("========================");
							
							setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
							FramesGlider.playPauseTimer();
						}
						
					});
					MainController.addTask(loadingTask);
					MainController.execute(loadingTask);
					return;
				} else {
					System.out.println("NB-C2: ===================");
					//we have to pre-load ALL frames in buffer
					int startLoadingIndex = currSliderV; 
					//-1 b/c LDI represents the frame to be displayed next which means the next frame is already loaded
					int endLoadingIndex = (currSliderV + bufferSize -1);
					Task loadingTask = new GenericLoadTask(startLoadingIndex, 
							endLoadingIndex, bufferSize);
//					System.out.println( "startIdx: " + startLoadingIndex + " /endIdx: " + endLoadingIndex);
					
					loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							ArrayList<ImageView> newBuffer = (ArrayList<ImageView>) loadingTask.getValue();
							
							FramesBufferController.setBuffer((ArrayList<ImageView>) newBuffer);
							
							FramesGlider.setCurrBuffPtr((startLoadingIndex%bufferSize));
							//if it had a value and we needed to keep it, we'd be in the first base case
							FramesGlider.setStopPtr(-1); 
							FramesGliderController.setLastLoadedFrame((endLoadingIndex+1));//end loading is the last 
//							System.out.println("last loaded index: " + getLastLoadedFrame());
//							System.out.println("currbufferPtr: " + (startLoadingIndex%bufferSize));
//							System.out.println("========================");
							//we actually loaded but last loaded frame is used to load the new frame to be loaded 
							setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
							FramesGlider.playPauseTimer();
						}
						
					});
					MainController.addTask(loadingTask);
					MainController.execute(loadingTask);
					return;
				}
				
			} 
			
			
			
//			else { //EQUAL CASE IS HANDLED AS THE FIRST BASE CASE
//			//2)Seek FW
//				int diff = currSliderV - lastDisplayedIndexBeforeDragging;
//				if(diff < bufferSize) { //partially load
//					List<ImageView> newBuffer = new ArrayList<ImageView>(Constants.FRAMES_BUFFER_SIZE);
//					//IT MAY BE THAT WHEN WE LOAD THE 2ND PART OF THE NEW BUFFER, WE REACH TOTAL FRAMES
//					Task loadingTask;
//					if(currSliderV+bufferSize >= totalFrames) {
//						//**REVISE
//						loadingTask = new PartialPreloadTask(
//								currSliderV + (bufferSize - diff), totalFrames-1);
//						loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//							@Override
//							public void handle(WorkerStateEvent event) {
//								newBuffer.addAll(FramesBufferController.getBuffer().subList(
//										currSliderV, 
//										currSliderV + (bufferSize - diff) -1 ));//***
//								newBuffer.addAll((Collection<? extends ImageView>) loadingTask.getValue());
//								FramesBufferController.setBuffer((ArrayList<ImageView>) newBuffer);
//								FramesGlider.setCurrBuffPtr(0);
//								//stopPtr is basically how much of the buffer we will occupy when we rearrange
//								FramesGlider.setStopPtr((totalFrames-currSliderV)); 
//								FramesGliderController.setLastLoadedFrame(totalFrames-1);
//								setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
//								FramesGlider.playPauseTimer();
//							}
//						});
//
//					} else {
//						loadingTask = new PartialPreloadTask(
//								currSliderV + (bufferSize - diff), currSliderV+bufferSize);
//						loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//							@Override
//							public void handle(WorkerStateEvent event) {
//								newBuffer.addAll(FramesBufferController.getBuffer().subList(
//										currSliderV, 
//										currSliderV + (bufferSize - diff) -1));//***
//								newBuffer.addAll((Collection<? extends ImageView>) loadingTask.getValue());
//								FramesBufferController.setBuffer((ArrayList<ImageView>) newBuffer);
//								FramesGlider.setCurrBuffPtr(0);
//								FramesGlider.setStopPtr(-1); 
//								FramesGliderController.setLastLoadedFrame(currSliderV+bufferSize); //the 2nd arg of partialloading
//								setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
//								FramesGlider.playPauseTimer();
//							}
//						});
//
//					}
//					
//					MainController.addTask(loadingTask);
//					MainController.execute(loadingTask);
//					return;
//
//				} else {
//					//we have to pre-load all frames
//					Task loadingTask;
//					if((currSliderV + bufferSize) >= totalFrames) {
//						//it may be that when load, we reach total frames
//						//so we have to update stopPtr and choose the partialPreloading task
//						List<ImageView> newBuffer = new ArrayList<ImageView>(Constants.FRAMES_BUFFER_SIZE);
//						//we could have set it to (totalFrames-currSliderV+1) but 
//						//when we set setbuffer it will ruin it 
//						loadingTask = new PartialPreloadTask(currSliderV, totalFrames-1);
//						loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//							@Override
//							public void handle(WorkerStateEvent event) {
//								newBuffer.addAll((Collection<? extends ImageView>) loadingTask.getValue());
//								FramesBufferController.setBuffer((ArrayList<ImageView>) newBuffer);
//								FramesGlider.setCurrBuffPtr(0);
//								FramesGlider.setStopPtr((totalFrames-currSliderV));//index is zero-based  
//								FramesGliderController.setLastLoadedFrame(totalFrames -1);
//								setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
//								FramesGlider.playPauseTimer();
//							}
//						});
//					} else {
//						//preload all frames; and setStopPtr to -1
//						FramesGliderController.setLastLoadedFrame(currSliderV);
//						loadingTask = new PreloadFramesTask(); //it sets the buffer & LLI on its own
//						loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//							@Override
//							public void handle(WorkerStateEvent event) {
//								//the task will set buffer and update lli on its own
//								FramesGlider.setCurrBuffPtr(0);
//								FramesGlider.setStopPtr(-1);
//								setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
//								FramesGlider.playPauseTimer();
//							}
//						});
//					}
//					MainController.addTask(loadingTask);
//					MainController.execute(loadingTask);
//					return;
//				}
//				
//			}
//			event.consume(); //UNREACHABLE CODE
		}
	};
	
	
	public static void fireAcceleratorReleaseEvent(){
		Button btnAcc = FramesGlider.getBtnAccelerator();
		if(btnAcc == null) {
			return;
		}

		btnAcc.fireEvent(
			new MouseEvent(MouseEvent.MOUSE_RELEASED,
			btnAcc.getLayoutY() , btnAcc.getLayoutY(), 
			btnAcc.getLayoutX(), btnAcc.getLayoutY(), 
			MouseButton.PRIMARY, 1,
            true, true, true, true, true, 
            true, true, true, true, true, 
            null
        ));
	}
	
	public static void fireDeceleratorReleaseEvent(){
		Button btnAcc = FramesGlider.getBtnDecelerator();
		if(btnAcc == null) {
			return;
		}
		btnAcc.fireEvent(
			new MouseEvent(MouseEvent.MOUSE_RELEASED,
			btnAcc.getLayoutY() , btnAcc.getLayoutY(), 
			btnAcc.getLayoutX(), btnAcc.getLayoutY(), 
			MouseButton.PRIMARY, 1,
            true, true, true, true, true, 
            true, true, true, true, true, 
            null
        ));
	}
	
	public static int getLastLoadedFrame() {
		return lastLoadedFrame;
	}
	
	public static void setLastLoadedFrame(int idx) {
		FramesGliderController.lastLoadedFrame = idx;
	}
	
	public static void decrementLastLoadedFrame() {
		FramesGliderController.lastLoadedFrame--;
	}

	public static void IncremenetLastLoadedFrame() {
		FramesGliderController.lastLoadedFrame++;
	}
	
	public static Image getBlackScreenImage() {
		return new Image(Constants.ASSETS_URL.toString() + Constants.BLACK_SCREEN, 
				ViewUtils.getMainScene().getWidth(),
				//TODO: fix the height prop to be that of framesGliderSP
				ViewUtils.getMainScene().getHeight(),
				false,
				true);
	}

	public static void setMinRateReached(boolean minRateReached) {
		FramesGliderController.minRateReached = minRateReached;
	}
	
	public static void setMaxRateReached(boolean maxRateReached) {
		FramesGliderController.maxRateReached = maxRateReached;
	}
	
	public static Duration getInitFrameRate() {
		return FramesGliderController.initFrameRate;
	}
	
	public static void fireNoOpMouseEvent(){
		HBox buttonsTP = FramesGlider.getButtonsHBox();
		if(buttonsTP == null) {
			return;
		}
		buttonsTP.fireEvent(
			new MouseEvent(MouseEvent.MOUSE_PRESSED,
			buttonsTP.getLayoutY() , buttonsTP.getLayoutY(), 
			buttonsTP.getLayoutX(), buttonsTP.getLayoutY(), 
			MouseButton.PRIMARY, 1,
            true, true, true, true, true, 
            true, true, true, true, true, 
            null
        ));


	}

	public static int getLastDisplayedIndexBeforeDragging() {
		return lastDisplayedIndexBeforeDragging;
	}

	public static void setLastDisplayedIndexBeforeDragging(int lastDisBeforeDrag) {
		System.out.println("setting last displayed frame bf drag to: " + lastDisBeforeDrag);
		FramesGliderController.lastDisplayedIndexBeforeDragging = lastDisBeforeDrag;
	}
}


//===============REPLACED BY TWO LINES OF CODE AT THE BEG. OF SEEKRELEASEHANDLER
//cases when nothing will be done; when the user is trying to access the same frame he
//was last playing before dragging the slider
//int total = FramesGlider.getTotalFrames();
//int currBuffPtr = FramesGlider.getCurrBufferPtr(); 
//if(getLastLoadedFrame() == total) {
//	//then we were only displaying already pre-loaded images right now
//	
//	//the curr buff ptr will either range from 0 to this number, inclusive (stopPtr)
//	//or be greater than it
//	int stopPtr = FramesGlider.getTotalFrames()%FramesGlider.getBufferSize();
//	int actualFrameSeq = 0;
//	if(stopPtr == 0) {
//		//total frames is a multiple of buffer
//		actualFrameSeq = total - currBuffPtr; //BRACKETS MATTER
//	} else if (currBuffPtr <= stopPtr ) {
//		actualFrameSeq = total - (stopPtr - currBuffPtr );
//	} else {
//		actualFrameSeq = total -((FramesGlider.getBufferSize() - currBuffPtr) + stopPtr);
//	}
//	//remember how currSlider value was always ahead of its value
//	//actualFS is NOT the index but the number of the frame for real
//	if(actualFrameSeq == FramesGlider.getCurrSliderValue()) {
//		FramesGlider.getPauseTimer().play(); //resume naturally
//		return;
//	}
//	
//	
//} else {
//	
//	if (FramesGlider.getCurrSliderValue() == 
//			(getLastLoadedFrame() - FramesGlider.getBufferSize()) ) 
//		return;
//}


