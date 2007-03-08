/*
 * com.percussion.pso.workflow PublishEditionService.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.InitializingBean;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class PublishEditionService implements InitializingBean
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PublishEditionService.class);

   private String baseUrl = "http://127.0.0.1";  
   private String listenerPort = null;
   //if local is true we expect to run as the caller, otherwise need CMS user and password
   private boolean local = true; 
   private String cmsUser = null; 
   private String cmsPassword = null; 
   private int retryCount = 10;
   
   private ScheduledThreadPoolExecutor execr = new ScheduledThreadPoolExecutor(1);
   
   private static IPSRhythmyxInfo rxinfo = null; 
   
   /*
    * Map of workflows
    *    Map of transitions
    *       Map of communities
    *          Value is edition
    */
   private Map<String,Map<String,Map<String,String>>> workflows 
      = new HashMap<String,Map<String,Map<String,String>>>();
   
   /**
    * 
    */
   public PublishEditionService()
   {
      
   }
   
   private static void initServices()
   {
      if(rxinfo == null)
      { 
         rxinfo = PSRhythmyxInfoLocator.getRhythmyxInfo();
      }
   }
   /**
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      log.debug("Base URL is " + baseUrl); 
      log.debug("listener port is " + listenerPort); 
      if(listenerPort == null)
     {
         initServices();
         listenerPort = rxinfo.getProperty(IPSRhythmyxInfo.Key.LISTENER_PORT).toString();
      }
      log.debug("CMS User is " + this.cmsUser);
      log.debug("CMS Password is " + this.cmsPassword); 
      log.debug("Local is " + this.local);
   }

   public void runQueuedEdition(QueuedEdition ed)
   {
      execr.execute(new EditionInitiator(ed));   
   }
   
   public void retryQueuedEdition(QueuedEdition ed)
   {
      if(ed.decrementAndTestRetries())
      {
         long delay = Math.round(Math.random()*10); //sleep 0 to 10 seconds
         log.debug("Edition delayed " + delay + " seconds"); 
         execr.schedule(new EditionInitiator(ed), delay, TimeUnit.SECONDS); 
      }
      else
      {
         log.debug("out of retries " + ed); 
      }
   }
   
   public int findEdition(int workflow, int transition, int community)
   {
      String workKey = String.valueOf(workflow); 
      Map<String,Map<String,String>> workMap = workflows.get(workKey);
      if(workMap == null)
      {
         String emsg = "Workflow not in configuration file " + workflow;
         log.error(emsg); 
         throw new IllegalArgumentException(emsg);
      }
      String transKey = String.valueOf(transition); 
      Map<String,String> transMap = workMap.get(transKey);
      if(transMap == null)
      {
         String emsg = "Transition " +  transition + " not in configuration file for workflow " + workflow;
         log.error(emsg); 
         throw new IllegalArgumentException(emsg);         
      }
      String commKey = String.valueOf(community);
      String edition  = transMap.get(commKey); 
      if(edition == null)
      {
         String emsg = "Community " +  community + " not in configuration file for workflow " + workflow
          + " and transition " + transition;
         log.error(emsg); 
         throw new IllegalArgumentException(emsg);
      }      
      return Integer.parseInt(edition);
   }
   
   protected QueuedEdition makeQueuedEdition(String editionId, String sessionId)
   {
      
      QueuedEdition result = new QueuedEdition(baseUrl,listenerPort,editionId,this.isLocal(), retryCount); 
      if(isLocal())
      {
         result.setSessionId(sessionId); 
      }
      else
      {
         result.setCmsUser(this.cmsUser);
         result.setCmsPassword(this.cmsPassword); 
      }    
      return result; 
   }
   
   
   /**
    * @return Returns the baseUrl.
    */
   public String getBaseUrl()
   {
      return baseUrl;
   }
   /**
    * @param baseUrl The baseUrl to set.
    */
   public void setBaseUrl(String baseUrl)
   {
      this.baseUrl = baseUrl;
   }
   /**
    * @return Returns the listenerPort.
    */
   public String getListenerPort()
   {
      return listenerPort;
   }
   /**
    * @param listenerPort The listenerPort to set.
    */
   public void setListenerPort(String listenerPort)
   {
      this.listenerPort = listenerPort;
   }
   /**
    * @return Returns the workflows.
    */
   public Map<String, Map<String, Map<String, String>>> getWorkflows()
   {
      return workflows;
   }
   /**
    * @param workflows The workflows to set.
    */
   public void setWorkflows(
         Map<String, Map<String,  Map<String, String>>> workflows)
   {
      this.workflows = workflows;
   }
   /**
    * @return Returns the cmsPassword.
    */
   public String getCmsPassword()
   {
      return cmsPassword;
   }
   /**
    * @param cmsPassword The cmsPassword to set.
    */
   public void setCmsPassword(String cmsPassword)
   {
      this.cmsPassword = cmsPassword;
      log.debug("Setting CMS Password"); 
      this.local = false; 
   }
   /**
    * @return Returns the cmsUser.
    */
   public String getCmsUser()
   {
      return cmsUser;
   }
   /**
    * @param cmsUser The cmsUser to set.
    */
   public void setCmsUser(String cmsUser)
   {
      this.cmsUser = cmsUser;
      log.debug("Setting CMS User " + cmsUser); 
      this.local = false;
   }
   /**
    * @return Returns the local.
    */
   public boolean isLocal()
   {
      return local;
   }
   /**
    * @param local The local to set.
    */
   public void setLocal(boolean local)
   {
      this.local = local;
   }

   /**
    * @return Returns the retryCount.
    */
   public int getRetryCount()
   {
      return retryCount;
   }

   /**
    * @param retryCount The retryCount to set.
    */
   public void setRetryCount(int retryCount)
   {
      this.retryCount = retryCount;
   }
}
