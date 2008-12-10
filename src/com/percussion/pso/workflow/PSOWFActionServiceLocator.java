/*
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.workflow PSOWFActionServiceLocator.java
 *
 */
package com.percussion.pso.workflow;

import com.percussion.services.PSBaseServiceLocator;

/**
 * Locator for the IPSOWFActionService bean. 
 *
 * @author DavidBenua
 * @see IPSOWFActionService
 * @see PSOSpringWorkflowActionDispatcher
 */
public class PSOWFActionServiceLocator extends PSBaseServiceLocator
{
   /**
    * Gets the PSO Workflow Action Service bean. 
    * @return the PSO Workflow Action Service bean. 
    */
   public static IPSOWFActionService getPSOWFActionService()
   {
      return (IPSOWFActionService) PSBaseServiceLocator.getBean(PSO_WF_ACTION_SERVICE_BEAN); 
   }
   
   public static final String PSO_WF_ACTION_SERVICE_BEAN = "psoWFActionService";
}
