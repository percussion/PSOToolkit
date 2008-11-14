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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemValidator;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.workflow.IPSOWorkflowInfoFinder;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

/**
 * Base class for Item Validation exits. Provides generic routines for handling of 
 * error documents, fields and lookup of destination workflow states. 
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
  
   private IPSOWorkflowInfoFinder finder = null; 
   /**
    * Default constructor.
    */
   public PSOAbstractItemValidationExit()
   {
     
   }
   
   /**
    * Initialize the service pointers. 
    */
   private void initServices()
   {
      if(finder == null)
      {
         finder = new PSOWorkflowInfoFinder();
      }
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
//      if(log.isTraceEnabled())
//      {
//         String idoc = PSXmlDocumentBuilder.toString(resultDoc);
//         log.trace("result doc is " + idoc); 
//      }
      Document errorDoc = PSXmlDocumentBuilder.createXmlDocument();
      try
      {
         validateDocs(resultDoc, errorDoc,  request, params);
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         throw new PSExtensionProcessingException(getClass().getName(), ex);
      }
      if(hasErrors(errorDoc))
      {
         log.debug("validation errors found"); 
         return errorDoc;
      }
      log.debug("validation successful"); 
      return null;    
   }
   
   protected abstract void validateDocs(Document inputDoc, Document errorDoc, IPSRequestContext req, Object[] params)
     throws Exception;
   
   
   /**
    * Finds the field element for a given field
    * @param inputDoc the XML document from the content editor
    * @param fieldName the field name
    * @return the XML element that represents the field. Will be <code>null</code> if the 
    * field does not exist.
    */
   protected Element getFieldElement(Document inputDoc, String fieldName)
   {
      PSXmlTreeWalker fieldWalker = new PSXmlTreeWalker(inputDoc.getDocumentElement());
      fieldWalker.getNextElement("ItemContent",PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN); 
      Element field = fieldWalker.getNextElement("DisplayField", PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN); 
      while(field != null)
      {
         PSXmlTreeWalker fw = new PSXmlTreeWalker(field); 
         Element control = fw.getNextElement("Control", PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if(control != null)
         {
            String fld = control.getAttribute("paramName");
            if(StringUtils.isNotBlank(fld) && fld.equals(fieldName))
            {  //the field name matches
               return field; 
            }
         }
         field = fieldWalker.getNextElement("DisplayField", PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS); 
      }
      return null; 
   }
   
   /**
    * Finds the String value for a field element.  
    * @param field the field element 
    * @return the value. Will be <code>null</code> if there is no &lt;Value&gt; node in the 
    * field element. 
    * @see #getFieldElement(Document, String)
    */
   protected String getFieldValue(Element field)
   {
      PSXmlTreeWalker w = new PSXmlTreeWalker(field);
      Element c = w.getNextElement("Control", PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if(c != null)
      {
         c = w.getNextElement("Value", PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if(c == null)
         {
            return null;
         }
         String val = w.getElementData();
         return val; 
      }
      return null; 
   }
   
   /**
    * Determines if an error document contains errors. 
    * @param errorDoc the error document 
    * @return <code>true</code> if there are any errors. 
    */
   protected boolean hasErrors(Document errorDoc)
   {
      Element root = errorDoc.getDocumentElement();
      if(root == null)
      {
         return false;
      }
      PSXmlTreeWalker w = new PSXmlTreeWalker(root);
      Element e = w.getNextElement(PSItemErrorDoc.ERROR_FIELD_SET_ELEM, PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if(e == null)
      {
         return false;
      }
      e = w.getNextElement(PSItemErrorDoc.ERROR_FIELD_ELEM, PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if(e == null)
      {
         return false;
      }
      return true; 
   }
   
   /**
    * Matches the current item workflow state and transition ids with a comma delimited list of workflow 
    * state names. 
    * This method will return true if the destination state matches one of the listed states.  If the state
    * list is blank or contains a single "*" it is assumed to match all states. 
    * @param contentid the content id of the item 
    * @param transitionid the transition id
    * @param allowedStates the list of destination state names that match. 
    * @return <code>true</code> when a match occurs,<code>false</code> otherwise. 
    * @throws PSException 
    */
   protected boolean matchDestinationState(String contentid, String transitionid, String allowedStates) throws PSException
   {
      if(StringUtils.isBlank(allowedStates))
      { //match everything
         return true;
      }
      if(allowedStates.trim().equals("*"))
      {
         return true; 
      }
      initServices();
      List<String> allowed = Arrays.<String>asList(allowedStates.split(","));
      PSState state = finder.findDestinationState(contentid, transitionid);
      Validate.notNull(state,"Invalid workflow state " + contentid );
      return allowed.contains(state.getName())? true : false;  
   }
   
   /**
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
   }

   /**
    * @param finder the finder to set. Used only for unit test.
    */
   protected void setFinder(IPSOWorkflowInfoFinder finder)
   {
      this.finder = finder;
   }
}
