/*******
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.utils FileItemDataSource.java
 *
 */
package com.percussion.pso.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import org.apache.commons.fileupload.FileItem;

/**
 * A thin wrapper for a FileItem as a DataSource. 
 * 
 * @author DavidBenua
 * @see javax.activation.DataSource
 * @see org.apache.commons.fileupload.FileItem
 */
public class FileItemDataSource implements DataSource
{
   /**
    * the wrapped file item. 
    */
   private FileItem item = null; 
   
   /**
    * Sole Constructor. 
    * @param item the file item to be wrapped. 
    * Must not be <code>null</code>. 
    */
   public FileItemDataSource(FileItem item)
   {
      this.item = item; 
   }
   
   /**
    * Gets the file data as a stream.
    * @return the stream  
    * @see javax.activation.DataSource#getInputStream()
    */
   public InputStream getInputStream() throws IOException
   {
       return item.getInputStream(); 
   }
   /**
    * Gets a stream for writing the data.
    * @return the stream. 
    * @see javax.activation.DataSource#getOutputStream()
    */
   public OutputStream getOutputStream() throws IOException
   {
      return item.getOutputStream();       
   }
   /**
    * Gets the MIME content type of this file.
    * @return the content type.  
    * @see javax.activation.DataSource#getContentType()
    */
   public String getContentType()
   {
      return item.getContentType(); 
   }
   /**
    * Gets the file name. 
    * @return the file name. 
    * @see javax.activation.DataSource#getName()
    */
   public String getName()
   {     
      return item.getName(); 
   }
}
