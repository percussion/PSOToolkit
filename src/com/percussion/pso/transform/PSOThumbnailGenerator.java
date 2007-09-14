package com.percussion.pso.transform;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

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
 * generates a fixed size thumbnail from an uploaded image. 
 * 
 * <p>Title: PSOThumbnailGenerator </p>
 * <p>Description: A Pre-exit that can be used on an Image Content editor to automatically
 * generate a thumbnail for the full image.
 * </p>
 * Modernized to use the ImageIO libraries. The new libraries use biCubic interpolation for 
 * higher image quality. 
 * <p>Copyright: Copyright (c) 2002, 2007</p>
 * <p>Company: Percussion Software </p>
 * @author Prasad Bandaru
 * @author DavidBenua
 * @version 1.0
 */

public class PSOThumbnailGenerator extends PSFileInfo
   implements IPSItemInputTransformer, IPSRequestPreProcessor {

   public PSOThumbnailGenerator() {
   }

   public void init(IPSExtensionDef parm1, File parm2)
      throws PSExtensionException
   {
   }


   /**
    * Pre processes the current request to generate the thumbnail automatically
    * The exit should be registered with 4 parameters. <p>
    * 1. SourceFieldName - Field name of the source image <BR>
    * 2. ThumbFieldName - Field name of the thumbnail image <BR>
    * 3. MaxDimension - Value of the Max Dimension.
    * 4. ThumbPrefix - Prefix added to the full size image name so that the filename
    * of the generated thumbnail is PREFIFX + FullSizeImageName
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

      String sourceFieldName = params[0].toString();
      String thumbFieldName = params[1].toString();
      int maxDimension = Integer.parseInt(params[2].toString());
      String thumb_prefix = params[3].toString();

      Object obj = request.getParameterObject(sourceFieldName);
      if(obj instanceof PSPurgableTempFile){
         PSPurgableTempFile temp = (PSPurgableTempFile) obj;
         String parent_mimetype =  temp.getSourceContentType();
         temp.deleteOnExit();
         String fullImagePath = temp.getAbsolutePath();
         String fullImageFileName = getFilename(temp.getSourceFileName(), File.separator);
         String thumb_filename = thumb_prefix + fullImageFileName;
         String tFileName = thumb_prefix + fullImageFileName;
         if (parent_mimetype.indexOf("image") >= 0)
         {
            try{
               String thumb_ContentType = temp.getSourceContentType();
               PSPurgableTempFile thumb_temp = new PSPurgableTempFile(tFileName,
                                                   "", null, thumb_filename,
                                                   thumb_ContentType, null);
               thumb_temp.deleteOnExit();
               createThumbnail(thumb_temp, fullImagePath, tFileName, maxDimension);
               logMessage("Size of the thumbnail created = " +
                          thumb_temp.length(), request);
               request.setParameter(thumbFieldName, thumb_temp);
            }
            catch(IOException ioe){
               ioe.printStackTrace();
            }
         }
         else{
            logMessage("The uploaded file is not an image, type = " +
               temp.getSourceContentType(), request);
         }
      }
      else{
         if (obj!=null)
            throw new PSParameterMismatchException("Parameter " + sourceFieldName +
               " is not a valid type. The parameter should be a sys_file control");
      }
      super.preProcessRequest(params, request);
   }

   /**
    * Reads an image in a file and creates a thumbnail in another file. 
    * Modified slightly to use ImageIO instead of Sun JPBG CODEC.  
    * @param thumbFile
    * @param orig The name of image file.
    * @param thumb The name of thumbnail file.
    * @param maxDim The width and height of the thumbnail must be maxDim pixels or less.
    */
   public void createThumbnail(PSPurgableTempFile thumbFile, String orig,
                               String thumb, int maxDim)
   {
      try {
         // Get the image from a file.
         BufferedImage inImage = ImageIO.read(new File(orig));
         // Determine the scale.
         double scale = (double)maxDim/(double)inImage.getHeight(null);
         if (inImage.getWidth() > inImage.getHeight())
         {
            scale = (double)maxDim/(double)inImage.getWidth(null);
         }
         // Determine size of new image.
         // One of them should equal maxDim.
         int scaledW = (int)(scale*inImage.getWidth(null));
         int scaledH = (int)(scale*inImage.getHeight(null));

         // Create an image buffer in which to paint on.
         BufferedImage outImage = new BufferedImage(scaledW, scaledH,
                                                    BufferedImage.TYPE_INT_RGB);
         
         // Paint image.
         Graphics2D g2d = outImage.createGraphics();
         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
               RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         g2d.drawImage(inImage, 0, 0, scaledW, scaledH, null);
         //g2d.dispose();

         // JPEG-encode the image and write to file.
         OutputStream os = new FileOutputStream(thumbFile);
         ImageIO.write(outImage, "jpeg", os); 
         os.close();
      }
      catch (IOException e) {
//         e.printStackTrace();
         System.out.println("Could not create thumbnail " + thumb + " for " + orig);
      }
   }


   /**
    * Returns the filename portion of the provided fully qualified path.
    *
    * @param fullPathname The full path of the file, assumed not
    * <code>null</code> or empty.
    *
    * @param pathSep The path separator to use, assumed not <code>null
    * </code>.
    *
    * @return The filename portion of the full path, based on the pathSep, or
    * the fullPathname if the provided pathSep is not found in the provided
    * fullPathname.  Never <code>null</code>, may be emtpy if the fullPathname
    * ends in the pathSep.
    */
   private String getFilename(String fullPathname, String pathSep)
   {
      String fileName = "";

      // add 1 to the index so that we do not include the separator in the
      // filename string
      int startOfFilename = fullPathname.lastIndexOf(pathSep) + 1;
      if (startOfFilename < fullPathname.length())
         fileName = fullPathname.substring(startOfFilename);

      return fileName;
   }

//   private void logMessage(String msg)
//   {
//      System.out.println(msg);
//   }

   private void logMessage(String msg, IPSRequestContext req){
      req.printTraceMessage(msg);
   }
}