/*******
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.utils MultipartFileDataSource.java
 *  
 * @author DavidBenua
 */
package com.percussion.pso.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import org.springframework.web.multipart.MultipartFile;

/**
 * A thin wrapper for a MultipartFile as a DataSource. 
 *
 * @author DavidBenua
 * @see javax.activation.DataSource
 * @see org.springframework.web.multipart.MultipartFile
 */
public class MultipartFileDataSource implements DataSource
{
   /**
    * the multipart file
    */
   private MultipartFile file = null; 
  
   /**
    * Sole Constructor
    * @param file the multipart file to wrap in this datasource. 
    */
   public MultipartFileDataSource(MultipartFile file)
   {
      this.file = file;
   }
   
   /*
    * @see javax.activation.DataSource#getInputStream()
    */
   public InputStream getInputStream() throws IOException
   {
       return file.getInputStream();  
   }
   /*
    * @see javax.activation.DataSource#getOutputStream()
    */
   public OutputStream getOutputStream() throws IOException
   {
      throw new IOException("OutputStreams not supported");  
   }
   /*
    * @see javax.activation.DataSource#getContentType()
    */
   public String getContentType()
   {
      return file.getContentType(); 
   }
   /*
    * @see javax.activation.DataSource#getName()
    */
   public String getName()
   {     
      return file.getName(); 
   }
}
