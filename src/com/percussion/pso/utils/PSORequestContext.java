/*
 * com.percussion.pso.utils PSORequestContext.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;

import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.util.IPSHtmlParameters;

/**
 * A system request that overrides the PSRequestContext. 
 * Use this class to obtain an IPSRequestContext for the
 * system user (RxServer).   
 *
 * @author DavidBenua
 *
 */
public class PSORequestContext extends PSRequestContext
      implements
         IPSRequestContext
{ 
   /**
    * Gets a the system user request.  
    * This system request is always forced to be local to the server, even if 
    * the original user request came from elsewhere. 
    */
   public PSORequestContext()
   {
      super(PSRequest.getContextForRequest(true));
   }
   
   /**
    * Gets the system user request, specifying a community. 
    * @param CommunityId
    */
   public PSORequestContext(String CommunityId)
   {
   	 this();
   	 this.setCommunity(CommunityId);
   }
   /**
    * This method always returns <code>false</code>. 
    * System requests cannot trace, beccause there is no home application. 
    * @see com.percussion.server.IPSRequestContext#isTraceEnabled()
    */
   public boolean isTraceEnabled()
   {
      return false;
   }
   
   /**
    * Sets the user community.  
    * @param communityId the Community Id to set. 
    */
   public void setCommunity(String communityId)
   {
   	super.setPrivateObject(IPSHtmlParameters.SYS_COMMUNITY, communityId);
   }
  
}
