/*
 * com.percussion.pso.jexl PSOBase64Codec.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.jexl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import org.apache.commons.codec.binary.Base64;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.tools.PSCopyStream;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class PSOBase64Codec extends PSJexlUtilBase implements IPSJexlExpression
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOBase64Codec.class);

   /**
    * 
    */
   public PSOBase64Codec()
   {
      super();
   }

   @IPSJexlMethod(description="Encode a binary property", 
         params={
        @IPSJexlParam(name="source", description="binary property to encode")})
   public String encode(Property jcrProperty) throws ValueFormatException, RepositoryException
   {
      if(jcrProperty == null)
      {
         log.debug("Property is null, no base64 encoding is possible" ); 
         return null; 
      }
      return encode(jcrProperty.getStream()); 
   }

   @IPSJexlMethod(description="Encode a binary stream", 
         params={
        @IPSJexlParam(name="source", description="binary stream to encode")})
   public String encode(InputStream stream)
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
      try
      {
         if(stream == null)
         {
            log.debug("No Stream found, cannot base64 encode ");
            return null;
         }
         PSCopyStream.copyStream(stream, baos);
      } catch (IOException ex)
      {
        // should never happen unless we run out of memory
        log.error("Unexpected exception " + ex.getMessage(), ex);
      } 
      return encode(baos.toByteArray());
   }
   
   @IPSJexlMethod(description="Encode a binary byte array", 
         params={
        @IPSJexlParam(name="source", description="binary byte array to encode")})
   public String encode(byte[] bytes)
   {
      byte[] out = Base64.encodeBase64(bytes);
      try
      {
         return new String(out,"ASCII"); //base64 strings are ASCII only
      } catch (UnsupportedEncodingException ex)
      {
         //ASCII is always supported, this should never happen 
         log.error("Unsupported Encoding " + ex.getMessage(), ex);
         return null;
      }
      
   }
}
