/*
 * com.percussion.pso.workflow EditionInitiator.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.workflow;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.percussion.publisher.server.PSPublisherHandlerResponse;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class EditionInitiator implements Runnable
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(EditionInitiator.class);

   QueuedEdition edition; 
   /**
    * 
    */
   public EditionInitiator(QueuedEdition edition)
   {
      this.edition = edition;
   }
   /**
    * @see java.lang.Runnable#run()
    */
   public void run()
   {
      try
      {
         Document pageResult = callServer();
         String code = getResponseCode(pageResult); 
         if(StringUtils.isNotBlank(code) && 
            code.equals(PSPublisherHandlerResponse.RESPONSE_CODE_INPROGRESS))
         {
            log.info("Edition started successfully " + edition.getEditionId());
         }
         else
         {
            log.info("Edition " + edition.getEditionId() +  
                  " not Started, response code is " + code);
            //something wrong, retry 
            if(edition.decrementAndTestRetries())
            {
               log.debug("retry edition"); 
            }
         }
      } catch (HttpException ex)
      {
         log.error("Http Exception " + ex.getMessage(), ex); 
         
      } catch (IOException ex)
      {
         log.error("IO Exception " + ex.getMessage(), ex); 
         
      } catch (SAXException ex)
      {
         log.error("Sax Exception " + ex.getMessage(), ex);
      }
   }
   
   private Document callServer() throws HttpException, IOException, SAXException
   {
      HttpClient client = new HttpClient();
      StringBuilder uri = new StringBuilder(); 
      uri.append(edition.getUri());
      uri.append(":"); 
      uri.append(edition.getListenerPort()); 
      uri.append("/Rhythmyx/sys_pubHandler/publisher.xml"); 
      
      log.debug("Edition URL is " + uri.toString());
      GetMethod method = new GetMethod(uri.toString());
      
      NameValuePair editparm = new NameValuePair("editionid", edition.getEditionId()); 
      NameValuePair pubparm = new NameValuePair("PUBAction", "publish"); 
      if(edition.isLocal())
      {
         NameValuePair sessparm = new NameValuePair("pssessionid",edition.getSessionId());
         NameValuePair[] putparms = { editparm, pubparm, sessparm };  
         method.setQueryString(putparms); 
      }
      else 
      {
         client.getState().setCredentials(null, edition.getUri(), 
               new UsernamePasswordCredentials(edition.getCmsUser(), edition.getCmsPassword()));
         method.addRequestHeader("RX_USEBASICAUTH", "yes");
         method.setDoAuthentication(true);
      }
      
      client.executeMethod(method);
      byte[] response = method.getResponseBody();
      method.releaseConnection(); 
      
      return PSXmlDocumentBuilder.createXmlDocument(new InputSource(new ByteArrayInputStream(response)),false);
   }
   
   private String getResponseCode(Document doc)
   {
      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc.getDocumentElement()); 
      Element resp = walker.getNextElement(PSPublisherHandlerResponse.ELEM_RESPONSE);
      if(resp != null)
      {
         String code = resp.getAttribute(PSPublisherHandlerResponse.ATTR_CODE);
         log.debug("Response code is " + code);
         return code;
      }
      return null;
   }
}
