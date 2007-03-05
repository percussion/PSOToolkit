/*
 * com.percussion.pso.itemfilter PSORevisionCorrectingItemFilter.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.itemfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.pso.utils.SimplifyParameters;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSItemFilterRule;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.impl.PSBaseFilter;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.system.data.PSState;
import com.percussion.utils.guid.IPSGuid;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class PSORevisionCorrectingItemFilter extends PSBaseFilter
      implements
         IPSItemFilterRule
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSORevisionCorrectingItemFilter.class);

   private static IPSSystemService sys = null; 
   private static IPSGuidManager gmgr = null;
   private static IPSCmsContentSummaries summ = null; 
   /**
    * 
    */
   public PSORevisionCorrectingItemFilter()
   {
      super();
   }
  
   /**
    * @see com.percussion.services.filter.IPSItemFilterRule#filter(java.util.Set, java.util.Map)
    */
   public List<IPSFilterItem> filter(List<IPSFilterItem> items,
         Map<String, String> params) throws PSFilterException
   {
      log.info("Filtering items for revision" );
      List<String> wfStates = SimplifyParameters.
          getValueAsList(params.get(WORKFLOW_STATES));
      List<IPSFilterItem> results = new ArrayList<IPSFilterItem>(items.size()); 
      
      for(IPSFilterItem item : items)
      {
         IPSGuid itemguid = item.getItemId(); 
         log.debug("Original Guid is " + itemguid); 
         IPSGuid revguid = correctGuid(itemguid,wfStates);
         log.debug("Corrected Guid is " + revguid); 
         if(!itemguid.equals(revguid)) 
         {
            try
            {
               log.debug("replacing GUID " + itemguid + " with GUID " + revguid);
               IPSFilterItem revItem = item.clone(revguid);
               results.add(revItem);
            } catch (CloneNotSupportedException ex)
            {
               log.error("Unable to clone " + item);
               //should NEVER happen.
               throw new IllegalStateException("Clone not supported"); 
            }  
         }
         else
         {
            results.add(item);
         }
      }
      return results; 
   }
   
   private static IPSGuid correctGuid(IPSGuid in, List<String> wfStates)
   {
      initServices();
      PSComponentSummary sum = summ.loadComponentSummary(gmgr.makeLocator(in).getId());
      IPSGuid workflowid = gmgr.makeGuid(sum.getWorkflowAppId(), PSTypeEnum.WORKFLOW);
      IPSGuid stateid = gmgr.makeGuid(sum.getContentStateId(), PSTypeEnum.WORKFLOW_STATE);      
      PSState state = sys.loadWorkflowState(stateid, workflowid);
      
      if(wfStates.contains(state.getName()))
      {
         return gmgr.makeGuid(sum.getCurrentLocator());
      }
      return in;
   }
   
   private static void initServices()
   {
      if(sys == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr();
         summ = PSCmsContentSummariesLocator.getObjectManager();
         sys = PSSystemServiceLocator.getSystemService();         
      }
   }
   
 
   public static final String WORKFLOW_STATES = "workflow_states";
   /**
    * @param gmgr The gmgr to set.
    */
   public static void setGmgr(IPSGuidManager gmgr)
   {
      PSORevisionCorrectingItemFilter.gmgr = gmgr;
   }

   /**
    * @param summ The summ to set.
    */
   public static void setSumm(IPSCmsContentSummaries summ)
   {
      PSORevisionCorrectingItemFilter.summ = summ;
   }

   /**
    * @param sys The sys to set.
    */
   public static void setSys(IPSSystemService sys)
   {
      PSORevisionCorrectingItemFilter.sys = sys;
   } 
}
