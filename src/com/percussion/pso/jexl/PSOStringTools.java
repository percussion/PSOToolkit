/*
 * com.percussion.pso.jexl PSOStringTools.java
 *  
 * @author DavidBenua
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 */
package com.percussion.pso.jexl;

import java.util.Locale;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.i18n.PSI18nUtils;

/**
 * Tools for String manipulation. Just basics for now
 *
 * @author DavidBenua
 *
 */
public class PSOStringTools extends PSJexlUtilBase implements IPSJexlExpression
{
   /**
    * Default constructor. 
    */
   public PSOStringTools()
   {
   }
   
   
   /**
    * Gets a StringBuilder for use in concatenating Strings.  JEXL has does 
    * some funny type conversions, and this class forces everything to be
    * a String
    * @param value the initial value.
    * @return the StringBuilder.  Never <code>null</code>. 
    */
   @IPSJexlMethod(description="gets a StringBuilder for concatenating strings",
         params={@IPSJexlParam(name="value", description="initial value")})
   public StringBuilder getStringBuilder(String value)
   {
      return new StringBuilder(value);
   }

   /**
    * Gets an empty StringBuilder for use in concatenating Strings.  JEXL has does 
    * some funny type conversions, and this class forces everything to be
    * a String. 
    * @return the StringBuilder.  Never <code>null</code>. Always <code>empty</code> 
    */
   @IPSJexlMethod(description="gets a StringBuilder for concatenating strings",
         params={})
   public StringBuilder getStringBuilder()
   {
      return new StringBuilder();
   }
   
   /**
    * Gets a locale based on the string representation. 
    * @param locString the String. 
    * @return the target Locale object.  Will be <code>null</code> if the input is 
    * <code>null</code> or invalid.  
    */
   @IPSJexlMethod(description="gets a java.util.Locale based on the String representation",
         params={@IPSJexlParam(name="locString", description="locale as a String")})
   public Locale getLocale(String locString)
   {
      return PSI18nUtils.getLocaleFromString(locString); 
   }
}
