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
public class PSOWorkflowInfoFinder implements IPSOWorkflowInfoFinder
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
    * @see com.percussion.pso.workflow.IPSOWorkflowInfoFinder#findWorkflow(int)
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
    * @see com.percussion.pso.workflow.IPSOWorkflowInfoFinder#findWorkflowState(com.percussion.services.workflow.data.PSWorkflow, int)
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
    * 
    * @see com.percussion.pso.workflow.IPSOWorkflowInfoFinder#findWorkflowTransition(int, int)
    */
   public PSTransition findWorkflowTransition(int workflow, int transid)
   {
      PSWorkflow wf = findWorkflow(workflow);
      if(wf == null)
      {
         String emsg = "Workflow id " + workflow + " not found";
         log.error(emsg); 
         throw new IllegalArgumentException(emsg);
      }      
      return findWorkflowTransition(wf, transid);       
   }
   
   /**
    * 
    * @see com.percussion.pso.workflow.IPSOWorkflowInfoFinder#findWorkflowTransition(com.percussion.services.workflow.data.PSWorkflow, int)
    */
   public PSTransition findWorkflowTransition(PSWorkflow wf, int transid)
   {
      for(PSState st : wf.getStates())
      {
         for(PSTransition trans : st.getTransitions())
         {
            long tid = trans.getGUID().longValue();
            if(tid == transid)
            {
               return trans;
            }
         }
//         for(PSTransition trans : st.getAgingTransitions())
//         {
//            long tid = trans.getGUID().longValue();
//            if(tid == transid)
//            {
//               return trans;
//            }
//         }
      }
      return null; 
   }
   /**
    * @see com.percussion.pso.workflow.IPSOWorkflowInfoFinder#findWorkflowState(int, int)
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
    * @see com.percussion.pso.workflow.IPSOWorkflowInfoFinder#findWorkflowState(java.lang.String)
    */
   public PSState findWorkflowState(String contentId) throws PSException
   {     
      PSComponentSummary sum = PSOItemSummaryFinder.getSummary(contentId);
      int wfapp = sum.getWorkflowAppId();
      int wfst = sum.getContentStateId();
      return findWorkflowState(wfapp, wfst);
   }
   
   /**
    * @see com.percussion.pso.workflow.IPSOWorkflowInfoFinder#findWorkflowStateName(java.lang.String)
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
    * @see com.percussion.pso.workflow.IPSOWorkflowInfoFinder#IsWorkflowValid(java.lang.String, java.util.Collection)
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
    * @see com.percussion.pso.workflow.IPSOWorkflowInfoFinder#findDestinationState(java.lang.String, java.lang.String)
    */
   public PSState findDestinationState(String contentId, String transitionId) throws PSException
   {
      PSState state = findWorkflowState(contentId);
      return findDestinationState(state, transitionId); 
   }
   
   /**
    * @see com.percussion.pso.workflow.IPSOWorkflowInfoFinder#findDestinationState(com.percussion.services.workflow.data.PSState, java.lang.String)
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
