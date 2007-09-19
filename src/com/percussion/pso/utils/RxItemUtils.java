/*
 * com.percussion.pso.utils RxItemUtils.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.IPSItemAccessor;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class RxItemUtils
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(RxItemUtils.class);
   
   
   /**
    * Static methods only
    */
   private RxItemUtils()
   {
   }
   
   /**
    * Gets the value of a field, handling null or empty fields. 
    * @param item the item 
    * @param fieldName the field name
    * @return the value of the field. Never <code>null</code>
    * may be <code>empty</code>
    * @throws PSCmsException when value cannot be converted
    */
   public static String getFieldValue(IPSItemAccessor item, String fieldName) throws PSCmsException
   {
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {
         log.debug("no such field " + fieldName); 
         return EMPTY; 
      }
      IPSFieldValue value = fld.getValue(); 
      if(value == null)
      { 
         log.debug("field has no values " + fieldName); 
         return EMPTY;
      }
      return value.getValueAsString(); 
   }
   
   public static Number getFieldNumeric(IPSItemAccessor item, String fieldName) throws PSCmsException
   {
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {
         log.debug("no such field " + fieldName); 
         return ZERO; 
      }
      IPSFieldValue value = fld.getValue(); 
      if(value == null)
      { 
         log.debug("field has no values " + fieldName); 
         return ZERO;
      }
      String fval = value.getValueAsString();
      if(StringUtils.isBlank(fval))
      {
         return ZERO;
      }
      if(StringUtils.isNumeric(fval))
      {
         return new Integer(fval);
      }
      log.info("numeric field contains non numeric data " + fieldName + " - "  + fval);
      return ZERO;
   }
   
   public static Date getFieldDate(IPSItemAccessor item, String fieldName) throws PSCmsException
   {
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {
         log.debug("no such field " + fieldName); 
         return null; 
      }
      IPSFieldValue value = fld.getValue(); 
      if(value == null)
      { 
         log.debug("field has no values " + fieldName); 
         return null;
      }
      if(value instanceof PSDateValue)
      {
         return (Date)value.getValue(); 
      }
      log.warn("Date field is not a date " + fieldName + " - " + value.getValueAsString());
      return null;
   }
   
   public static List<String> getFieldValues(IPSItemAccessor item, String fieldName) throws PSCmsException
   {
       List<String> list = new ArrayList<String>(); 
       PSItemField fld = item.getFieldByName(fieldName);
       if(fld == null)
       {
          log.debug("no such field " + fieldName); 
          return list; 
       }
       Iterator<IPSFieldValue> values = fld.getAllValues();
       while(values.hasNext())
       {
          IPSFieldValue val = values.next();
          list.add(val.getValueAsString()); 
       }
       
       return list; 
   }
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, IPSFieldValue value)
   {
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {  
         String emsg = "no such field " + fieldName;  
         log.debug(emsg); 
         throw new IllegalArgumentException(emsg);  
      }
      fld.clearValues(); 
      fld.addValue(value);
   }
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, String textValue)
   {
      log.debug("setting field " + fieldName + " value " + textValue); 
      IPSFieldValue val = new PSTextValue(textValue); 
      setFieldValue(item, fieldName, val); 
   }
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, Date dateValue)
   {
      log.debug("setting field " + fieldName + " value " + dateValue); 
      IPSFieldValue val = new PSDateValue(dateValue);
      setFieldValue(item, fieldName, val);
   }
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, Number numbValue)
   {
      log.debug("setting field " + fieldName + " value " + numbValue); 
      String textValue = String.valueOf(numbValue); 
      IPSFieldValue val = new PSTextValue(textValue); 
      setFieldValue(item, fieldName, val); 
   }
   public static void setFieldValue(IPSItemAccessor item, String fieldName, InputStream streamValue)
   {
      IPSFieldValue val;
      try
      {
         val = new PSBinaryValue(streamValue);
         setFieldValue(item, fieldName, val);
      } catch (IOException ex)
      {
         //should never happen, this is really a byte array
         log.error("Unexpected IO Exception " + ex.getLocalizedMessage(), ex); 
      }
      
   }
   
   public static void setFieldValue(IPSItemAccessor item, String fieldName, List<String> listValue)
   {
      log.debug("setting mult-value field " + fieldName); 
      PSItemField fld = item.getFieldByName(fieldName);
      if(fld == null)
      {  
         String emsg = "no such field " + fieldName;  
         log.debug(emsg); 
         throw new IllegalArgumentException(emsg);  
      }
      fld.clearValues(); 
      for(String textValue : listValue)
      {
         log.debug("adding value " + textValue); 
         fld.addValue(new PSTextValue(textValue)); 
      }
   }
   
   private static final String EMPTY = "";
   private static final Number ZERO = new Integer(0); 
   
}
