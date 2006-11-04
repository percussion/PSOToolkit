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

import java.util.ArrayList;
import java.util.List;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase;

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
}
