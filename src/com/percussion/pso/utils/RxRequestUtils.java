/*
 * com.percussion.pso.utils RxRequestUtils.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;
import javax.servlet.ServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.server.IPSRequestContext;

/**
 * Helper methods for accessing Rhythmyx request objects. 
 *
 * @author DavidBenua
 *
 */
public class RxRequestUtils
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(RxRequestUtils.class);
   
   /**
    * Static methods only 
    */
   private RxRequestUtils()
   {
   }
   
   /**
    * Gets the Rhythmyx request for this servlet request.
    * @param req the calling servlet's request
    * @return the Rhythmyx request context or null if this request
    * did not originate in Rhythmyx. 
    */
   public static IPSRequestContext getRequest(ServletRequest req)
   {
      return (IPSRequestContext) req.getAttribute(REQUEST_ATTRIBUTE); 
   }
 
   /**
    * Convience function to get the rx username from a servlet request.
    * @param req
    * @return the user name
    */
   public static String getUserName(ServletRequest req) {
      IPSRequestContext irq = getRequest(req);
      return irq.getUserName();
   }
   
   public static String getSessionId(ServletRequest req)
   {
      IPSRequestContext irq = getRequest(req); 
      if(irq == null)
      {
         throw new IllegalStateException("Rhythmyx Request not found");
      }
      String sessionid = irq.getUserSessionId();
      log.debug("Session ID from request: " + sessionid); 

      String community = (String)irq.getSessionPrivateObject("sys_community");
      log.debug("Community is " + community); 
      return  sessionid; 
   }
   
 
   public static final String REQUEST_ATTRIBUTE = "RX_REQUEST_CONTEXT"; 
}
