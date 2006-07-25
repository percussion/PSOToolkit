/*
 * com.percussion.pso.sandbox PSOFolderTools.java
 * 
 * @copyright 2006 Percussion Software, Inc. All rights reserved.
 * See license.txt for detailed restrictions. 
 * @author DavidBenua
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
 * @author DavidBenua
 *
 */
public class PSOFolderTools extends PSJexlUtilBase implements IPSJexlExpression 
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOFolderTools.class);
   
   
   /**
    * 
    */
   public PSOFolderTools()
   {
      super();
   }
   
   @IPSJexlMethod(description="get the folder path for this item", 
         params={@IPSJexlParam(name="itemId", description="the item GUID")})
   public String getParentFolderPath(IPSGuid itemId) 
   throws PSErrorException, PSExtensionProcessingException   
   {
      String errmsg; 
      if(itemId == null)
      {
         errmsg = "No path for null guid"; 
         log.error(errmsg); 
         throw new PSExtensionProcessingException(0, errmsg); 
      }
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      
      String[] paths = null;
      try
      {
         paths = cws.findFolderPaths(itemId);
      } catch (Exception e)
      {
        log.error("Unexpected exception " + e.getMessage(), e );
        throw new PSExtensionProcessingException(this.getClass().getCanonicalName(), e); 
      } 
      if(paths == null)
      {
         errmsg = "cannot find folder path for " + itemId; 
         log.error(errmsg); 
         throw new PSExtensionProcessingException(0, errmsg); 
      }
      if(paths.length == 0)
      {
         errmsg = "no paths returned for " + itemId; 
         log.error(errmsg);
         throw new PSExtensionProcessingException(0, errmsg); 
      }
      if(paths.length == 1)
      {
         log.debug("found path " + paths[0]);
         return paths[0]; 

      }
      log.warn("multiple paths found for item " + itemId);
      return paths[0];
   }
   
}
