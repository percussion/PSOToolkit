/*
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.workflow PSOSpringWorkflowActionDispatcher.java
 *  
 */
package com.percussion.pso.workflow;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;


/**
 * A workflow action based on the PSOWFActionService bean. 
 * 
 *
 * @author DavidBenua
 * @see IPSOWFActionService
 */
public class PSOSpringWorkflowActionDispatcher extends PSDefaultExtension
    implements IPSWorkflowAction 
{
    private static final Log log = LogFactory.getLog(PSOSpringWorkflowActionDispatcher.class);
    IPSOWFActionService asvc = null; 
    
    /**
     * Default constructor
     */
    public PSOSpringWorkflowActionDispatcher()
    {
        
    }

    /**
     * 
     * @see com.percussion.extension.PSDefaultExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
     */
    public void init(IPSExtensionDef extensionDef, File codeRoot)
        throws PSExtensionException
    {
        super.init(extensionDef, codeRoot);
        log.debug("Initializing WFActionDispatcher...");
        
        if(asvc == null)
        {
           asvc = PSOWFActionServiceLocator.getPSOWFActionService(); 
        }
    }

    /**
     * Perform the action. 
     * @see com.percussion.extension.IPSWorkflowAction#performAction(com.percussion.extension.IPSWorkFlowContext, com.percussion.server.IPSRequestContext)
     */
    public void performAction(IPSWorkFlowContext wfContext, IPSRequestContext request)
        throws PSExtensionProcessingException
    {

        int transitionId = wfContext.getTransitionID();
        int workflowId = wfContext.getWorkflowID();
        log.debug("Workflow id: " + workflowId);
        log.debug("Transition Id: " + transitionId);
        try
        {
           List<IPSWorkflowAction> actions = asvc.getActions(workflowId, transitionId);
           for(IPSWorkflowAction act : actions)
           {
              log.debug("performing action " + act.getClass().getCanonicalName()); 
              act.performAction(wfContext, request); 
           }
           log.debug("finished actions"); 
        }
        catch(Exception nfx)
        {
            log.error("unknown error " + nfx.getMessage(),nfx);
            throw new PSExtensionProcessingException("PSOWFActionDispatcher", nfx);
        }
    }

   /**
    * @param asvc the asvc to set. Used for unit test only. 
    */
   protected void setAsvc(IPSOWFActionService asvc)
   {
      this.asvc = asvc;
   }
}