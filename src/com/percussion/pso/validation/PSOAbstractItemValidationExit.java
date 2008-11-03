/*
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.validation PSOAbstractItemValidationExit.java
 *
 */
package com.percussion.pso.validation;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemValidator;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public abstract class PSOAbstractItemValidationExit
      implements
         IPSItemValidator,
         IPSResultDocumentProcessor
{
   private static Log log = LogFactory.getLog(PSOAbstractItemValidationExit.class); 
   
   private Document errorDoc = null; 
   /**
    * 
    */
   public PSOAbstractItemValidationExit()
   {
     
   }
   /**
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }
   /**
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[], com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      Document errorDoc = PSXmlDocumentBuilder.createXmlDocument();
      return errorDoc;
   }
   
   
   
   /**
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
   }
}
