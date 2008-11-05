/*
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.utils IPSOItemSummaryFinder.java
 *
 */
package com.percussion.pso.utils;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.utils.guid.IPSGuid;
public interface IPSOItemSummaryFinder
{
   public PSLocator getCurrentOrEditLocator(IPSGuid guid) throws PSException;
   public PSLocator getCurrentOrEditLocator(String contentId)
         throws PSException;
   public PSLocator getCurrentOrEditLocator(int id) throws PSException;
   public int getCheckoutStatus(String contentId, String userName)
         throws PSException;
   /**
    * Gets the component summary for an item.
    * @param contentId the content id
    * @return the component summary. Never <code>null</code>.
    * @throws PSException when the item does not exist.
    */
   public PSComponentSummary getSummary(String contentId) throws PSException;
   public PSComponentSummary getSummary(IPSGuid guid) throws PSException;
   public PSComponentSummary getSummary(int id) throws PSException;
}