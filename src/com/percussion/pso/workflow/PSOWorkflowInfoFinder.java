/*
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.workflow PSOWorkflowInfoFinder.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.workflow;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSException;
import com.percussion.pso.utils.PSOItemSummaryFinder;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
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
   private IPSGuidManager gmgr = null; 
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
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
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
   
   /**
    * Find the workflow state name
    * @param contentId the content id
    * @return the name of the workflow state. Never <code>null</code>
    * @throws PSException if the state is invalid.
    */
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
    * Is the workflow state of an item one of the valid ones.  The content item state contains
    * a single valid flag. The method checks that this flag is one of the listed ones, comparing
    * with a case insensitive comparison.
    * @param contentId the content id for the item
    * @param validFlags the list of valid flags. 
    * @return <code>true</code> if the content valid value is one of the listed ones. 
    * @throws PSException
    */
   public boolean IsWorkflowValid(String contentId, Collection<String> validFlags) 
      throws PSException
   {
      PSState state = findWorkflowState(contentId); 
      String cvalid = state.getContentValidValue(); 
      if(StringUtils.isBlank(cvalid))
      {
         String emsg = "Invalid content valid flag for state " + state.getName(); 
         log.error(emsg);
         throw new PSException(emsg); 
      }
      for(String v : validFlags)
      {
         if(cvalid.equalsIgnoreCase(v))
         {
            return true;
         }
      }
      return false; 
   }

   /**
    * Find the destination state for a particular workflow transition.
    * Convenience method for findDestinationState(String, String). 
    * @param contentId the content id. 
    * @param transitionId the transition id
    * @return the state where the transition goes. May be <code>null</code> if the transition is not found. 
    * @throws PSException
    */
   public PSState findDestinationState(String contentId, String transitionId) throws PSException
   {
      PSState state = findWorkflowState(contentId);
      return findDestinationState(state, transitionId); 
   }
   
   /**
    * Find the destination statie for a workflow transtion. 
    * @param state the current workflow state
    * @param transitionId the transition id
    * @return the destination state. May be <code>null</code> if the transition id is not found. 
    * @throws PSException 
    */
   public PSState findDestinationState(PSState state, String transitionId) throws PSException
   {
      String emsg;
      if(state == null)
      {
         emsg = "State must not be null";
         log.error(emsg);
         throw new PSException(emsg); 
      }
      if(StringUtils.isBlank(transitionId))
      {
         emsg = "Transition id must not be null"; 
         log.error(emsg); 
         throw new PSException(emsg); 
      }
      IPSGuid tguid = gmgr.makeGuid(transitionId, PSTypeEnum.WORKFLOW_TRANSITION); 
      for(PSTransition tr : state.getTransitions())
      {
         if(tguid.equals(tr.getGUID()))
         {   //found our transition
             PSState dest = findWorkflowState((int)state.getWorkflowId(), (int)tr.getToState());
             if(dest == null)
             {   //stateid is invalid for this workflow. 
                emsg = "no such state " + state.getWorkflowId() + " - " + tr.getToState();
                log.error(emsg); 
                throw new PSException(emsg); 
             }
             log.debug("found destination state " + dest.getName()); 
             return dest; 
         }
      }
      //no transition found
      return null;
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
