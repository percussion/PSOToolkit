/*
 * com.percussion.pso.jexl PSOObjectFinder.java
 *
 * @COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * @author davidbenua
 *
 */
package com.percussion.pso.jexl;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSException;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.utils.PSOItemSummaryFinder;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * JEXL function for locating various legacy objects by GUID. 
 * These functions are commonly available in the Java API, but 
 * not directly accessible in JEXL. 
 *
 * @author davidbenua
 *
 */
public class PSOObjectFinder extends PSJexlUtilBase
      implements
         IPSJexlExpression
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOObjectFinder.class);
 
   /**
    * Content Web Service pointer. 
    */
   private static IPSContentWs cws = null; 
   
   
   /**
    * 
    */
   public PSOObjectFinder()
   {
      super();
      
   }
   
   /**
    * Initialize Java services. Must be called before any 
    * Java Services are accessed. 
    */
   private static void initServices()
   {
      if(cws == null)
      {
      cws = PSContentWsLocator.getContentWebservice();
      }
   }
   
   /**
    * Gets the Legacy Component Summary for an item by GUID. 
    * @param guid the item GUID.
    * @return the Component Summary for the item. Never <code>null</code>
    * @throws PSException when the item is not found. 
    */
   @IPSJexlMethod(description="get the Legacy Component Summary for an item",
         params={@IPSJexlParam(name="guid",description="the item GUID")})
   public PSComponentSummary getComponentSummary(IPSGuid guid) throws PSException
   {
      return PSOItemSummaryFinder.getSummary(guid); 
   }
   
   /**
    * Gets the content type summary for a content type by GUID. 
    * @param guid the Content Type GUID
    * @return the content type summary or <code>null</code> if the 
    * content type is not found. 
    */
   @IPSJexlMethod(description="get the content type summary for a specified type",
         params={@IPSJexlParam(name="guid",description="the content type GUID")}) 
   public PSContentTypeSummary getContentTypeSummary(IPSGuid guid)
   {
      initServices();
      List<PSContentTypeSummary> ctypes = cws.loadContentTypes(null); 
      for(PSContentTypeSummary ctype : ctypes)
      {
         if(ctype.getGuid().longValue() == guid.longValue())
         {
            log.debug("found Content type" + ctype.getName()); 
            return ctype; 
         }
      }
      return null;
   }
 
   /**
    * Gets the JSESSIONID value for the current session.
    * @return the jsessionid
    * @deprecated in 6.5 and later, replaced by PSSessionUtils.getJSessionId(). 
    */
   @IPSJexlMethod(description="Get the JSESSIONID value for the current request",
      params={})
   public String getJSessionId()
   {
       String jsession = PSRequestInfo.
           getRequestInfo(PSRequestInfo.KEY_JSESSIONID).toString();
       log.debug("JSESSIONID=" + jsession);
       return jsession;
   }
   
   /**
    * Gets the PSSessionId for the current session. 
    * @return the pssessionid. 
    * @deprecated in 6.5 and later, replaced by PSSessionUtils.getPSSessionId.
    */
   @IPSJexlMethod(description="Get the PSSESSIONID value for the current request",
         params={})
   public String getPSSessionId()
   {
      PSRequest req = (PSRequest)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      String sessionid = req.getUserSessionId();
      log.debug("PSSessionId=" + sessionid ); 
      return sessionid;
   }
   
   /**
    * Gets the users current locale.
    * @return the users current locale, or <code>null</code> if none is defined. 
    */
   @IPSJexlMethod(description="get the users current locale",
         params={})
   public String getUserLocale()
   {
      PSUserSession session = getSession();
      Object obj = session.getPrivateObject(IPSHtmlParameters.SYS_LANG);
      if(obj != null)
      {
         return obj.toString(); 
      }
      return null;
   }
   
   /**
    * Gets the name of the current user community.
    * @return the user community name, or <code>null</code> if none
    * is defined. 
    */
   @IPSJexlMethod(description="get the users current community name",
         params={})
   public String getUserCommunity()
   {
      PSUserSession session = getSession();
      return session.getUserCurrentCommunity();       
   }

   /**
    * Gets the users current community id. 
    * @return the community id, or <code>null</code> if none is defined. 
    */
   @IPSJexlMethod(description="get the users current community id",
         params={})
   public String getUserCommunityId()
   {
      PSUserSession session = getSession();
      Object obj = session.getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);
      if(obj != null)
      {
         return obj.toString(); 
      }
      return null;
   }
   
   /**
    * Gets the user session. 
    * @return the user session
    */
   private PSUserSession getSession()
   {
      PSRequest req = (PSRequest)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSUserSession session = req.getUserSession();
      return session; 
   }
   
   /**
    * @param cws The cws to set. This routine should only be used
    * for unit testing. 
    */
   public static void setCws(IPSContentWs cws)
   {
      PSOObjectFinder.cws = cws;
   }
   

}
