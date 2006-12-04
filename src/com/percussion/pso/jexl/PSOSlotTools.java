/*
 * com.percussion.pso.jexl PSOSlotTools.java
 * 
 * @copyright 2006 Percussion Software, Inc. All rights reserved.
 * See license.txt for detailed restrictions. 
 * 
 * @author Adam Gent
 *
 */
package com.percussion.pso.jexl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.utils.exceptions.PSExceptionHelper;

/**
 * This class is a utility class to help work with slots.
 * 
 * @see #getSlotContents(IPSAssemblyItem, IPSTemplateSlot, Map)
 * @author agent
 *
 */
public class PSOSlotTools extends PSJexlUtilBase implements IPSJexlExpression
{

   private static final Log ms_log = LogFactory.getLog(PSOListTools.class);
   
   /**
    * Ctor. 
    */
   public PSOSlotTools()
   {
      super();
      // TODO Auto-generated constructor stub
   }
   
   /**
    * Get the contents of a slot as a list of assembly items.
    * 
    * @param item
    * @param slot
    * @param params
    * @return a list of results
    * @throws Throwable
    */
   @IPSJexlMethod(description = "Get the contents of a slot as a list of assembly items", params =
   {
         @IPSJexlParam(name = "item", description = "the parent assembly item"),
         @IPSJexlParam(name = "slot", description = "the slot"),
         @IPSJexlParam(name = "params", description = "extra parameters to the process")}, returns = "list of assembly items")
   public List<IPSAssemblyItem> getSlotContents(IPSAssemblyItem item,
         IPSTemplateSlot slot, Map<String, Object> params) throws Throwable
   {
      try
      {
         if (slot == null)
         {
            throw new IllegalArgumentException(
                  "slot may not be null, check template's slot reference");
         }
         if (params == null)
            params = new HashMap<String, Object>();
         IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
         // Handle old slots
         String findername = slot.getFinderName();
         if (findername == null)
         {
            ms_log.warn("No finder defined for slot " + slot.getName()
                  + " defaulting to sys_RelationshipContentFinder");
            findername = "Java/global/percussion/slotcontentfinder/sys_RelationshipContentFinder";
         }
         IPSSlotContentFinder finder = asm.loadFinder(findername);
         if (finder == null)
            throw new PSAssemblyException(IPSAssemblyErrors.MISSING_FINDER,
                  finder);
         List<IPSAssemblyItem> relitems = finder.find(item, slot, params);
         return relitems;
      }
      catch (PSAssemblyException ae)
      {
         
         /*
          * What should we do if assembly failes.
          */
         PSAssemblyWorkItem work = (PSAssemblyWorkItem) item;
         // Create clone for response
         work = (PSAssemblyWorkItem) work.clone();
         work.setStatus(Status.FAILURE);
         work.setMimeType("text/html");
         //TODO: Change this log message
         ms_log.warn("Assembly failed.");
         List<IPSAssemblyItem> rvalue = new ArrayList<IPSAssemblyItem>();
         // Should we add the failed work item?
         rvalue.add(work);
         return rvalue; 
      }
      catch (Throwable e)
      {
         Throwable orig = PSExceptionHelper.findRootCause(e, true);
         ms_log.error(PSI18nUtils
               .getString("psx_assembly@Problem during assembly"), orig);
         throw e;
      }
   }

}
