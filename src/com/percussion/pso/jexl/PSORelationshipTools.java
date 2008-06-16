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
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase; 
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.pso.relationships.PSOParentFinder;
import com.percussion.pso.utils.SimplifyParameters;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
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
   private static IPSGuidManager gmgr = null;
   
   /**
    * 
    */
   public PSORelationshipTools()
   {
      super();
   }
   
   private static void initServices()
   {
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
      }
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
   
   @IPSJexlMethod(description="find all parent items", 
         params={@IPSJexlParam(name="guid",description="the current item guid"),
          @IPSJexlParam(name="slotName",description="the slot name")},returns="a list of all parent GUIDs")
   public List<IPSGuid> findAllParentIds(IPSGuid guid, String slotName)
      throws Exception
   {
      initServices();
      String id = gmgr.makeLocator(guid).getPart(PSLocator.KEY_ID); 
      return findAllParentIds(id, slotName);
   }
   
   @IPSJexlMethod(description="find all parent items", 
         params={@IPSJexlParam(name="contentid",description="the content id of the current item"),
          @IPSJexlParam(name="slotName",description="the slot name")},returns="a list of all parent GUIDs")
   public List<IPSGuid> findAllParentIds(String contentid, String slotName)
      throws Exception
   {
      initServices();
     
      PSOParentFinder relFinder = new PSOParentFinder();
      Set<PSLocator> parentLocs = relFinder.findAllParents(contentid, slotName);
      List<IPSGuid> guids = new ArrayList<IPSGuid>(parentLocs.size()); 
      for(PSLocator loc : parentLocs)
      {
         guids.add(gmgr.makeGuid(loc)); 
      }
      return guids; 
   }

   /**
    * Is this page referenced in the landing page slot.
    * Will return true if the specified content id is referenced in a landing page slot.
    * Note that this function is limited to PUBLIC navons. Navons in QuickEdit will not 
    * consider the relationships of the current revision, only the  relationships of the 
    * last public revision. 
    * @param contentid the content id
    * @return <code>true</code> if this page has a public navon parent in the landing page slot. 
    * @throws Exception
    */
   @IPSJexlMethod(description="is this page referenced in the landing page slot",
         params={@IPSJexlParam(name="contentid", description="the content id of the current page")})
   public boolean isLandingPage(String contentid) throws Exception
   {
      PSOParentFinder relFinder = new PSOParentFinder();
      PSNavConfig nc = PSNavConfig.getInstance(); 
      String landingSlot = nc.getLandingPageRelationship(); 
      Set<PSLocator> parents = relFinder.findParents(contentid, landingSlot, true);
      return !parents.isEmpty(); 
   }
   
   /**
    * Is this page referenced in the landing page slot. Convenience method for 
    * {@link #isLandingPage(String)}
    * @param guid the guid
    * @return code>true</code> if this page has a public navon parent in the landing page slot.
    * @throws Exception
    */
   @IPSJexlMethod(description="is this page referenced in the landing page slot",
         params={@IPSJexlParam(name="guid", description="the current item guid")})
   public boolean isLandingPage(IPSGuid guid) throws Exception
   {
      initServices();
      String id = gmgr.makeLocator(guid).getPart(PSLocator.KEY_ID); 
      return isLandingPage(id); 
   }
   
   /**
    * Checks if an item has any non-public ancestors.  Convenience method for 
    * {@link #hasOnlyPublicAncestors(String, String, String)}
    * @param guid the current content item guid. 
    * @param slotName the slot name
    * @return <code>true</code> if all ancestors in the slot are public. 
    * @throws Exception
    */
   @IPSJexlMethod(description="Does this item have any non-public ancestors",
         params={@IPSJexlParam(name="guid",description="the content item GUID"),
                 @IPSJexlParam(name="slotName",description="slotName")})
   public boolean hasOnlyPublicAncestors(IPSGuid guid, String slotName)
      throws Exception
   {
      initServices();
      String contentid = gmgr.makeLocator(guid).getPart(PSLocator.KEY_ID);
      return hasOnlyPublicAncestors(contentid, slotName, null); 
   }
   /**
    * Checks if an item has any non-public ancestors.  The direct and indirect ancestors 
    * in the specified slot are scanned to make sure that they are in a workflow state 
    * consistent with the supplied validFlags. 
    * @see PSOParentFinder#hasOnlyPublicAncestors(String, String, List)
    * @param contentId the content id for the current item. 
    * @param slotName the slot name to scan
    * @param validFlags the validity flags considered public as a comma separated list. Defaults to 
    * &quot;y,i&quot; 
    * @return <code>true</code> if all ancestors in the slot are public. 
    * @throws Exception
    */
   @IPSJexlMethod(description="Does this item have any non-public ancestors",
         params={@IPSJexlParam(name="contentId",description="content id for item"),
                 @IPSJexlParam(name="slotName",description="slotName"),
                 @IPSJexlParam(name="validFlags",
                       description="validity flags considered public. Defaults to y,i")})
   public boolean hasOnlyPublicAncestors(String contentId, String slotName, String validFlags)
      throws Exception
   {
      if(StringUtils.isBlank(validFlags))
      {
         validFlags = "y,i"; 
      }
      List<String> vfList = SimplifyParameters.getValueAsList(validFlags);
      PSOParentFinder relFinder = new PSOParentFinder();

      return relFinder.hasOnlyPublicAncestors(contentId, slotName, vfList);
      
   }
   
   /**
    * @param gmgr the gmgr to set
    */
   public static void setGmgr(IPSGuidManager gmgr)
   {
      PSORelationshipTools.gmgr = gmgr;
   }
}
