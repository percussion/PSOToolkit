/*
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * test.percussion.pso.validation PSOAbstractItemValidationExitTest.java
 *
 */
package test.percussion.pso.validation;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.error.PSException;
import com.percussion.pso.validation.PSOAbstractItemValidationExit;
import com.percussion.pso.workflow.IPSOWorkflowInfoFinder;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.xml.PSXmlDocumentBuilder;

public class PSOAbstractItemValidationExitTest
{
   private static Log log = LogFactory.getLog(PSOAbstractItemValidationExitTest.class); 

   TestableItemValidationExit cut;
   Document sample; 
   Mockery context; 
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(){{setImposteriser(ClassImposteriser.INSTANCE);}};
      
      cut = new TestableItemValidationExit();
      sample = PSXmlDocumentBuilder.createXmlDocument(this.getClass().getResourceAsStream("editorsample.xml"), false);
   }
   @Test
   public final void testGetFieldElement()
   {
      Element el = cut.getFieldElement(sample, "sys_title"); 
      assertNotNull(el);
      String rep = PSXmlDocumentBuilder.toString(el);
      log.info("sys_title element is " +rep);
      assertTrue(rep.contains("paramName=\"sys_title\""));
   }
   public final void testGetFieldElementNotFound()
   {
      Element el = cut.getFieldElement(sample, "xyzzy"); 
      assertNull(el);
   }
   
   
   @Test
   public final void testGetFieldValue()
   {
      Element el = cut.getFieldElement(sample, "sys_title"); 
      assertNotNull(el);
      String val = cut.getFieldValue(el);      
      assertNotNull(val); 
      log.info("value is " + val);
      assertTrue(val.length() > 0); 
   }
   @Test
   public final void testGetFieldValueNoValue()
   {
      Element el = cut.getFieldElement(sample, "keywords"); 
      assertNotNull(el);
      String val = cut.getFieldValue(el);
      assertNull(val); 
   }
   
   @Test
   public final void testHasErrors()
   {
      Document err = PSXmlDocumentBuilder.createXmlDocument();
      boolean rslt = cut.hasErrors(err);
      assertFalse(rslt);
      PSItemErrorDoc.addError(err, "foo", "bar" , "xyzzy" , new Object[0]);
      rslt = cut.hasErrors(err);
      assertTrue(rslt);
   }
   
   @Test
   public final void testMatchDestinationStateBasics()
   {
      try
      {
         boolean rslt = cut.matchDestinationState("1" , "2", null);
         assertTrue(rslt);
         rslt = cut.matchDestinationState("1" , "2", "");
         assertTrue(rslt);
         rslt = cut.matchDestinationState("1", "2","*"); 
         assertTrue(rslt);
      } catch (PSException ex)
      {  
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception");
      }
   }
   
   @Test
   public final void testMatchDestinationStateComplex()
   {
      final PSState state = context.mock(PSState.class);
      final IPSOWorkflowInfoFinder finder = context.mock(IPSOWorkflowInfoFinder.class);
      cut.setFinder(finder);
      try
      {
         
         context.checking(new Expectations(){{
            one(finder).findDestinationState("1","2");
            will(returnValue(state));
            atLeast(1).of(state).getName();
            will(returnValue("fi"));
         }});
         boolean rslt = cut.matchDestinationState("1" , "2", "fee,fi,fo,fum");
         assertTrue(rslt);
         context.assertIsSatisfied();
         
      } catch (PSException ex)
      {  
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception");
      }
   }
   private class TestableItemValidationExit extends PSOAbstractItemValidationExit
   {

      /**
       * @see com.percussion.pso.validation.PSOAbstractItemValidationExit#getFieldElement(org.w3c.dom.Document, java.lang.String)
       */
      @Override
      public Element getFieldElement(Document inputDoc, String fieldName)
      {
         return super.getFieldElement(inputDoc, fieldName);
      }

      /**
       * @see com.percussion.pso.validation.PSOAbstractItemValidationExit#getFieldValue(org.w3c.dom.Element)
       */
      @Override
      public String getFieldValue(Element field)
      {
         return super.getFieldValue(field);
      }

      /**
       * @see com.percussion.pso.validation.PSOAbstractItemValidationExit#hasErrors(org.w3c.dom.Document)
       */
      @Override
      public boolean hasErrors(Document errorDoc)
      {
         return super.hasErrors(errorDoc);
      }

      /**
       * @see com.percussion.pso.validation.PSOAbstractItemValidationExit#validateDocs(org.w3c.dom.Document, org.w3c.dom.Document, com.percussion.server.IPSRequestContext, java.lang.Object[])
       */
      @Override
      public void validateDocs(Document inputDoc, Document errorDoc,
            IPSRequestContext req, Object[] params)
      {
         
      }

      /**
       * @see com.percussion.pso.validation.PSOAbstractItemValidationExit#matchDestinationState(java.lang.String, java.lang.String, java.lang.String)
       */
      @Override
      public boolean matchDestinationState(String contentid,
            String transitionid, String allowedStates) throws PSException
      {
         return super.matchDestinationState(contentid, transitionid, allowedStates);
      }

      /**
       * @see com.percussion.pso.validation.PSOAbstractItemValidationExit#setFinder(com.percussion.pso.workflow.IPSOWorkflowInfoFinder)
       */
      @Override
      public void setFinder(IPSOWorkflowInfoFinder finder)
      {         
         super.setFinder(finder);
      }
      
   }
}
