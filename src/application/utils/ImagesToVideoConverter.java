package application.utils;

/*******************************************************************************
 * Copyright (c) 2014, Art Clarke.  All rights reserved.
 *  
 * This file is part of Humble-Video.
 *
 * Humble-Video is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Humble-Video is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Humble-Video.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

import java.awt.image.BufferedImage;
import java.io.IOException;

//import io.humble.video.AudioChannel;
import io.humble.video.Codec;
import io.humble.video.Codec.ID;
import io.humble.video.Coder.State;
import io.humble.video.Encoder;
import io.humble.video.MediaAudio;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
//import io.humble.video.javaxsound.MediaAudioConverter;

/**
 * This is meant as a demonstration program to teach the use of the Humble API.
 * <p>
 * Concepts introduced:
 * </p>
 * <ul>
 * <li>Muxer: A {@link Muxer} object is a container you can write media data to.</li>
 * <li>Encoders: An {@link Encoder} object lets you convert {@link MediaAudio} or {@link MediaPicture} objects into {@link MediaPacket} objects
 * so they can be written to {@link Muxer} objects.</li>
 * </ul>
 * @author aclarke
 * 
 * Instead of screen recording, I take in images from the data source, create a bufferedImage
 * then pass it to the convertor and do the same encoding job done in the demo
 * 
 * Additionally, and as per the project requirement, I make an Audio and add it to the picture
 * 
 * @author carsoli
 * @since June, 2018
 *
 */
public class ImagesToVideoConverter {
  private static Rational framerate = null; // divide(1,fps)
  private static MediaPictureConverter videoConverter = null;
  private static Encoder videoEncoder;
  private static PixelFormat.Type pixelformat; 
  private static Muxer videoMuxer;
  private static MediaPicture picture;
  private static MediaPacket videoPacket;
    
  public static void initializeConvertor(String filename, String formatname,
	    String codecname, int fps, int width, int height) {
	  
	  framerate = Rational.make(1, fps);
	  videoMuxer = Muxer.make(filename, null, formatname);
	  
	  /* Now, we need to decide what type of codec to use to encode video. 
	   * Muxers have limited sets of codecs they can use. We're going to pick the 
	   * first one that works, or if the user supplied a codec name, we're going 
	   * to force-fit that in instead.
	  */
	    final MuxerFormat videoMuxerFormat = videoMuxer.getFormat();
	
//	    System.out.println("Muxer format: " + videoMuxerFormat);
//	    System.out.println("Muxer format name: "+ videoMuxerFormat.getLongName());      
	    final Codec videoCodec; 
	    
	    if (codecname != null) {
	      videoCodec = Codec.findEncodingCodecByName(codecname);
	    } else {
	      ID videoCodecID = videoMuxerFormat.getDefaultVideoCodecId();
	     
//	      System.out.println("Video CODEC ID:" + videoCodecID);
	      videoCodec = Codec.findEncodingCodec(videoCodecID);
//	      System.out.println("video codec info: "+ videoCodec.toString());
//	      System.out.println("===================");
	      
	    }
	    /**
	     * Now that we know what codec, we need to create an encoder
	     */
	    videoEncoder = Encoder.make(videoCodec);
//	    System.out.println("Video ENCODER: "+ videoEncoder.toString());

	    /**
	     * Video encoders need to know at a minimum:
	     *   width
	     *   height
	     *   pixel format
	     * Some also need to know frame-rate (older codecs that had a fixed rate at which video files could
	     * be written needed this). There are many other options you can set on an encoder, but we're
	     * going to keep it simpler here.
	     * encoder.setWidth/Height: transferred to the encodeImage and is changed per image
	     */
	    // We are going to use 420P as the format because that's what most video formats these days use
	    videoEncoder.setWidth(width);
	    videoEncoder.setHeight(height);
	    //color encoding for the human eye
	    pixelformat = PixelFormat.Type.PIX_FMT_YUV420P; 
	    videoEncoder.setPixelFormat(pixelformat);
	    videoEncoder.setTimeBase(framerate);
//	    System.out.println("VIDEO ENCODER AFTER INITIALIZING: "+ videoEncoder.toString());
//    	System.out.println("===================");
    	
	    /** An annoyance of some formats is that they need global (rather than per-stream) headers,
	     * and in that case you have to tell the encoder. And since Encoders are decoupled from
	     * Muxers, there is no easy way to know this beyond 
	     */
	    if (videoMuxerFormat.getFlag(MuxerFormat.Flag.GLOBAL_HEADER)) {
	    	System.out.println("Global header flag setting");
	    	videoEncoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
	    }

	    /** Open the encoder. */
	    videoEncoder.open(null, null);
	    
	    
	    /** Add this stream to the muxer. */
	    if(videoEncoder != null && videoEncoder.getState() == State.STATE_OPENED) {
	    	System.out.println("ENCODER'S STATE: OPEN");
//	    	System.out.println("===================");
	    	videoMuxer.addNewStream(videoEncoder);
	    }
	    
	    /** And open the muxer for business. */
	    try {
			videoMuxer.open(null, null);
		} catch (InterruptedException e1) {
	    	System.out.println("Exception while opening muxer");
			e1.printStackTrace();
		} catch ( IOException e2) {
			System.out.println("Exception while opening muxer");
			e2.printStackTrace();
		}
	  /* 
	   	 Next, we need to make sure we have the right MediaPicture format objects
	     to encode data with. Java (and most on-screen graphics programs) use some
	     variant of Red-Green-Blue image encoding (a.k.a. RGB or BGR). Most video
	     CODECs use some variant of YCrCb formatting. So we're going to have to
	     convert. To do that, we'll introduce a MediaPictureConverter object later. object.
	  */
	    if(videoEncoder.getWidth() > 0 && videoEncoder.getHeight() > 0
	    		&& pixelformat != PixelFormat.Type.PIX_FMT_NONE) {
	    	
//	    	System.out.println("MEDIA PIC IN THE MAKING");
//	    	System.out.println("======================");
	    	picture = MediaPicture.make(
	    			videoEncoder.getWidth(),
	    			videoEncoder.getHeight(),
	    			pixelformat);
	    	picture.setTimeBase(framerate);
//	    	System.out.println("PICTURE: " + picture.toString());
//	    	System.out.println("======================");
	    }
	    /* We're going to encode and then write out any resulting packets. */
	    videoPacket = MediaPacket.make();
  }
  
  public static void encodeImageFile(BufferedImage readBufferedImage, long frameIndex){
      final BufferedImage bufferedImage = convertToType(readBufferedImage, BufferedImage.TYPE_3BYTE_BGR);
      
      if (videoConverter == null) {
    	  try {
    	      /** This is LIKELY not in YUV420P format, so we're going to 
    	       * convert it using some handy utilities. */
    		  videoConverter = MediaPictureConverterFactory.createConverter(bufferedImage, picture);
    	  } catch (UnsupportedOperationException e1) {
    		  System.out.println("Unsupported Operation Exception when creating convertor");
    		  e1.printStackTrace();
    	  }
      }
	  try {
		  videoConverter.toPicture(picture, bufferedImage, frameIndex);
//		  System.out.println("toPicture works, index: " + frameIndex);
	  } catch ( IllegalArgumentException e2) {
		  System.out.println("Illegal argument exception when calling convertor.ToPicture");
		  e2.printStackTrace();
	  }

      if(videoPacket != null && picture != null) {
    	  //c: there must be a better implementation, with a listener and a callback fn?????
    	  do {
    		  videoPacket = MediaPacket.make();
//    		  System.out.println("logging pckt size:" + packet.getSize());
    		  videoEncoder.encode(videoPacket, picture);
    		  if (videoPacket.isComplete()){
    			  try {
    				  videoMuxer.write(videoPacket, false);
//    				  System.out.println("Successfully wrote packet to muxer");
    			  } catch (RuntimeException e) {
    				  System.out.println("RT Exception while writing packet to muxer");
    				  e.printStackTrace();
    			  }   
    		  }
    	  } while (videoPacket.isComplete()); 
      }
  }
  
  public static void EncodeVideoAndClose() {
//	  System.out.println("flushing and closing");
//	  System.out.println("=======================");
	  /** Encoders, like decoders, sometimes cache pictures so it can do the right key-frame optimizations.
	     * So, they need to be flushed as well. As with the decoders, the convention is to pass in a null
	     * input until the output is not complete.
	     */
	    do {
	      videoEncoder.encode(videoPacket, null);
	      if (videoPacket.isComplete())
	        videoMuxer.write(videoPacket,  false);
	    } while (videoPacket.isComplete());
	    
	    /** Finally, let's clean up after ourselves. */
	    videoMuxer.close();
	    
  }
    
  /**
   * Convert a {@link BufferedImage} of any type, to {@link BufferedImage} of a
   * specified type. If the source image is the same type as the target type,
   * then original image is returned, otherwise new image of the correct type is
   * created and the content of the source image is copied into the new image.
   * 
   * @param sourceImage
   *          the image to be converted
   * @param targetType
   *          the desired BufferedImage type
   * 
   * @return a BufferedImage of the specifed target type.
   * 
   * @see BufferedImage
   */
  private static BufferedImage convertToType(BufferedImage sourceImage, int targetType){
    BufferedImage image;
    // if the source image is already the target type, return the source image
    if (sourceImage.getType() == targetType) {
    	image = sourceImage;
    } else {
    /*otherwise create a new image of the target type and draw the new image*/
      image = new BufferedImage(sourceImage.getWidth(),
          sourceImage.getHeight(), targetType);
      image.getGraphics().drawImage(sourceImage, 0, 0, null);
    }
    return image;
  }

}