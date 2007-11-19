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

import javax.jcr.RepositoryException;

import static java.util.Arrays.asList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
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
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.assembly.impl.nav.PSNavHelper;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.filter.PSFilterException;
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
   public String getParentFolderPath(IPSAssemblyItem assemblyItem)
            throws PSErrorResultsException, PSExtensionProcessingException, PSErrorException {
        int id = assemblyItem.getFolderId();
        String path = null;
        /*
         * If there is no folder id associated with the assembly item
         * (ie sys_folderid was not passed as a parameter) then we are going
         * to have to lookup the folder using the same process that managed nav
         * does.
         * Unfortuanatly this process is tightly coupled to Nav so we have
         * to instantiate the PSNavHelper class instead of using a service.
         * TODO Dave should look over this.
         */
        if (id <= 0) {
            log.debug("Assembly Item does not have a folder id.");
            if (assemblyItem instanceof PSAssemblyWorkItem) {
                PSAssemblyWorkItem awi = (PSAssemblyWorkItem) assemblyItem;
                PSNavHelper helper = awi.getNavHelper();
                if (helper != null) {
                    log.debug("Using NavHelper to find folder id.");
                    String errMesg = "Tried to use NavHelper to get parent folder path but failed!";
                    try {
                        IPSNode navNode = (IPSNode) helper.findNavNode(assemblyItem);
                        if (navNode != null) {
                            path = getParentFolderPath(navNode.getGuid());
                        }
                        else {
                            log.warn("Tried to use NavHelper to getParentFolderPath " +
                                    "but no navon could be found.");
                            path = null;
                        }
                    } catch (PSCmsException e) {
                        log.error(errMesg, e);
                        throw new RuntimeException(e);
                    } catch (PSFilterException e) {
                        log.error(errMesg,e);
                        throw new RuntimeException(e);
                    } 
                    catch (RepositoryException e) {
                        log.warn("Could not find folder using NavHelper: ", e);
                        path = null;
                    }
                }
                else {
                    log.debug("Could not use NavHelper to find folderid because the" +
                            " provided assembly item did not have one. (getNavHelper() == null)");
                    path = null;
                }
            }
        } 
        else {
            log.debug("Using AssemblyItem's folderid = " + id);
            IPSGuid folderGuid = guidManager.makeGuid(new PSLocator(id, -1));
            List<PSFolder> folders = contentWs.loadFolders(asList(folderGuid));
            if (folders.size() < 1) {
                path = null;
            } 
            else {
                path = folders.get(0).getFolderPath();
            }
        }
        return path;
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
