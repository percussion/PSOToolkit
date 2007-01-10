/*
 * com.percussion.pso.mlb.photogallery.workflow PSOWorkflowInfoFinder.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.workflow;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSException;
import com.percussion.pso.utils.PSOItemSummaryFinder;
import com.percussion.services.system.data.PSState;
import com.percussion.services.system.data.PSWorkflow;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
 * Finds basic info about the workflow state for an item.
 * 
 * This class loads the workflow list from the system web 
 * service, and will not detect changes until the next restart. 
 * 
 *
 * @author DavidBenua
 *
 */
public class PSOWorkflowInfoFinder
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOWorkflowInfoFinder.class);

   private IPSSystemWs sws = null;    
   private List<PSWorkflow> workflows = null;
 
   
   /**
    * Creates a new finder.
    */
   public PSOWorkflowInfoFinder()
   {
      super();      
   }
   
   /**
    * Initializes system services. 
    *
    */
   private void initServices()
   {
      if(sws == null)
      {
         sws = PSSystemWsLocator.getSystemWebservice();
      }
      if(workflows == null)
      {
         workflows = sws.loadWorkflows(null);
      }
   }
 
   /**
    * Finds a workflow by id.
    * @param id the workflow id
    * @return the workflow or <code>null</code> if not found. 
    */
   public PSWorkflow findWorkflow(int id)
   {
      initServices();
      for(PSWorkflow wf : workflows)
      {
         if(wf.getGUID().longValue() == id)
         {
            return wf;
         }
      }
      return null;
   }
   
   /**
    * Finds the workflow state. 
    * @param wf the workflow
    * @param state the state id 
    * @return the workflow state or <code>null</code> if 
    * not found.
    */
   public PSState findWorkflowState(PSWorkflow wf, int state)
   {
      for(PSState st : wf.getStates())
      {
         long st2 = st.getGUID().longValue(); 
         if(st2 == state)
         {
            return st;
         }
      }
      return null;
   }
   
   /**
    * finds the workflow state by Id. 
    * @param workflow the workflow id.
    * @param state the state id. 
    * @return the state or <code>null</code> if the state
    * does not exist. 
    * @throws IllegalArgumentException if the workflow does not exist.
    */
   public PSState findWorkflowState(int workflow, int state)
   {
      PSWorkflow wf = this.findWorkflow(workflow); 
      if(wf == null)
      {
         String emsg = "Workflow id " + workflow + " not found";
         log.error(emsg); 
         throw new IllegalArgumentException(emsg);
      }
      return findWorkflowState(wf, state);
   }
   
   /**
    * Finds the workflow state for a content item. 
    * @param contentId the content id. Must not be blank. 
    * @return the workflow state or <code>null</code> if the 
    * state does not exist. 
    * @throws PhotoGalleryException when the content item is not found.
    * @throws IllegalArgumentException when the contentid is not numeric, or when
    * the workflow id of the item is invalid.
    */
   public PSState findWorkflowState(String contentId) throws PSException
   {     
      PSComponentSummary sum = PSOItemSummaryFinder.getSummary(contentId);
      int wfapp = sum.getWorkflowAppId();
      int wfst = sum.getContentStateId();
      return findWorkflowState(wfapp, wfst);
   }
   
   public String findWorkflowStateName(String contentId) throws PSException
   {
      PSState state = findWorkflowState(contentId);
      if(state == null)
      {
         String emsg = "Invalid workflow state for item " + contentId; 
         log.error(emsg); 
         throw new PSException(emsg);
      }
      return state.getName();
   }


   /**
    * Sets the system web service.
    * Should be used only in unit tests.
    * @param sws The sws to set.
    */
   public void setSws(IPSSystemWs sws)
   {
      this.sws = sws;
   }

   /**
    * Sets the workflow list. 
    * Should be used only in unit tests.
    * @param workflows The workflows to set.
    */
   public void setWorkflows(List<PSWorkflow> workflows)
   {
      this.workflows = workflows;
   }
}
