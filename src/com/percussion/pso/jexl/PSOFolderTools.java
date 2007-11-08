/*
 * com.percussion.pso.sandbox PSOFolderTools.java
 * 
 * @copyright 2006 Percussion Software, Inc. All rights reserved.
 * See license.txt for detailed restrictions. 
 * @author DavidBenua
 *
 */
package com.percussion.pso.jexl;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * 
 *
 * @author DavidBenua
 * @author AdamGent
 *
 */
public class PSOFolderTools extends PSJexlUtilBase implements IPSJexlExpression 
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOFolderTools.class);
   private IPSContentWs contentWs;
   private IPSGuidManager guidManager;
   
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
    String[] paths = null;
      try
      {
         paths = contentWs.findFolderPaths(itemId);
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

   @IPSJexlMethod(description="get the folder path for this item", 
           params={@IPSJexlParam(name="assemblyItem", description="$sys.assemblyItem")},
           returns="the path of the folder that contains this item"
   )
   public String getParentFolderPath(IPSAssemblyItem assemblyItem) throws PSErrorResultsException { 
       int id = assemblyItem.getFolderId();
       if (id < 0) return null;
       IPSGuid folderGuid = guidManager.makeGuid(new PSLocator(id, -1));
       List<PSFolder> folders = contentWs.loadFolders(asList(folderGuid));
       if (folders.size() < 1) return null;
       return folders.get(0).getFolderPath();
       
   }

   @Override
    public void init(IPSExtensionDef def, File codeRoot)
            throws PSExtensionException {
        super.init(def, codeRoot);
        contentWs = PSContentWsLocator.getContentWebservice();
        guidManager = PSGuidManagerLocator.getGuidMgr();
    }

    public IPSContentWs getContentWs() {
        return contentWs;
    }

    public void setContentWs(IPSContentWs contentWs) {
        this.contentWs = contentWs;
    }

    public IPSGuidManager getGuidManager() {
        return guidManager;
    }

    public void setGuidManager(IPSGuidManager guidManager) {
        this.guidManager = guidManager;
    }
   
}
