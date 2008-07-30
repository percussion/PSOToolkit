/*
 * test.percussion.pso.utils PSONodeCatalogerTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.utils;

import static org.junit.Assert.*;

import java.util.List;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.services.contentmgr.IPSContentMgr;

public class PSONodeCatalogerTest
{

   private static Log log = LogFactory.getLog(PSONodeCatalogerTest.class); 
   
   Mockery context; 
   PSONodeCataloger cut; 
   IPSContentMgr cmgr; 
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(); 
      cut = new PSONodeCataloger();
      cmgr = context.mock(IPSContentMgr.class); 
      cut.setCmgr(cmgr);
      
   }
   @Test
   public final void testGetContentTypeNames()
   {
      log.info("Getting content type names");
      final Sequence ctypes = context.sequence("ctypes"); 
      final NodeTypeIterator nodes = context.mock(NodeTypeIterator.class);
      final NodeType t1 = context.mock(NodeType.class); 
      final NodeType t2 = context.mock(NodeType.class); 
      
      try
      {
         context.checking(new Expectations()
         {{
               one(cmgr).getAllNodeTypes(); inSequence(ctypes);
               will(returnValue(nodes)); 
               one(nodes).hasNext(); inSequence(ctypes);
               will(returnValue(true)); 
               one(nodes).nextNodeType(); inSequence(ctypes); 
               will(returnValue(t1));
               one(nodes).hasNext(); inSequence(ctypes);
               will(returnValue(true));
               one(nodes).nextNodeType(); inSequence(ctypes); 
               will(returnValue(t2));
               one(nodes).hasNext(); inSequence(ctypes);
               will(returnValue(false));
               allowing(t1).getName(); 
               will(returnValue("type1"));
               allowing(t2).getName(); 
               will(returnValue("type2"));
         }});
         
         List<String> names = cut.getContentTypeNames();
         assertNotNull(names);
         assertEquals(2,names.size()); 
         assertEquals("type1", names.get(0));
         assertEquals("type2", names.get(1));
         
         
      } catch (Exception e)
      {
         log.error("Unexpected Exception" + e, e); 
         fail("Exception");
      }
      log.info("test complete"); 
   }
   
   @Test
   public final void testGetContentTypeNamesWithField()
   {
      log.info("Getting content type names with field");
      final Sequence ctypes = context.sequence("ctypes"); 
      final NodeTypeIterator nodes = context.mock(NodeTypeIterator.class);
      final NodeType t1 = context.mock(NodeType.class); 
      final NodeType t2 = context.mock(NodeType.class); 
      final PropertyDefinition p1 = context.mock(PropertyDefinition.class); 
      final PropertyDefinition p2 = context.mock(PropertyDefinition.class); 
      final PropertyDefinition p3 = context.mock(PropertyDefinition.class); 
      
      final PropertyDefinition[] t1p = new PropertyDefinition[]{p1,p2,p3};
      final PropertyDefinition[] t2p = new PropertyDefinition[]{p1,p3};
      
      try
      {
         context.checking(new Expectations()
         {{
               one(cmgr).getAllNodeTypes(); inSequence(ctypes);
               will(returnValue(nodes)); 
               one(nodes).hasNext(); inSequence(ctypes);
               will(returnValue(true)); 
               one(nodes).nextNodeType(); inSequence(ctypes); 
               will(returnValue(t1));
               one(nodes).hasNext(); inSequence(ctypes);
               will(returnValue(true));
               one(nodes).nextNodeType(); inSequence(ctypes); 
               will(returnValue(t2));
               one(nodes).hasNext(); inSequence(ctypes);
               will(returnValue(false));
               allowing(t1).getName(); 
               will(returnValue("type1"));
               allowing(t2).getName(); 
               will(returnValue("type2"));
               one(t1).getDeclaredPropertyDefinitions();
               will(returnValue(t1p));
               one(t2).getDeclaredPropertyDefinitions();
               will(returnValue(t2p));
               allowing(p1).getName();
               will(returnValue("prop1"));
               allowing(p2).getName();
               will(returnValue("prop2"));
               allowing(p3).getName();
               will(returnValue("prop3"));
               
         }});
         
         List<String> names = cut.getContentTypeNamesWithField("prop2"); 
         assertNotNull(names);
         assertEquals(1,names.size()); 
         assertEquals("type1", names.get(0));
         
         
      } catch (Exception e)
      {
         log.error("Unexpected Exception" + e, e); 
         fail("Exception");
      }
      log.info("test complete"); 
   }
   @Test
   public final void testGetFieldNamesForContentType()
   {
      log.info("Getting content type names with field");
  
      final NodeTypeIterator nodes = context.mock(NodeTypeIterator.class);
      final NodeType t1 = context.mock(NodeType.class); 
      final PropertyDefinition p1 = context.mock(PropertyDefinition.class); 
      final PropertyDefinition p2 = context.mock(PropertyDefinition.class); 
      final PropertyDefinition p3 = context.mock(PropertyDefinition.class); 
      
      final PropertyDefinition[] t1p = new PropertyDefinition[]{p1,p2,p3};
      
      try
      {
         context.checking(new Expectations()
         {{
               one(cmgr).getNodeType("type1"); 
               will(returnValue(t1));
               allowing(t1).getName(); 
               will(returnValue("type1"));
               one(t1).getDeclaredPropertyDefinitions();
               will(returnValue(t1p));
               allowing(p1).getName();
               will(returnValue("prop1"));
               allowing(p2).getName();
               will(returnValue("prop2"));
               allowing(p3).getName();
               will(returnValue("prop3"));
               
         }});
         
         List<String> names = cut.getFieldNamesForContentType("type1"); 
         assertNotNull(names);
         assertEquals(3,names.size()); 
         assertEquals("prop1", names.get(0));
         
         
      } catch (Exception e)
      {
         log.error("Unexpected Exception" + e, e); 
         fail("Exception");
      }
      log.info("test complete"); 
   }
}
