/*
 * com.percussion.pso.utils PSONodeCataloger.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;

/**
 * Finds Property Definitions for a given NodeType.
 * This can be used to catalog node types;   
 *
 * @author DavidBenua
 *
 */
public class PSONodeCataloger
{
   private static Log log = LogFactory.getLog(PSONodeCataloger.class);
   
   private IPSContentMgr cmgr = null; 
   
   /**
    * Sole constructor. 
    */
   public PSONodeCataloger()
   {
      
   }
   
   /**
    * Initialize service pointers. 
    */
   private void init()
   {
      if(cmgr == null)
      {
         cmgr = PSContentMgrLocator.getContentMgr();
      }
   }
   
   /**
    * Get Content Type Names
    * @return return the list of content type names defined in the system. 
    * @throws RepositoryException
    */
   public List<String> getContentTypeNames() throws RepositoryException 
   {
      init();
      log.trace("getting content type names"); 
      List<String> names = new ArrayList<String>(); 
      
      NodeTypeIterator nodeTypes = cmgr.getAllNodeTypes();
      while(nodeTypes.hasNext())
      {
         NodeType nt = nodeTypes.nextNodeType();
         names.add(nt.getName());
      }
      log.debug("content type names " + names); 
      return names;
   }
   
   
   /**
    * Get Content Type Names with a specified field; 
    * @param fieldName the field name to search for.
    * @return the names of the content types.  Never <code>null</code> but may be 
    * <code>empty</code>
    * @throws RepositoryException if the content type is not found. 
    */
   public List<String> getContentTypeNamesWithField(String fieldName) throws RepositoryException
   {
      init();
      List<String> names = new ArrayList<String>(); 
      
      NodeTypeIterator nodeTypes = cmgr.getAllNodeTypes();
      while(nodeTypes.hasNext())
      {
         NodeType nt = nodeTypes.nextNodeType(); 
         PropertyDefinition[] props = nt.getDeclaredPropertyDefinitions();
         for(PropertyDefinition p : props)
         {
             if(p.getName().equals(fieldName) )
             {
                names.add(nt.getName());
                break; 
             }
         }
      }
      log.debug("content type names for field " + fieldName + " -- " + names); 
      return names;
      
   }

   /**
    * Gets the field names for a given content type
    * @param typeName the type name
    * @return the list of field names. Never <code>null</code> but may be 
    * <code>empty</code>
    * @throws NoSuchNodeTypeException
    * @throws RepositoryException
    */
   public List<String> getFieldNamesForContentType(String typeName) throws NoSuchNodeTypeException, RepositoryException
   {
      init();
      List<String> names = new ArrayList<String>(); 
      NodeType nt = cmgr.getNodeType(typeName); 
      PropertyDefinition[] props = nt.getDeclaredPropertyDefinitions();
      for(PropertyDefinition p : props)
      {
         names.add(p.getName());
      }
   
      log.debug("field names for content type " + typeName + " -- " + names); 
      return names; 
   }

   /**
    * Set the content manager service pointer.  Used only for unit testing. 
    * @param cmgr the cmgr to set
    */
   public void setCmgr(IPSContentMgr cmgr)
   {
      this.cmgr = cmgr;
   }
   
}
