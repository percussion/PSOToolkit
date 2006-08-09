/*
 * com.percussion.pso.sandbox PSOFolderTools.java
 * 
 * @copyright 2006 Percussion Software, Inc. All rights reserved.
 * See license.txt for detailed restrictions. 
 * @author MikeStarck
 *
 */
package com.percussion.pso.jexl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
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
		@IPSJexlParam(name="contenttypeid", type="String", description="the name of the content type we are testing for")})
   public String getRelationships(IPSGuid itemId, String contenttypename) 
   throws PSErrorException, PSExtensionProcessingException   
   {
      String errmsg; 
      if(itemId == null || contenttypename == null)
      {
         errmsg = "No dependents found for null guid or null contenttypename"; 
         log.error(errmsg); 
         throw new PSExtensionProcessingException(0, errmsg); 
      }
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      String contentypeid = cws.loadContentTypes(contenttypename).getTypeId;
      
      String [] dependents;
      try
      {
         dependents = cws.findDependents(
				itemId, 
				setOwnerId(itemId), 
				setDependentContentTypeId(contenttypeid)
			);
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
