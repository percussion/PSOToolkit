/*
 * com.percussion.pso.workflow QueuedEdition.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.workflow;


/**
 * A Data Object describing an edition to be run in the future. 
 *
 * @author DavidBenua
 *
 */
public class QueuedEdition 
{
   private boolean isLocal = false;
   private String editionId; 
   private String sessionId = null; 
   private String cmsUser = null;
   private String cmsPassword = null; 
   private String uri; 
   private String listenerPort; 
   private int retryCount = 0; 
   /**
    * 
    */
   public QueuedEdition(String uri,String port, String editionId, boolean local, int retries )
   {
       this.uri = uri; 
       this.listenerPort = port;
       this.editionId = editionId;
       this.isLocal = local; 
       this.retryCount = retries; 
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
   }
   /**
    * @return Returns the editionId.
    */
   public String getEditionId()
   {
      return editionId;
   }
   /**
    * @param editionId The editionId to set.
    */
   public void setEditionId(String editionId)
   {
      this.editionId = editionId;
   }
   /**
    * @return Returns the sessionId.
    */
   public String getSessionId()
   {
      return sessionId;
   }
   /**
    * @param sessionId The sessionId to set.
    */
   public void setSessionId(String sessionId)
   {
      this.sessionId = sessionId;
   }
   /**
    * @return Returns the uri.
    */
   public String getUri()
   {
      return uri;
   }
   /**
    * @param uri The uri to set.
    */
   public void setUri(String uri)
   {
      this.uri = uri;
   }
   /**
    * @return Returns the local flag.
    */
   public boolean isLocal()
   {
      return isLocal;
   }

   /**
    * @return Returns the retryCount.
    */
   public int getRetryCount()
   {
      return retryCount;
   }
   
   public boolean decrementAndTestRetries()
   {
      retryCount--;
      return(retryCount > 0);
   }

   /**
    * @return Returns the listenerPort.
    */
   public String getListenerPort()
   {
      return listenerPort;
   }
}
