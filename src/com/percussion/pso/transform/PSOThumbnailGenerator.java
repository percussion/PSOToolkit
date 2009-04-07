/***
 * com.percussion.pso.transform PSOThumbnailGenerator.java
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 */
package com.percussion.pso.transform;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.extensions.general.PSFileInfo;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.PSPurgableTempFile;


/**
 * Generates a fixed size thumbnail from an uploaded image. 
 * 
 * <p>Title: PSOThumbnailGenerator </p>
 * <p>Description: A Pre-exit that can be used on an Image Content editor to automatically
 * generate a thumbnail for the full image.
 * </p>
 * Modernized to use the ImageIO libraries. The new libraries use biCubic interpolation for 
 * higher image quality.
 * <p>
 * This version is enhanced to handle both PSPurgableTempFile instances and Base64 encoded
 * strings.  
 * <p>Copyright: Copyright (c) 2002, 2008</p>
 * <p>Company: Percussion Software </p>
 * @author Prasad Bandaru
 * @author DavidBenua
 * @version 2.0
 */
public class PSOThumbnailGenerator extends PSFileInfo
   implements IPSItemInputTransformer, IPSRequestPreProcessor {
 
   private static Log log = LogFactory.getLog(PSOThumbnailGenerator.class); 
   
   /**
    * Controls the size of steps by which the image is rescaled. 
    * This value should always be a power of 2 (2,4,8...). Larger values will
    * increase performance, but add sampling noise to the compressed image. 
    * Defaults to 2. 
    */
   private int stepFactor = 2;
   
   /**
    * Maximum image size where interpolation is used.  For images larger
    * than this size, the image pixels are not interpolated: the "nearest 
    * pixel" algorithm is used instead. Increasing the value may increase
    * image quality for images that are smaller than the specified size. 
    * It will also decrease performance, sometimes significantly. 
    * Defaults to 1,000,000 pixels.  
    */
   private int maxInterpolationSize = 1000000; 

   private String imageFormat = "jpeg";

   private float compression = 0.85f;
   public PSOThumbnailGenerator() {
   }

 
   /**
    * Pre processes the current request to generate the thumbnail automatically
    * The exit should be registered with 4 parameters. <p>
    * 1. SourceFieldName - Field name of the source image <BR>
    * 2. ThumbFieldName - Field name of the thumbnail image <BR>
    * 3. MaxDimension - Value of the Max Dimension.
    * 4. ThumbPrefix - Prefix added to the full size image name so that the filename
    * of the generated thumbnail is PREFIFX + FullSizeImageName
    * 5. ThumbWidth - Specified width, overrides MaxDimension. 
    * 6. ThumbHeight - Specified height, overrides MaxDimention
    * <p>
    * If both width and height are specified, then the image will squashed to those 
    * dimensions.  If only one is specified, the aspect ratio of the original image
    * will be used to compute the desired image size. If neither height nor width is specified, 
    * then MaxDimension will be used instead.  
    * </p>
    * @param params - parameters
    * @param request - IPSRequestContext
    * @throws PSAuthorizationException
    * @throws PSRequestValidationException
    * @throws PSParameterMismatchException
    * @throws PSExtensionProcessingException
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException, PSRequestValidationException,
             PSParameterMismatchException, PSExtensionProcessingException
   {
      if (params == null || params.length < 4)
          throw new PSParameterMismatchException("Required Parameters are missing");

      String emsg = "";
      
      int height = 0;
      int width = 0; 
      int maxDimension = 0; 
      String sourceFieldName = params[0].toString();
      if(StringUtils.isBlank(sourceFieldName))
      {
         emsg = "Source Field is required"; 
         log.error(emsg); 
         throw new IllegalArgumentException(emsg); 
      }
      String thumbFieldName = params[1].toString();
      if(StringUtils.isBlank(thumbFieldName))
      {
         emsg = "Thumbnail Field is required"; 
         log.error(emsg); 
         throw new IllegalArgumentException(emsg);
      }
      String maxD =params[2].toString(); 
      if(StringUtils.isNotBlank(maxD))
      {
         maxDimension = Integer.parseInt(maxD);
      }
      
      String thumb_prefix = params[3].toString();
      if(StringUtils.isBlank(thumb_prefix))
      {
         emsg = "Thumbnail prefix is required"; 
         log.error(emsg); 
         throw new IllegalArgumentException(emsg);
      }
      if(params.length > 4)
      {
         String thumbWidth = params[4].toString(); 
         if(StringUtils.isNotBlank(thumbWidth))
         {
            width = Integer.parseInt(thumbWidth); 
         }
      }
      if(params.length > 5)
      {
         String thumbHeight  = params[5].toString();
         if(StringUtils.isNotBlank(thumbHeight))
         {
            height = Integer.parseInt(thumbHeight); 
         }
      }
      try
      {
    	  Object obj = request.getParameterObject(sourceFieldName);
    	  if(obj != null && obj instanceof PSPurgableTempFile){
    		  PSPurgableTempFile temp = (PSPurgableTempFile) obj;
    		  long fileSize = temp.length();
    		  if ( fileSize > 0 ) 
    		  {
    			  String parent_mimetype =  temp.getSourceContentType();
    			  String source_filename =  temp.getSourceFileName();
    			  temp.deleteOnExit();
    			  InputStream imageStream = new FileInputStream(temp); 

    			  PSPurgableTempFile thumbFile = makeThumbFile(imageStream, source_filename, thumb_prefix, 
    					  parent_mimetype, maxDimension, width, height);            

    			  if(thumbFile != null)
    			  {
    				  request.setParameter(thumbFieldName, thumbFile);
    			  }
    		  }
    	  }
         else{
            if (obj!=null && obj instanceof String)
            {
               String imageStr = (String) obj; 
               byte[] imageBytes = imageStr.getBytes();  
               String parent_mimetype = request.getParameter(sourceFieldName + "_type"); 
               String source_filename = request.getParameter(sourceFieldName + "_filename"); 
               if(imageBytes.length >0 && Base64.isArrayByteBase64(imageBytes) &&
                     StringUtils.isNotBlank(parent_mimetype) && StringUtils.isNotBlank(source_filename))
               {  
                  InputStream imageStream = new ByteArrayInputStream(Base64.decodeBase64(imageBytes));
                  PSPurgableTempFile thumbFile = makeThumbFile(imageStream, source_filename, thumb_prefix, 
                        parent_mimetype, maxDimension, width, height);            

                  if(thumbFile != null)
                  {
                        request.setParameter(thumbFieldName, thumbFile);
                        request.setParameter(thumbFieldName + "_type", thumbFile.getSourceContentType()); 
                        request.setParameter(thumbFieldName + "_filename", StringUtils.substringBeforeLast(thumbFile.getName(),".")); 
                        request.setParameter(thumbFieldName + "_ext", ".JPG");
                  }                  
                  else
                  {
                     emsg = "error processing thumbnail for " + source_filename;
                     log.error(emsg); 
                  }
               }
               else
               {
                  log.info("Unable to process thumbnail from base64 encoded String"); 
                  log.info("image file name is " + source_filename); 
                  log.info("mimetype is " + parent_mimetype);
                  log.info("data starts with " + StringUtils.substring(imageStr,0, 50)); 
               }
            }
         }
      } catch (Exception ex)
      {
         log.error("Unexpected Exception processing thumbnail " + ex,ex);
      }
      super.preProcessRequest(params, request);
   }

   /**
    * Makes a thumbnail file from a byte stream.  
    * @param imageStream the image as a byte stream. 
    * @param source_filename the file name from the source image upload. 
    * @param thumb_prefix the prefix to be applied to the thumbnail file.
    * @param parent_mimetype the content type of the image data. This must contain
    * the word "image". 
    * @param maxDimension the maximum size of the thumbnail.  
    * @param width the desired width of the thumbnail. 
    * @param height the desired height of the thumbnail. 
    * @return the thumbnail as a file.  Will be <code>null</code> if any errors
    * occur.  
    */
   public PSPurgableTempFile makeThumbFile(InputStream imageStream, String source_filename, String thumb_prefix,
         String parent_mimetype, int maxDimension, int width, int height )
   {
      log.debug("processing file " + source_filename);
      String fullImageFileName = StringUtils.contains(source_filename, File.separator) ? 
            StringUtils.substringAfterLast(source_filename, File.separator) : source_filename;
            
      String thumb_filename = thumb_prefix + fullImageFileName;
      if (parent_mimetype.indexOf("image") >= 0)
      {
         try{
            //String thumb_ContentType = temp.getSourceContentType();
            PSPurgableTempFile thumb_temp = new PSPurgableTempFile(thumb_filename,
                                                "", null, thumb_filename,
                                                "image/jpeg", null);
            thumb_temp.deleteOnExit();
            OutputStream thumbStream = new FileOutputStream(thumb_temp);
            
            createThumbnail(thumbStream, imageStream, maxDimension, width, height);
            logMessage("Size of the thumbnail created = " +
                       thumb_temp.length(), null);
           return thumb_temp; 
         }
         catch(IOException ioe){
            log.error("unable to create thumbnail " + thumb_filename, ioe);            
         }
      }
      else{
         log.info("The uploaded file is not an image, type = " +
            parent_mimetype);
      }
      return null;
   }
   /**
    * Reads an image in a file and creates a thumbnail in another file. 
    * Modified slightly to use ImageIO instead of Sun JPEG CODEC.  
    * @param outstream the thumbnail image as a byte stream.  This image will
    * always be coded as a JPEG. 
    * @param instream the source image as a byte stream. 
    * @param maxDim The width and height of the thumbnail must be maxDim pixels or less.
    */
   protected void createThumbnail(OutputStream outstream, InputStream instream,  int maxDim, int width, int height) throws IOException
   {
      try {
         // Get the image from a file.
         BufferedImage inImage = ImageIO.read(instream);
         // Determine the scale.
         Dimension originalSize = new Dimension(inImage.getWidth(), inImage.getHeight()); 
         
         Dimension outSize = computeSize(maxDim, width, height, originalSize);
         
         while(inImage.getHeight() > outSize.height*stepFactor || inImage.getWidth() > outSize.width*stepFactor)
         {
            inImage = halfImage(inImage);
         }
         
         
         // Create an image buffer in which to paint on.
         int imageType = (inImage.getTransparency() == Transparency.OPAQUE) ? 
               BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
         BufferedImage outImage = new BufferedImage(outSize.width, outSize.height, imageType);
         
         // Paint image.
         Graphics2D g2d = outImage.createGraphics();
         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
               RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         g2d.drawImage(inImage, 0, 0, outSize.width, outSize.height, null);
         g2d.dispose();

         Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(imageFormat);
         ImageWriter iw = iter.next(); 
         ImageWriteParam iwp = iw.getDefaultWriteParam();
         iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
         
         iwp.setCompressionQuality(compression); 
         
         MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(outstream);
         iw.setOutput(mcios); 
         iw.write(null, new IIOImage(outImage, new ArrayList<BufferedImage>(), null), iwp); 
    
         outstream.flush();
      }
      catch (IOException e) {
         log.error("Could not create thumbnail " + e.getMessage(), e);
      }
   }

   /**
    * Computes the size of the thumbnail image based on parameters specified. 
    * If height and width are both specified, these values define the size directly. 
    * If only one is specified, then the aspect ration of the original image is used to 
    * find the other value. 
    * If height and width are both 0, then the maxDim parameter is used with Aspect
    * ratio of the original image to find the thumbnail dimensions. 
    * @param maxDim maximum dimension 
    * @param thumbWidth desired width
    * @param thumbHeight desired height
    * @param originalSize size of the original image. 
    * @return the size of the thumbnail. 
    */
   protected Dimension computeSize(int maxDim, int thumbWidth, int thumbHeight, Dimension originalSize)
   {
      int width = 0;
      int height = 0;
      String emsg;
      if(thumbWidth > 0 && thumbHeight > 0 )
      {
         log.debug("using specified size " + thumbWidth + " w " + thumbHeight + " h "); 
         return new Dimension(thumbWidth, thumbHeight); 
      }
      double aspect = originalSize.getWidth() / originalSize.getHeight(); 
      if(thumbWidth > 0)
      {
         width = thumbWidth; 
         height = Long.valueOf(Math.round(width / aspect)).intValue(); 
         return new Dimension(width, height); 
      }
      if(thumbHeight > 0)
      {
         height = thumbHeight; 
         width = Long.valueOf(Math.round(height * aspect)).intValue(); 
         return new Dimension(width, height); 
      }
      //if we get here, neither height nor width was specified. 
      if(maxDim == 0)
      {
         emsg = "at least one of height, width and maxdim must be specified"; 
         log.error(emsg);
         throw new IllegalArgumentException(emsg); 
      }
      if(aspect > 1.0)
      { // the image is wider than it is high
         width = maxDim; 
         height = Long.valueOf(Math.round(width / aspect)).intValue();
      }
      else
      {
         height = maxDim; 
         width = Long.valueOf(Math.round(height * aspect)).intValue();
      }
      return new Dimension(width, height); 
   }
   
   /**
    * Scales an image in half, both vertically and horizontally
    * @param inImage the original image
    * @return the scaled image. Never <code>null</code>
    */
   protected BufferedImage halfImage(BufferedImage inImage)
   {
      long timer = System.currentTimeMillis(); 
      int height = inImage.getHeight() / stepFactor;
      int width = inImage.getWidth() / stepFactor; 
      log.debug("Scaling to image height " + height + " width " + width );
      int imageType = (inImage.getTransparency() == Transparency.OPAQUE) ? 
              BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
      BufferedImage halfImage = new BufferedImage(width, height, imageType);
      Graphics2D half = halfImage.createGraphics();
      if((height * width) < maxInterpolationSize)
      {
      log.debug("using bilinear interpolation");    
      half.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
             RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
      }
      /*half.setRenderingHint(RenderingHints.KEY_RENDERING, 
            RenderingHints.VALUE_RENDER_QUALITY); */ 
      /* half.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, 
            RenderingHints.VALUE_COLOR_RENDER_QUALITY); */
      half.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON);
      half.setRenderingHint(RenderingHints.KEY_DITHERING, 
            RenderingHints.VALUE_DITHER_ENABLE); 
      
      half.drawImage(inImage, 0, 0, width, height, 0, 0, inImage.getWidth(), inImage.getHeight(), null);
      if(log.isDebugEnabled())
      {
         long timestop = System.currentTimeMillis();
         long elapsed = timestop - timer; 
         log.debug("Time elapsed is " + elapsed);
      }      
      half.dispose();
      return halfImage;
   }
 
   
   
   /**
    * @see com.percussion.extension.PSDefaultExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   @Override
   public void init(IPSExtensionDef extDef, File initFile)
         throws PSExtensionException
   {
      super.init(extDef, initFile);
      String comp = extDef.getInitParameter(PARAM_BASE + ".compression");
      if(StringUtils.isNotBlank(comp))
      {
         float compression = Float.parseFloat(comp);
         log.debug("setting compression " + compression); 
         setCompression(compression); 
      }
      String iformat = extDef.getInitParameter(PARAM_BASE + ".imageFormat"); 
      if(StringUtils.isNotBlank(iformat))
      {
         log.debug("setting image format: " + iformat); 
         setImageFormat(iformat);    
      }
      String maxInter = extDef.getInitParameter(PARAM_BASE + ".maxInterpolationSize");
      if(StringUtils.isNotBlank(maxInter))
      {
         int interpSize = Integer.parseInt(maxInter); 
         log.debug("setting max interpolation size: " + interpSize); 
         setMaxInterpolationSize(interpSize);  
      }
      String stepStr = extDef.getInitParameter(PARAM_BASE + ".stepFactor");
      if(StringUtils.isNotBlank(stepStr))
      {
         int step = Integer.parseInt(stepStr); 
         log.debug("setting step size: " + step); 
         setStepFactor(step);  
      }
      
   }

   private static final String PARAM_BASE = "com.percussion.pso.transform.PSOThumbnailGenerator";  
   private void logMessage(String msg, IPSRequestContext req){
      log.info(msg);
      if(req != null)
      {
         req.printTraceMessage(msg);
      }
   }


   /**
    * @return the stepFactor
    */
   public int getStepFactor()
   {
      return stepFactor;
   }


   /**
    * @param stepFactor the stepFactor to set
    */
   public void setStepFactor(int stepFactor)
   {
      this.stepFactor = stepFactor;
   }


   /**
    * @return the maxInterpolationSize
    */
   public int getMaxInterpolationSize()
   {
      return maxInterpolationSize;
   }


   /**
    * @param maxInterpolationSize the maxInterpolationSize to set
    */
   public void setMaxInterpolationSize(int maxInterpolationSize)
   {
      this.maxInterpolationSize = maxInterpolationSize;
   }


   /**
    * @return the imageFormat
    */
   public String getImageFormat()
   {
      return imageFormat;
   }


   /**
    * @param imageFormat the imageFormat to set
    */
   public void setImageFormat(String imageFormat)
   {
      this.imageFormat = imageFormat;
   }


   /**
    * @return the compression
    */
   public float getCompression()
   {
      return compression;
   }


   /**
    * @param compression the compression to set
    */
   public void setCompression(float compression)
   {
      this.compression = compression;
   }
   
   
}