/*
 * COPYRIGHT (c) 1999 - 2010 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.legacy PSOUserCommunityUDF.java
 *
 */
package com.percussion.pso.legacy;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.server.IPSRequestContext;

/**
 * A UDF to get the user community name. 
 * This function, which has previously been available 
 * only in JEXL, can now be accessed in an XML application. 
 *
 * @author davidbenua
 *
 */
public class PSOUserCommunityUDF extends PSSimpleJavaUdfExtension
      implements
         IPSUdfProcessor
{
   /**
    * Gets the user community from user session.  
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] arg0, IPSRequestContext arg1)
         throws PSConversionException
   {
      PSOObjectFinder finder = new PSOObjectFinder(); 
      return finder.getUserCommunity(); 
   }
}
