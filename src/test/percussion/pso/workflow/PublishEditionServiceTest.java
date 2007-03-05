/*
 * test.percussion.pso.workflow PublishEditionServiceTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.workflow;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import com.percussion.pso.workflow.PublishEditionService;
import junit.framework.TestCase;

public class PublishEditionServiceTest extends TestCase
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory
         .getLog(PublishEditionServiceTest.class);

   PublishEditionService svc = null; 
   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(PublishEditionServiceTest.class);
   }
   protected void setUp() throws Exception
   {
      super.setUp();
      
      Resource res = new FileSystemResource("WEB-INF/config/user/spring/PSOPublishEdition-beans.xml");
      XmlBeanFactory factory = new XmlBeanFactory(res);
      
      svc = (PublishEditionService) factory.getBean("PSOPublishEditionService");
   }
   
   public void testService()
   {
      assertNotNull(svc);
      assertEquals("9932", svc.getListenerPort());
   }
   
   public void testFindEdition()
   {
      assertEquals(314, svc.findEdition(5, 301, 1001));
      assertEquals(315, svc.findEdition(5, 301, 1002));
      
      try
      {
         log.info("Expect not to find workflow 6"); 
         svc.findEdition(6, 301, 1001);
         fail("expected exception, invalid workflow id"); 
      }
      catch (IllegalArgumentException iae) 
      {
         //iae.printStackTrace(); 
         //this is expected
      }
      
      try
      {
         log.info("Expect not to find transition 304");
         svc.findEdition(5, 304, 1001);
         fail("expected exception, invalid transition id");
      }
      catch (IllegalArgumentException iae)
      {
         //this is expected         
      }
   }
}
