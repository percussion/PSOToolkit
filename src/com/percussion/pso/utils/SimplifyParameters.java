/*
 * com.percussion.pso.utils SimplifyParameters.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class SimplifyParameters
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(SimplifyParameters.class);
   
   /**
    * Static methods only
    */
   private SimplifyParameters()
   {
   }
   
   public static Map<String,String> simplifyMap(Map<String,Object> input)
   {
      Map<String,String> outMap = new LinkedHashMap<String,String>();
      
      for(Map.Entry<String,Object> entry : input.entrySet())
      {
         String key = entry.getKey();
         Object value = entry.getValue();
         log.trace("Entry Name " + key + " value type " + value.getClass().getCanonicalName()); 
         String sval = simplifyValue(value); 
         outMap.put(key, sval); 
      }
      return outMap; 
   }
   
   public static String simplifyValue(Object value)
   {
      if(value == null)
      {
         log.debug("null value"); 
         return null; 
      }
      String sval; 
      if(value instanceof String[])
      {
         String[] x = (String[])value; 
         if(x.length == 0)
         {
            log.trace("Empty String array"); 
            return ""; 
         }
         sval = x[0]; 
         log.trace("Converted String[] to " + sval + " " + value);
      }
      else if(value instanceof List)
      {
         List x = (List)value;
         if(x.size() == 0)
         {
            log.debug("Empty List"); 
            return ""; 
         }
         sval = x.get(0).toString(); 
         log.trace("Converted List to " + sval + " " + value); 
      }
      else
      {
         sval = value.toString(); 
         log.trace("Converted Object to " + sval); 
      }
      return sval;
   }
}
