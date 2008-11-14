/*
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.workflow IPSOWorkflowInfoFinder.java
 *
 */
package com.percussion.pso.workflow;
import java.util.Collection;

import com.percussion.error.PSException;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
public interface IPSOWorkflowInfoFinder
{
   /**
    * Finds a workflow by id.
    * @param id the workflow id
    * @return the workflow or <code>null</code> if not found. 
    */
   public PSWorkflow findWorkflow(int id);
   /**
    * Finds the workflow state. 
    * @param wf the workflow
    * @param state the state id 
    * @return the workflow state or <code>null</code> if 
    * not found.
    */
   public PSState findWorkflowState(PSWorkflow wf, int state);
   /**
    * finds the workflow state by Id. 
    * @param workflow the workflow id.
    * @param state the state id. 
    * @return the state or <code>null</code> if the state
    * does not exist. 
    * @throws IllegalArgumentException if the workflow does not exist.
    */
   public PSState findWorkflowState(int workflow, int state);
   /**
    * Finds the workflow state for a content item. 
    * @param contentId the content id. Must not be blank. 
    * @return the workflow state or <code>null</code> if the 
    * state does not exist. 
    * @throws PhotoGalleryException when the content item is not found.
    * @throws IllegalArgumentException when the contentid is not numeric, or when
    * the workflow id of the item is invalid.
    */
   public PSState findWorkflowState(String contentId) throws PSException;
   /**
    * Find the workflow state name
    * @param contentId the content id
    * @return the name of the workflow state. Never <code>null</code>
    * @throws PSException if the state is invalid.
    */
   public String findWorkflowStateName(String contentId) throws PSException;
   /**
    * Is the workflow state of an item one of the valid ones.  The content item state contains
    * a single valid flag. The method checks that this flag is one of the listed ones, comparing
    * with a case insensitive comparison.
    * @param contentId the content id for the item
    * @param validFlags the list of valid flags. 
    * @return <code>true</code> if the content valid value is one of the listed ones. 
    * @throws PSException
    */
   public boolean IsWorkflowValid(String contentId,
         Collection<String> validFlags) throws PSException;
   /**
    * Find the destination state for a particular workflow transition.
    * Convenience method for findDestinationState(String, String). 
    * @param contentId the content id. 
    * @param transitionId the transition id
    * @return the state where the transition goes. May be <code>null</code> if the transition is not found. 
    * @throws PSException
    */
   public PSState findDestinationState(String contentId, String transitionId)
         throws PSException;
   /**
    * Find the destination state for a workflow transition. 
    * @param state the current workflow state
    * @param transitionId the transition id
    * @return the destination state. May be <code>null</code> if the transition id is not found. 
    * @throws PSException 
    */
   public PSState findDestinationState(PSState state, String transitionId)
         throws PSException;
}