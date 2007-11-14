/*
 * com.percussion.pso.jexl PSONavTools.java
 * 
 * @copyright 2006 Percussion Software, Inc. All rights reserved.
 * See license.txt for detailed restrictions. 
 * 
 * @author DavidBenua
 *
 */
package com.percussion.pso.jexl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.assembly.impl.nav.PSNavHelper;
import com.percussion.services.assembly.jexl.PSAssemblerUtils;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.PSSiteHelper;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.exceptions.PSBaseException;
import com.percussion.utils.jexl.PSJexlEvaluator;

/**
 * Binding functions usefule for Navigation.
 * These functions are intended to be called from JEXL.   
 *
 * @author DavidBenua
 *
 */
public class PSONavTools extends PSJexlUtilBase implements IPSJexlExpression
{
   
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSONavTools.class);   
   
   private static IPSGuidManager gmgr = null; 
   
   private static IPSRhythmyxInfo rxinfo = null; 
   
   private static IPSAssemblyService asm = null; 
   /**
    * 
    */
   public PSONavTools()
   {
      super();
   }
   
   /**
    * Builds a list of ancestor nodes.  
    * The first node in the list will be the <code>Root</code> node
    * and the last node will be the <code>Self</code> node. If
    * the <code>Self</code> node is the <code>Root</code> node, 
    * there will be only one node in the list.
    * <p>
    * This function can be used to generate breadcrumbs. Here is
    * a simple example: 
    * <pre>
    * &lt;div class="BreadCrumbs">
    *   #set($bpath = $user.psoNavTools.getAncestors($nav.self))      
    *   #foreach($bcrumb in $bpath)
    *       #set($landing_page = $bcrumb.getProperty("nav:url").String)
    *       #set($title = $bcrumb.getProperty("rx:displaytitle").String)
    *       #if( $landing_page )
    *          &lt;a href="$landing_page">$title&lt;/a>
    *       #else
    *          $title
    *       #end
    *   #end    
    * &lt;/div>
    * </pre>
    * 
    * @param selfNode the self node, usually <code>$nav.self</code>
    * @return The list of ancestors, including the self node. 
    * Never <code>null</code> or <code>empty</code>.
    * @throws PSExtensionProcessingException
    */
   @IPSJexlMethod(description="get the ancestors for this node", 
         params={@IPSJexlParam(name="selfNode", description="the current item")})
   public List<Node> getAncestors(Node selfNode) 
      throws PSExtensionProcessingException
   {
      if(selfNode == null)
      {
         String emsg = "Self Node cannot be null"; 
         log.error(emsg);
         throw new IllegalArgumentException(emsg); 
      }
      ArrayList<Node> ancestors = new ArrayList<Node>(); 
      Node node = selfNode; 
      
      try
      {
         while(node != null)
         {
            ancestors.add(0, node);
            log.trace("adding node " + node.getName() + " depth " + node.getDepth());
            if(node.getDepth() == 0)
            {
               log.trace("Depth is 0"); 
               break;
            }
            node = node.getParent(); 
         }
      /*
       * The JCR Javadoc says that calling getParent() from the root will
       * throw this exception.  However, the Rhythmyx implementation will
       * just return NULL instead. 
       */
      } catch (ItemNotFoundException ie)
      { 
        log.trace("item not found, might be root");
        //not really an error: we are done 
      } catch (Exception e)
      {
        String emsg = "Unexpected Exception " + e.getMessage(); 
        log.error(emsg, e); 
        throw new PSExtensionProcessingException("PSONavTools", e); 
      }
      
      return ancestors;
   }
   
   @IPSJexlMethod(description="Gets the Nav Node for a Page item",
          params={@IPSJexlParam(name="assemblyItem", description="Assembly Item to find Nav Node")})
   public Map<String,Object> getNavNode(IPSAssemblyItem assemblyItem) 
      throws PSCmsException, PSFilterException, RepositoryException
   {
      initServices(); 
      PSAssemblyWorkItem work = (PSAssemblyWorkItem)assemblyItem;
      PSJexlEvaluator eval = new PSJexlEvaluator(assemblyItem.getBindings());
      PSLocator home = gmgr.makeLocator(work.getId()); 
      log.debug("home is " + home.getId());
      PSNavHelper helper =  work.getNavHelper();
      
      IPSNode navon = (IPSNode) helper.findNavNode(work);
      if(navon == null)
      {
         log.warn("cannot find navon for item");
         return new HashMap<String, Object>(); 
      }
      PSLocator navLoc = gmgr.makeLocator(navon.getGuid());
      log.debug("nav is " + navLoc.getId());
     
      helper.setupNavValues(work, eval, navon);
      Map<String,Object> nav = (Map)work.getBindings().get("$nav");  
      IPSNode root = (IPSNode) nav.get("root"); 
      PSLocator rootLoc = gmgr.makeLocator(root.getGuid());
      log.debug("root is " + rootLoc.getId()); 
      IPSNode self = (IPSNode) nav.get("self");
      PSLocator selfLoc = gmgr.makeLocator(self.getGuid());
      log.debug("self is " + rootLoc.getId()); 
      
      return nav;
   }

   @IPSJexlMethod(description="Gets the Nav Node for a Page item",
         params={@IPSJexlParam(name="assemblyItem", description="Assembly Item to find Nav Node")})
   public Map<String, Object> getNav(IPSAssemblyItem assemblyItem) throws Throwable
   {
      IPSAssemblyResult navon = getAssembledNavon(assemblyItem);
      log.debug("Navon id is " + navon.getId()); 
      Map<String,Object> bindings = navon.getBindings(); 
      Map<String,Object> nav = new HashMap<String, Object>(); 
      nav.putAll((Map<String, Object>) bindings.get("$nav"));
      return nav; 
   }
   
   
   
   private IPSAssemblyResult getAssembledNavon(IPSAssemblyItem sysItem) throws Throwable 
   {
       initServices();
       IPSAssemblyItem item = (IPSAssemblyItem) sysItem.clone(); 
       String emsg; 
       Properties navProps = getNavProperties(); 
       String navSlotName = StringUtils.substringBefore(navProps.getProperty(NAVON_SLOTNAMES), ",");
       log.debug("Navon slot name " + navSlotName); 
       String navTemplateName = navProps.getProperty(NAVON_VARIANT_INFO);
       log.debug("Navon template name " + navTemplateName); 
       IPSTemplateSlot navSlot = asm.findSlotByName(navSlotName); 
       PSAssemblerUtils asmHelper = new PSAssemblerUtils();
       Map<String,Object> slotParms = new HashMap<String, Object>(); 
       slotParms.put("template", navTemplateName);
       
       //Map<String, String> variables = item.getVariables();
       Map<String,String[]> sysParams = item.getParameters();
       
       //String siteid = sysParams.get(IPSHtmlParameters.SYS_SITEID)[0]; 
       //String contextid = sysParams.get(IPSHtmlParameters.SYS_CONTEXT)[0]; 
      // PSSiteHelper.setupSiteInfo(eval, siteid, contextid);
       Map<String, Object> combinedParms = asmHelper.combine(sysParams, slotParms); 
       List<IPSAssemblyResult> results = asmHelper.assemble(item, navSlot, combinedParms); 
       if(results.size() == 0)
       {
          emsg = "unable to assemble navon in slot " + navSlotName + " for item " + item.getId(); 
          log.error(emsg); 
          throw new RuntimeException(emsg); 
       }
       return results.get(0); 
   }
   
   private static void initServices()
   {
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
         rxinfo = PSRhythmyxInfoLocator.getRhythmyxInfo(); 
         asm = PSAssemblyServiceLocator.getAssemblyService(); 
         
         navPropertiesFile = (String)rxinfo.getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY)
         + File.separator + "rxconfig/Server/Navigation.properties";

      }
   }
   
   protected synchronized Properties getNavProperties()
   {
      initServices(); 
      File propsFile = new File(navPropertiesFile);
      long lastmodified = propsFile.lastModified();
      if(navProperties == null || lastmodified != npLastModified )
      {
         navProperties = new Properties();
         try
         {
            FileInputStream fis = new FileInputStream(propsFile);
            navProperties.load(fis);
         } catch (Exception ex)
         {
            log.error("Exception loading navProperties " + ex,ex);
            throw new RuntimeException("unable to load nav properties"); 
         } 
      }
      
      return navProperties;
   }
   
   private long npLastModified = 0L; 
   
   private Properties navProperties = null;
   
   private static String navPropertiesFile = null; 
   
   public static final String NAVON_SLOTNAMES = "navon.slotnames"; 
   
   public static final String NAVON_VARIANT_INFO = "navon.variant.info"; 
}
