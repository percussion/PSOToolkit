/*
 * com.percussion.pso.relationships PSOParentFinder.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.relationships;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.pso.utils.PSORequestContext;
import com.percussion.pso.utils.UniqueIdLocatorSet;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;

/**
 * Finds parent items for a given content item.
 *
 * @author DavidBenua
 *
 */
public class PSOParentFinder
{
   
   private IPSRequestContext requestContext = null; 
   private PSRelationshipProcessorProxy proxy = null;
   private IPSGuidManager gmgr = null; 
   private IPSAssemblyService asm = null;
   private PSOWorkflowInfoFinder workflow;   

   private static Log log = LogFactory.getLog(PSOParentFinder.class); 

   /**
    * Default constructor.
    */
   public PSOParentFinder()
   {
      workflow = new PSOWorkflowInfoFinder();
   }
   
   /**
    * Finds all parents for an item.  Convenience method for {@link #findAllParents(PSLocator, String)}.. 
    * @param contentid the item content id
    * @param slotName the slot name
    * @return the set of parent locators. Never <code>null</code> but may be <code>empty</code>. 
    * @throws PSAssemblyException
    * @throws PSCmsException
    */
   public Set<PSLocator> findAllParents(String contentid, String slotName) 
      throws PSAssemblyException, PSCmsException
   {
      PSLocator dependent = new PSLocator(contentid); 
      return findAllParents(dependent, slotName); 
   }
   
   /**
    * Finds all parents for an item. Includes both parents with the current owner revision, the edit 
    * owner revision, and the last public revision. 
    * @param dependent the dependent item. 
    * @param slotName the slot name. Must not be null or empty. 
    * @return the set of parent locators. Never <code>null</code> but may be <code>empty</code>.
    * @throws PSAssemblyException
    * @throws PSCmsException
    */
   public Set<PSLocator> findAllParents(PSLocator dependent, String slotName) 
      throws PSAssemblyException, PSCmsException
   {
      Set<PSLocator> parents = new UniqueIdLocatorSet();
      //add the parents for the current revision
      parents.addAll(findParents(dependent,slotName, false));
      //add the parents for the last public revision
      parents.addAll(findParents(dependent, slotName, true));
      return parents; 
   }
   
   /**
    * Find parents for an item. Convenience method for {@link #findParents(PSLocator, String, boolean)}. 
    * @param contentid the content id.  
    * @param slotName  the slot name.
    * @param usePublic the public revision flag. 
    * @return the set of parent locators. Never <code>null</code> but may be <code>empty</code>.
    * @throws PSAssemblyException
    * @throws PSCmsException
    */
   public Set<PSLocator> findParents(String contentid, String slotName, boolean usePublic ) 
   throws PSAssemblyException, PSCmsException
   {
      PSLocator dependent = new PSLocator(contentid);
      return findParents(dependent, slotName, usePublic);
   }
   
   /**
    * Finds the 
    * @param dependent the locator for the dependent item.
    * @param slotName
    * @param usePublic the public revision flag. If <code>true</code>, only relationships where the owner is 
    * the public revision will be considered.  If<code>false</code>, only relationships where the owner is the 
    * current or edit revision will be considered.  
    * @return the set of parent locators. Never <code>null</code> but may be <code>empty</code>.
    * @throws PSAssemblyException
    * @throws PSCmsException
    */
   public Set<PSLocator> findParents(PSLocator dependent, String slotName, boolean usePublic ) 
      throws PSAssemblyException, PSCmsException
   {       
      initServices();
      String slotid = getSlotId(slotName); 
      log.debug("Slot name " + slotName +  " id is " + slotid); 
      PSRelationshipFilter filter = new PSRelationshipFilter(); 
      filter.setDependent(dependent); 
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
      if(usePublic)
      {
         filter.limitToPublicOwnerRevision(true); 
      }
      else
      {
         filter.limitToEditOrCurrentOwnerRevision(true); 
      }
      filter.setProperty(IPSHtmlParameters.SYS_SLOTID, slotid); 
      PSRelationshipSet rels = proxy.getRelationships(filter); 
      log.debug("there were " + rels.size() + " parents found"); 
      Set<PSLocator> parents = new UniqueIdLocatorSet();
      for(Object relobj : rels)
      {
         PSRelationship rel = (PSRelationship) relobj;
         PSLocator parent = rel.getOwner(); 
         parents.add(parent); 
      }
      
      return parents; 
       
   }
   
   /**
    * Determines if this item has any non-public ancestors in the given slot name. 
    * Will return <code>false</code> if any direct or indirect ancestor item (in the given slot) 
    * is in a workflow state that does not have one of the valid flags in it. An item with no parents 
    * will return <code>true</code>.  
    * @param contentId the content id of the item. 
    * @param slotName the name of the slot.
    * @param validFlags the list of valid flags. 
    * @return <code>true</code> if all of the items ancestors in the slot are public. 
    * @throws PSAssemblyException
    * @throws PSException
    */
   public boolean hasOnlyPublicAncestors(String contentId, String slotName, List<String> validFlags) 
      throws PSAssemblyException, PSException
   {      
      Set<PSLocator> parents = this.findAllParents(contentId, slotName);  
      for(PSLocator p : parents)
      {
         String id = p.getPart(PSLocator.KEY_ID); 
         if(!workflow.IsWorkflowValid(id , validFlags)) 
         {
            return false; 
         }
         if(!hasOnlyPublicAncestors(id, slotName, validFlags))
         {
            return false; 
         }
      }
      return true;
   }
   
   /**
    * Gets the slotid from the slot name
    * @param slotName the slot name. 
    * @return the slot id. 
    * @throws PSAssemblyException if the named slot does not exist.
    */
   protected String getSlotId(String slotName) throws PSAssemblyException
   {
      initServices();
      IPSTemplateSlot slot = asm.findSlotByName(slotName);
      int slotid = slot.getGUID().getUUID();
      return String.valueOf(slotid);
   }
   
   
   private void initServices()
   {
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
      }
      if(asm == null)
      {
         asm = PSAssemblyServiceLocator.getAssemblyService(); 
      }
      if(proxy == null)
      {
         try
         {
            requestContext = new PSORequestContext();
            proxy = new PSRelationshipProcessorProxy(
                  PSRelationshipProcessorProxy.PROCTYPE_SERVERLOCAL,requestContext);
         } catch (PSCmsException ex)
         {
            log.error("Unexpected Exception initializing proxy " + ex,ex);
         }
      }
   }

   /**
    * @param proxy the proxy to set
    */
   public void setProxy(PSRelationshipProcessorProxy proxy)
   {
      this.proxy = proxy;
   }

   /**
    * @param gmgr the gmgr to set
    */
   public void setGmgr(IPSGuidManager gmgr)
   {
      this.gmgr = gmgr;
   }

   /**
    * @param asm the asm to set
    */
   public void setAsm(IPSAssemblyService asm)
   {
      this.asm = asm;
   }
}
