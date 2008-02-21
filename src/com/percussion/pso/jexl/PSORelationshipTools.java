/*
 * com.percussion.pso.sandbox PSOFolderTools.java
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * @author MikeStarck
 *
 */
package com.percussion.pso.jexl;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase; 
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * 
 *
 * @author MikeStarck
 *
 */
public class PSORelationshipTools extends PSJexlUtilBase implements IPSJexlExpression 
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSORelationshipTools.class);
   
   
   /**
    * 
    */
   public PSORelationshipTools()
   {
      super();
   }
   
   @IPSJexlMethod(description="get the dependents of this item of a certain content type", 
         params={
		@IPSJexlParam(name="itemId", description="the item GUID"),
		@IPSJexlParam(name="contenttypename", type="String", description="the name of the content type we are testing for"),
		@IPSJexlParam(name="userName", type="String", description="the userName with which to make the request")})
   public List<PSItemSummary> getRelationships(IPSGuid itemId, String contenttypename, String userName) 

   throws PSErrorException, PSExtensionProcessingException   
   {
      String errmsg; 
      if(itemId == null || contenttypename == null || userName == null)
      {
         errmsg = "No dependents found for null guid or null contenttypename or null user"; 
         log.error(errmsg); 
         throw new PSExtensionProcessingException(0, errmsg); 
      }
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();

      IPSGuid contenttypeid;
      List<PSContentTypeSummary> ctypes = null;
      try
      {
         ctypes = cws.loadContentTypes(contenttypename);
         if (ctypes.size() > 0)
         {
        	 contenttypeid = ctypes.get(0).getGuid();
        	 }
         else
         {
             log.warn("Content type " + contenttypename + " not found"); 
        	 return new ArrayList<PSItemSummary>();
         }
      } catch (Exception e1)
      {
         log.error("Cannot load content types", e1); 
         throw new PSExtensionProcessingException(PSORelationshipTools.class.getName(), e1);
      } 
      
      List<PSItemSummary> dependents = null; 
      try
      {
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setDependentContentTypeId(contenttypeid.longValue()); 
         dependents = cws.findDependents(
				itemId, 
				filter,
                false,
                userName);
      } catch (Exception e)
      {
        log.error("Unexpected exception " + e.getMessage(), e );
        throw new PSExtensionProcessingException(this.getClass().getCanonicalName(), e); 
      } 
      if(dependents.isEmpty())
      {
         errmsg = "cannot find dependents for " + itemId; 
         log.error(errmsg); 
         throw new PSExtensionProcessingException(0, errmsg); 
      }
      return dependents;
   }
   
}
