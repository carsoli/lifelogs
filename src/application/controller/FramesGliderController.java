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
	private static int lastDisplayedIndexBeforePressing = -1;
	private static int toBeDisplayedAfterPressing = -1;
	
	
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
			System.out.println("release: lastbefore pressing: " + lastDisplayedIndexBeforePressing 
					+ " , last before dragging: " + lastDisplayedIndexBeforeDragging);
			if(lastDisplayedIndexBeforeDragging == -1) {
				//then the user pressed on the new value immediately and didn't drag
				//BY DRAGGING WE MEAN: 1) PRESSING(WHICH PAUSES, SETS SLIDER VALUE, AND DISPLAYS DUMMY FRAMES)
				//-- 2)CHANGING VALUE(IF PAUSED, THE LISTENER CAPTURES THE LAST FRAME BEFORE FURTHER CHANGES/DRAGGING 
				//-- 3)RELEASING
				//the value change listener is called BEFORE the release listener
				//so it changes the last pressed value (old value), then release handler is called
				//where it has the last value before press; in case the user is not dragging
				lastDisplayedIndexBeforeDragging = lastDisplayedIndexBeforePressing; //do i need to reset it?
			} 
			int currSliderV = new Double(FramesGlider.getCurrSliderValue() ).intValue();
			int stopPtr = FramesGlider.getStopPtr();
			int totalFrames = FramesGlider.getTotalFrames();
			int bufferSize = FramesGlider.getBufferSize();

			System.out.println("Seek Release Handler=======");
			System.out.println("currSliderV: "+ currSliderV);
			System.out.println("totalFrames: " + totalFrames);
			System.out.println("stopPtr: "+ stopPtr);
			
			//==============BASE CASES: {2,3,4}: NO loading needed
			/*
				1) Seek to the end of the video; stopPlayingAndReset 
			*/
			if(currSliderV == totalFrames ) { // || currSliderV == 0) {
				System.out.println("base case 1====================");
				FramesGlider.stopPlayingAndReset();
				setLastDisplayedIndexBeforeDragging(-1);
				return;
			}
			/*
			    2) the user presses on the NEXT SLIDER VALUE
				or drags to the value NEXT TO THE ONE HE HAD BEFORE DRAGGING
				In this case, no loading is needed, because we know that it was the frame
				we were going to play next anyways, so we just unpause
				do nothing; just resume playing and return 
			*/
			if(lastDisplayedIndexBeforeDragging == currSliderV) {
				System.out.println("base case: 2====================");
				setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
				FramesGlider.playPauseTimer();
				return;
			}
			
			/*
			 * 3) buffer fits all Images
			 */
			if(bufferSize == totalFrames) {
				System.out.println("base case: 3===================");
				FramesGlider.setCurrBuffPtr(currSliderV);
				//if the user drags or presses on zero; the stopPtr
				//will be set to zero b/c buffer fits all; in this case, reset stopPtr
				//in the setOnFinished loop's first iteration, it will
				//display the currentBufferPtr, not stop, check that the lli is == totalFrames
				//then reset the pointer
				//the lli will always be the same as totalFrames in this base case
				//so the stopPtr will be set in the first iteration anyways
				//after displaying the currSliderV
				FramesGlider.setStopPtr(-1);
				setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
				FramesGlider.playPauseTimer();
				return;
			}
			
			//4) we already loaded the final frames, and were only displaying them before the slider action started
			//AND we are seeking fw OR bw within them (RANGE::FROM: {total-buffer size} TO: {total} 
			if(stopPtr != -1) {
				if(currSliderV >= (totalFrames - bufferSize) && currSliderV < totalFrames) {
					System.out.println("base case: 4=================");
					//seeking within the range of already loaded frames 
					int nThFrame = currSliderV%bufferSize;
					FramesGlider.setCurrBuffPtr(nThFrame);
					//NO NEED TO SET STOPPTR OR LLI
					FramesGlider.setStopPtr(-1); 
					//in the case where the number of frames is an exact multiple, stopPtr 
					//will have the same value as the currentBufferPtr (0)
					//so we reset it and because LLI == totalFrames, after the first iteration
					//of anim loop, it will be set again properly
					setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
					FramesGlider.playPauseTimer();
					return;
				}
			}
			
			//REMINDER: lastDisplayedFrame represents the frame INDEX to be loaded NEXT
			//sliderValue represents the frame idx to be loaded NEXT
			//====================NON-BASE CASES: loading needed(partially or fully)
			//1)BW SEEKING
			if(currSliderV < lastDisplayedIndexBeforeDragging) {
				//number of images we need to load
				int diff = lastDisplayedIndexBeforeDragging - currSliderV;
				System.out.println("BW SEEK: DIFF:" + diff);
				if(diff < bufferSize) {
					System.out.println("BW-PARTIAL PRELOADING");
				    int startLoadingIndex = currSliderV; //Also, = LastDisplayedIndexBeforeDrag-diff
//					//if the slider value reached the max, and user dragged it, we stopPlayingAndReset
//					//and we set lastDisplayedIdx to 0; so any change by the user would probably be seek 
//					//forward in this case and endLoadingIndex would never be out of bound in the following line
					int endLoadingIndex = 0;
//					if(lastDisplayedIndexBeforeDragging >= totalFrames-bufferSize && lastDisplayedIndexBeforeDragging < totalFrames) {
					if(getLastLoadedFrame() == totalFrames) {//alternatively
						endLoadingIndex = (totalFrames-bufferSize-1);
						//1st case of partial loading
						System.out.println("1st case: not preloading any of the last {BS} frames");
					} else {
						endLoadingIndex = (lastDisplayedIndexBeforeDragging-1);
						System.out.println("2nd case");
					}
//					//-1 b/c LDI represents the frame to be displayed next which means the next frame is already loaded
					Task loadingTask = new GenericLoadTask(startLoadingIndex, 
							endLoadingIndex, bufferSize);
					System.out.println( "startIdx: " + startLoadingIndex + " /endIdx: " + endLoadingIndex);
					//if defined inside handle, error is thrown, b/c endLoadingIdx has to be effectively final
					//which can't be because we have two possible values for it
					int startReplacementIndex = ((endLoadingIndex+1)%bufferSize);
					loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							ArrayList<ImageView> newBuffer = (ArrayList<ImageView>) loadingTask.getValue();
							
							ArrayList<ImageView> oldBuffer = FramesBufferController.getBuffer();
							int currPtr = startReplacementIndex;
							while(currPtr!= (startLoadingIndex%bufferSize)) {
								//loop from after end to start index (on all the null values of the newBuffer)
								newBuffer.set(currPtr, oldBuffer.get(currPtr));
								currPtr = ((currPtr+1)%bufferSize);
							}
							
							FramesBufferController.setBuffer((ArrayList<ImageView>) newBuffer);
							FramesGlider.setCurrBuffPtr((startLoadingIndex%bufferSize));
							FramesGlider.setStopPtr(-1); //reset anyways, the anim loop will set it based on lli  
							//last loaded frame had a value before resetting the buffer
							//we replaced N images from the old buffer where N == diff
							//thus, ignoring them from the buffer that already existed, and now we need to set
							//lastloaded to lastloaded - diff; to be able to replace correctly the next time
							//the buffer is accessed
							//the most general for both cases: (lastLoaded == totalFrames / lastLoaded != totalFrames) 
							//is to set it to currSlider + buffSize 
							//NOT lastloaded - diff
							
							//REMEMBER: lastLoadedFrame is basically used when we replace!
							//so the value of the replacement would be the currSliderV + bufferSize
							//because we will display CurrSliderV, then replace it with the next index
							//that can occupy the same position in the buffer, which
							//will never be larger than totalFrames because if it were, it would mean
							//the currSlider value is within the last {BS} frames, and since we are seeking backward
							//then it must be that the last displayed was actually in the last {BS} frames as well
							//which is one of the base cases(4) (seeking fw and bw within the last {BS} frames)
							FramesGliderController.setLastLoadedFrame(currSliderV + bufferSize);

							System.out.println("last loaded frame: " + getLastLoadedFrame());
					        System.out.println("currbufferPtr: " + FramesGlider.getCurrBufferPtr());
					        System.out.println("========================");
							
							setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
							FramesGlider.playPauseTimer();
						}
						
					});
					MainController.addTask(loadingTask);
					MainController.execute(loadingTask);
					return;
				} else {
					System.out.println("BW-FULL PRELOADING");
					//we have to pre-load ALL frames in buffer
					int startLoadingIndex = currSliderV; 
					//-1 b/c LDI represents the frame to be displayed next which means the next frame is already loaded
					int endLoadingIndex = (currSliderV + bufferSize -1);
					if( (getLastLoadedFrame() == totalFrames) &&
							(endLoadingIndex >= (totalFrames - bufferSize -1) 
								&& endLoadingIndex < totalFrames)) {
						//if we were only displaying the last {BS} frames before seeking backwards
						//&& endLoadingIndex is in the range of the last {BS} frames, make it only until
						//the index just before the {BS} frames, b/c the last {BS} frames would be 
						//already loaded
						endLoadingIndex = (totalFrames - bufferSize -1);
					}
					
					Task loadingTask = new GenericLoadTask(startLoadingIndex, 
							endLoadingIndex, bufferSize);
					System.out.println( "startIdx: " + startLoadingIndex + " /endIdx: " + endLoadingIndex);
					
					int startReplacementIndex = ((endLoadingIndex+1)%bufferSize);
					loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							ArrayList<ImageView> newBuffer = (ArrayList<ImageView>) loadingTask.getValue();
							ArrayList<ImageView> oldBuffer = FramesBufferController.getBuffer();
							
							int currPtr = startReplacementIndex;
							while(currPtr!= (startLoadingIndex%bufferSize)) {
								//loop from after end to start index (on all the null values of the newBuffer)
								newBuffer.set(currPtr, oldBuffer.get(currPtr));
								currPtr = ((currPtr+1)%bufferSize);
							}
							
							
							FramesBufferController.setBuffer((ArrayList<ImageView>) newBuffer);
							FramesGlider.setCurrBuffPtr((startLoadingIndex%bufferSize));
							FramesGlider.setStopPtr(-1); 
							//REMEMBER: lastLoadedFrame is basically used when we replace!
							//so the value of the replacement would be the currSliderV + bufferSize
							//because we will display CurrSliderV, then replace it with the next index
							//that can occupy the same position in the buffer, which
							//will never be larger than totalFrames because if it were, it would mean
							//the currSlider value is within the last {BS} frames, 
							//and since we are seeking backwards, then it must be that the 
							//last displayed was actually in the last {BS} frames as well
							//given that the diff >= bufferSize
							
							FramesGliderController.setLastLoadedFrame(startLoadingIndex + bufferSize);

							System.out.println("last loaded index: " + getLastLoadedFrame());
							System.out.println("currbufferPtr: " + FramesGlider.getCurrBufferPtr());
							System.out.println("========================");
							setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
							FramesGlider.playPauseTimer();
						}
					});
					MainController.addTask(loadingTask);
					MainController.execute(loadingTask);
					return;
				}
			} 
//else { //EQUAL CASE IS HANDLED AS THE FIRST BASE CASE
//			//2)Seek FW
//				//now represents number of frames we need to ignore 
//				int diff = currSliderV - lastDisplayedIndexBeforeDragging;
//				if(diff < bufferSize) { //partially load
//					//IT MAY BE THAT WHEN WE LOAD THE 2ND PART OF THE NEW BUFFER, WE REACH TOTAL FRAMES
//					if((currSliderV+bufferSize) >= totalFrames) {
//						System.out.println("FW: C1: Partial Loading till the last {BS} Frames=====");
//						int startLoadingIdx = (currSliderV + bufferSize - diff); 
//						int endLoadingIdx = (totalFrames -1);
//						Task loadingTask = new GenericLoadTask(startLoadingIdx, endLoadingIdx, bufferSize);
//						System.out.println( "startIdx: " + startLoadingIdx + " /endIdx: " + endLoadingIdx);
//						
//						loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//							@Override
//							public void handle(WorkerStateEvent event) {
//								ArrayList<ImageView> newBuffer = (ArrayList<ImageView>) loadingTask.getValue();
//								ArrayList<ImageView> oldBuffer = FramesBufferController.getBuffer();
//								int currPtr = ((endLoadingIdx+1)%bufferSize);
//								while(currPtr!= (startLoadingIdx%bufferSize)) {
//									//loop from after end to start index (on all the null values of the newBuffer)
//									newBuffer.set(currPtr, oldBuffer.get(currPtr));
//									currPtr = ((currPtr+1)%bufferSize);
//								}
//								
//								FramesBufferController.setBuffer((ArrayList<ImageView>) newBuffer);
//								FramesGlider.setCurrBuffPtr((currSliderV%bufferSize));
//								if(currSliderV+bufferSize == totalFrames) {
//									FramesGlider.setStopPtr(-1);
//								} else {
//									FramesGlider.setStopPtr((totalFrames%bufferSize)); 
//								}
//
//								FramesGliderController.setLastLoadedFrame(totalFrames);
//								setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
//								
//								System.out.println("last loaded index: " + getLastLoadedFrame());
//								System.out.println("currbufferPtr: " + FramesGlider.getCurrBufferPtr());
//								System.out.println("stopPtr: " + FramesGlider.getStopPtr());
//								System.out.println("========================");
//
//								FramesGlider.playPauseTimer();
//							}
//						});
//						MainController.addTask(loadingTask);
//						MainController.execute(loadingTask);
//						return;
//
//					} else {
//						System.out.println("FW: C2: Partial Loading but not till the end=====");
//
//						int startLoadingIdx = (currSliderV + bufferSize - diff); 
//						int endLoadingIdx = (currSliderV + bufferSize -1);
//						Task loadingTask = new GenericLoadTask(startLoadingIdx, endLoadingIdx, bufferSize);
//						System.out.println( "startIdx: " + startLoadingIdx + " /endIdx: " + endLoadingIdx);
//						
//						loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//							@Override
//							public void handle(WorkerStateEvent event) {
//								ArrayList<ImageView> newBuffer = (ArrayList<ImageView>) loadingTask.getValue();
//								ArrayList<ImageView> oldBuffer = FramesBufferController.getBuffer();
//								int currPtr = ((endLoadingIdx+1)%bufferSize);
//								while(currPtr!= (startLoadingIdx%bufferSize)) {
//									//loop from after end to start index (on all the null values of the newBuffer)
//									newBuffer.set(currPtr, oldBuffer.get(currPtr));
//									currPtr = ((currPtr+1)%bufferSize);
//								}
//								
//								FramesBufferController.setBuffer((ArrayList<ImageView>) newBuffer);
//								FramesGlider.setCurrBuffPtr((currSliderV%bufferSize));
//								FramesGlider.setStopPtr(-1); 
//								FramesGliderController.setLastLoadedFrame(endLoadingIdx+1);
//								setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
//
//								System.out.println("last loaded index: " + getLastLoadedFrame());
//								System.out.println("currbufferPtr: " + FramesGlider.getCurrBufferPtr());
//								System.out.println("stopPtr: " + FramesGlider.getStopPtr());
//								System.out.println("========================");
//								
//								FramesGlider.playPauseTimer();
//							}
//						});
//						MainController.addTask(loadingTask);
//						MainController.execute(loadingTask);
//						return;
//
//					}
//				} else {//we have to load everything
//					if((currSliderV+bufferSize) >= totalFrames) {
//						System.out.println("FW: C3: Full Loading till the last {BS} Frames=====");
//						int startLoadingIdx = currSliderV; 
//						int endLoadingIdx = totalFrames -1; 
//						Task loadingTask = new GenericLoadTask(startLoadingIdx, endLoadingIdx, bufferSize);
//						System.out.println( "startIdx: " + startLoadingIdx + " /endIdx: " + endLoadingIdx);
//
//						loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//							@Override
//							public void handle(WorkerStateEvent event) {
//								ArrayList<ImageView> newBuffer = (ArrayList<ImageView>) loadingTask.getValue();
//								
//								FramesBufferController.setBuffer((ArrayList<ImageView>) newBuffer);
//								FramesGlider.setCurrBuffPtr((currSliderV%bufferSize));
//								//there is a particular case when currBufferPtr would be exactly equal to StopPtr
//								//in the case where we load the buffer fully and the currSliderValue is 
//								//equal to total-buffersize --> in this case; we don't set
//								//the stopPtr, it will be set anyways after displaying this frame
//								//to the correct value in the pauseTransition Loop body in FramesGlider
//								if(currSliderV+bufferSize == totalFrames) {
//									FramesGlider.setStopPtr(-1);
//								} else {
//									FramesGlider.setStopPtr((totalFrames%bufferSize)); 
//								}
//								
//								FramesGliderController.setLastLoadedFrame(totalFrames);
//								setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
//								
//								System.out.println("last loaded index: " + getLastLoadedFrame());
//								System.out.println("currbufferPtr: " + FramesGlider.getCurrBufferPtr());
//								System.out.println("stopPtr: " + FramesGlider.getStopPtr());
//								System.out.println("========================");
//
//								FramesGlider.playPauseTimer();
//
//							}
//						});
//						MainController.addTask(loadingTask);
//						MainController.execute(loadingTask);
//						return;
//					} else {
//						System.out.println("FW: C4: fully load buffer but not reach total frames=====");
//						int startLoadingIdx = currSliderV; 
//						int endLoadingIdx = (currSliderV + bufferSize -1); 
//						Task loadingTask = new GenericLoadTask(startLoadingIdx, endLoadingIdx, bufferSize);
//						System.out.println( "startIdx: " + startLoadingIdx + " /endIdx: " + endLoadingIdx);
//
//						loadingTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//							@Override
//							public void handle(WorkerStateEvent event) {
//								ArrayList<ImageView> newBuffer = (ArrayList<ImageView>) loadingTask.getValue();
//								
//								FramesBufferController.setBuffer((ArrayList<ImageView>) newBuffer);
//								FramesGlider.setCurrBuffPtr((currSliderV%bufferSize));
//								FramesGlider.setStopPtr(-1); 
//								FramesGliderController.setLastLoadedFrame(endLoadingIdx+1);
//								setLastDisplayedIndexBeforeDragging(-1);//reset for the next mouse press
//								
//								System.out.println("last loaded index: " + getLastLoadedFrame());
//								System.out.println("currbufferPtr: " + FramesGlider.getCurrBufferPtr());
//								System.out.println("stopPtr: " + FramesGlider.getStopPtr());
//								System.out.println("========================");
//
//								FramesGlider.playPauseTimer();
//
//							}
//						});
//						MainController.addTask(loadingTask);
//						MainController.execute(loadingTask);
//						return;
//					}
//				
//				}
//				
//			}
			
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
		FramesGliderController.lastDisplayedIndexBeforeDragging = lastDisBeforeDrag;
	}


	public static void setLastDisplayedIndexBeforePressing(int oldValue) {
		FramesGliderController.lastDisplayedIndexBeforePressing = oldValue;
	}
	
//	public static void setToBeDisplayedAfterPressing(int newValue) {
//		FramesGliderController.toBeDisplayedAfterPressing = newValue;
//	}
}
