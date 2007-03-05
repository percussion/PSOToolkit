package com.percussion.pso.workflow;
import com.percussion.services.PSBaseServiceLocator;
/*
 *  PSOPublishEditionServiceLocator.java
 *  
 * @author DavidBenua
 *
 */
/**
 * 
 *
 * @author DavidBenua
 *
 */
public class PSOPublishEditionServiceLocator extends PSBaseServiceLocator
{
   /**
    * static methods only, never constructed. 
    */
   private PSOPublishEditionServiceLocator()
   {
      
   }
   
   public static PublishEditionService getPublishEditionService()
   { 
      return (PublishEditionService) getBean(PSOPUBLISHSERVICEBEAN);    
   }
   
   private static final String PSOPUBLISHSERVICEBEAN = "PSOPublishEditionService"; 
}
