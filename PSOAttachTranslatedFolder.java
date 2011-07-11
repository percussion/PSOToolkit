/*******************************************************************************
 * © 2005-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the “Software”) for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.effects;

/******************************************************************************
*
* [ PSAttachTranslatedFolder.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemDesignWs;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
* This effect typically is attached to {@link com.percussion.design.
* objectstore.PSRelationshipConfig#CATEGORY_TRANSLATION translation} 
* category of relationships.
* <p>
* The newly translated object (item or folder) is attached to all parent 
* folders. Parent folders are determined as follows:
* <ol>
*    <li>
*    Collect all parent folders of the translated object where the folder 
*    is the owner of the translation relationship and the translation 
*    relationship name matches the name of the currently processed translation 
*    relationship.
*    </li>
*    <li>
*    Collect all parent folders of the translated object where the folder 
*    is the dependent of the translation relationship and the translation 
*    relationship name matches the name of the currently processed translation 
*    relationship.
*    </li>
*    <li>
*    Determine the locale through the dependent of the current translation 
*    relationship.
*    </li>
*    <li>
*    From all collected parent folders weed out the ones which do not match 
*    the determined locale.
*    </li>
* </ol>
* <p>
* The translated object will be attached to all translated parent folders if
* found or to all original parent folders for which no translated parent 
* folder was found. If the original object is not attached to any folder, the 
* translated object will not be attached to any folder.
* <p>
* This effect will be active for he following situations only:
* <ol>
*    <li>
*    The execution context must be {@link com.percussion.relationship.
*    IPSExecutionContext#RS_PRE_CONSTRUCTION}
*    </li>
*    <li>
*    The originating relationship and the current relationship both must be 
*    of {@link PSRelationshipConfig#CATEGORY_TRANSLATION translation}
*    </li>
*    <li>
*    The originating relationshipship's owner must be a Folder
*    </li>
* </ol>
* 
* @author RammohanVangapalli
*/
public class PSOAttachTranslatedFolder extends PSEffect
{
	

	/**
	    * The logger for this class.
	*/
	private static Logger log = Logger.getLogger(PSOAttachTranslatedFolder.class);
	
	public static final String DEEP_FOLDER_EXCLUSION_FLAG = 
	    "com.percussion.pso.effects.PSDeepFolderExclusion";

  /*
   * (non-Javadoc)
   * 
   * @see com.percussion.relationship.IPSEffect#test(java.lang.Object[],
   * com.percussion.server.IPSRequestContext,
   * com.percussion.relationship.IPSExecutionContext,
   * com.percussion.relationship.PSEffectResult)
   */
  @SuppressWarnings("unused")
  @Override  
  public void test(Object[] params, IPSRequestContext request,
     IPSExecutionContext context, PSEffectResult result)
     throws PSExtensionProcessingException, PSParameterMismatchException
  {

     if(!context.isPreConstruction())
     {
        result.setWarning(
           "This effect is active only during relationship construction");
        return;
     }
     
     PSRelationship originatingRel = context.getOriginatingRelationship();
     if (!originatingRel.getConfig().getCategory().equals(
        PSRelationshipConfig.CATEGORY_TRANSLATION))
     {
        result.setWarning(
           "This effect is active only if the originating relationship is " +
           "of category '" + PSRelationshipConfig.CATEGORY_TRANSLATION + "'");
        return;
     }
     
     PSRelationship currentRel = context.getCurrentRelationship();
     if (!currentRel.getConfig().getCategory().equals(
        PSRelationshipConfig.CATEGORY_TRANSLATION))
     {
        result.setWarning(
           "This effect is active only if the current relationship is " +
           "of category '" + PSRelationshipConfig.CATEGORY_TRANSLATION + "'");
        return;
     }
     
     try
     {
        PSLocator origOwner = originatingRel.getOwner();
        IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
        PSComponentSummary summary = cms.loadComponentSummary(
           origOwner.getId());

        if (summary.getType() == PSCmsObject.TYPE_FOLDER && 
           (originatingRel.getOwner().getId() != 
              currentRel.getOwner().getId()))
        {
           result.setWarning("This effect is active only if the originating " +
              "relationship's parent is a Folder and the owners of current " +
              "and originating relationships have he same contentid");
           return;
        }
        
        PSLocator depLocator = currentRel.getDependent();
        PSRelationshipProcessorProxy relProxy;
        relProxy = new PSRelationshipProcessorProxy(
           PSProcessorProxy.PROCTYPE_SERVERLOCAL, request, 
           PSDbComponent.getComponentType(PSFolder.class));
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setDependent(depLocator);
        filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
        PSComponentSummaries folderParents = relProxy.getSummaries(filter,
           true);
        if (folderParents.size() > 0)
        {
           result.setWarning("Folder is already attached");
           return;
        }
     }
     catch (PSCmsException e)
     {
        e.printStackTrace();
        result.setError(e);
     }
     
     result.setSuccess();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.percussion.relationship.IPSEffect#attempt(java.lang.Object[],
   * com.percussion.server.IPSRequestContext,
   * com.percussion.relationship.IPSExecutionContext,
   * com.percussion.relationship.PSEffectResult)
   */
  @SuppressWarnings({"unused","unchecked"})
  @Override
  public void attempt(Object[] params, IPSRequestContext request,
     IPSExecutionContext context, PSEffectResult result)
     throws PSExtensionProcessingException, PSParameterMismatchException
  {
	  
	  
     PSRelationship currentRel = context.getCurrentRelationship();
     PSLocator owner = currentRel.getOwner();
     try
     {
        PSRelationshipProcessorProxy relProxy = 
           new PSRelationshipProcessorProxy(
              PSProcessorProxy.PROCTYPE_SERVERLOCAL, request);

        /*
         * Locate the containing folders for the owner of the current 
         * relationship.
         */
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setDependent(owner);
        filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
        PSComponentSummaries folderParents = relProxy.getSummaries(filter,
           true);
        
        /*
         * Do nothing if none exists. This is legal, for example one is 
         * creating a translation of an item that is not attached to any 
         * folder and hence is visible in a view.
         */
        if (folderParents.size() < 1)
        {
           /*
            * The original item is not related to any folder, do nothing, set 
            * status to success and return. 
            */
           result.setSuccess();
           return;
        }
        
        /*
         * Load the component summary for the current relationship's 
         * dependent to get the required locale.
         */
        PSLocator depLocator = currentRel.getDependent();
        IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
        PSComponentSummary depSummary = cms.loadComponentSummary(
           depLocator.getId());
        String locale = depSummary.getLocale();

        /*
         * The current relationship's owner may belong to one or more folders. 
         * Find the folder's translation counterpart with the current 
         * dependent's locale.
         * First follow the translation relationship on the folder parent 
         * down, then follow the relationship up to find all translated 
         * parent folders.
         */
        List<PSLocator> originalParents = new ArrayList<PSLocator>();
        List<PSLocator> newParents = new ArrayList<PSLocator>();
        Iterator walker = folderParents.iterator();
        while (walker.hasNext())
        {
           PSComponentSummary summary = (PSComponentSummary) walker.next();
           PSLocator originaFolderLocator = (PSLocator) summary.getLocator();
           
           boolean foundTranslatedParent = false;
           
           foundTranslatedParent = filterNewParents(relProxy, currentRel, locale, originaFolderLocator, newParents);
           
           
           if (!foundTranslatedParent) {
        	   log.debug("Did not found translated parent for folder");
        	   translateParentFolders(relProxy, currentRel,originaFolderLocator,locale,request);
        	   foundTranslatedParent = filterNewParents(relProxy, currentRel, locale, originaFolderLocator, newParents);
           }
           
           if (!foundTranslatedParent) {
        	  log.debug("No Parent folders were translated will add transation to original folder");
        	   originalParents.add((PSLocator) summary.getLocator());
        	  
           }
        }

        /*
         * Add all original parents for which we did not find a translated
         * counterpart.
         */
        newParents.addAll(originalParents);

        // create the new relationships
        List<PSLocator> list = new ArrayList<PSLocator>();
        list.add(depLocator);
        relProxy = new PSRelationshipProcessorProxy(
           PSProcessorProxy.PROCTYPE_SERVERLOCAL, request, 
           PSFolder.getComponentType(PSFolder.class));
        for (int i=0; i<newParents.size(); i++)
        {
           PSLocator newParent = newParents.get(i);
           relProxy.add(PSRelationshipConfig.TYPE_FOLDER_CONTENT, list, 
              newParent);
        }
        
        result.setSuccess();
     }
     catch (PSException e)
     {
        e.printStackTrace();
        result.setError(e);
     }
  }
  
  @SuppressWarnings({"unused","unchecked"})
  private boolean filterNewParents(PSRelationshipProcessorProxy relProxy, PSRelationship currentRel, String locale, PSLocator folder, List<PSLocator>  newParents) throws PSCmsException {
  // follow relationship down
  boolean foundTranslatedParent= false;
  PSRelationshipFilter filter = new PSRelationshipFilter();
  log.debug("Checking folder id="+folder.getId()+" revision "+folder.getRevision());
  log.debug("relationship is "+currentRel.getConfig().getName());
  filter.setOwner(folder);
  filter.setName(currentRel.getConfig().getName());
  if (updateNewParents(
     relProxy.getSummaries(filter, false).iterator(), locale, 
     newParents))
     foundTranslatedParent = true;

  // follow relationship up
  filter = new PSRelationshipFilter();
  filter.setDependent(folder);
  filter.setName(currentRel.getConfig().getName());
  if (updateNewParents(
     relProxy.getSummaries(filter, true).iterator(), locale, 
     newParents))
     foundTranslatedParent = true;
  	return foundTranslatedParent;
  }
  private void translateParentFolders(PSRelationshipProcessorProxy relProxy, PSRelationship currentRel, PSLocator originaFolderLocator, String locale,IPSRequestContext request) throws PSCmsException {
	  IPSContentWs cws = PSContentWsLocator.getContentWebservice();
	  IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
	  
	  
	  try {
		List<PSFolder> folders = cws.loadFolders(Collections.singletonList(gmgr.makeGuid(originaFolderLocator)));
		if (folders!= null && folders.size()> 0 ) {
			String folderPath=folders.get(0).getFolderPath();
			log.debug("Checking translations for full folder path "+folderPath+ " into "+locale);
			List<IPSGuid> pathIds = cws.findPathIds(folderPath);
			boolean foundTranslation = false;
			for(IPSGuid folder : pathIds) {
				log.debug("Checking translation "+folder.getUUID());
				List<PSLocator> locators = new ArrayList<PSLocator>();;
				boolean thisFolderIsTranslated = filterNewParents(relProxy, currentRel, locale, gmgr.makeLocator(folder), locators);
		           
					
					//checkIfFolderTranslated(folder,locale,request);
				log.debug("Is Translated = "+thisFolderIsTranslated);
				if (!foundTranslation && thisFolderIsTranslated) foundTranslation = true;
				
				if (foundTranslation && !thisFolderIsTranslated) {
					log.debug("An ancestor folder is translated but this is not.  Will translate");
					translateFolderOnly(folder,locale,request);
				}
			}
		}
	  } catch (PSErrorResultsException e) {
		  log.error("Failed to translate parent folders",e);
	} catch (PSErrorException e) {
		  log.error("Failed to translate parent folders",e);
	}
	  
	
}

private void translateFolderOnly(IPSGuid folder, String locale, IPSRequestContext request) throws PSErrorResultsException, PSErrorException {
	log.debug("Setting deep clone prevention");
	setPreventFolderDeepClone(request);
	IPSContentWs cws = PSContentWsLocator.getContentWebservice();
	PSAutoTranslation transConfig = new PSAutoTranslation();
	transConfig.setLocale(locale);
	
	List<PSCoreItem> trans = cws.newTranslations(Collections.singletonList(folder), Collections.singletonList(transConfig), PSRelationshipConfig.TYPE_TRANSLATION, false, request.getUserName(), request.getUserSessionId());
	log.debug("Got "+trans.size() + " translation items");
}

private boolean checkIfFolderTranslated(IPSGuid folder, String locale, IPSRequestContext request) throws PSErrorException {
	// 
	// User relationship processor 
	log.debug("Checking translations for item "+folder.getUUID());
	IPSSystemWs systemWs = PSSystemWsLocator.getSystemWebservice();
	PSRelationshipFilter filter = new PSRelationshipFilter();
	filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_TRANSLATION);
	//filter.setDependentContentTypeId(arg0;)
	filter.setProperty("sys_lang", locale);
	List<IPSGuid> translations = systemWs.findDependents(folder, filter);
	if (log.isDebugEnabled()) {
		if(translations == null ) {
			log.debug("Translations returned null");
			
		} else {
			if (translations.size()==0) {
				log.debug("Did not find any translations");
			}
			for (IPSGuid guid : translations) {
				log.debug("Found translation item id "+guid.getUUID());
			}
		}
	}
	return translations == null || translations.size() > 1; 	
}

/**
 * set the exclusion flag.
 * 
 * @param req the request context of the caller.
 * @param b the new exclusion value. <code>true</code> means that
 *           subsequent effects should not interfere with event processing.
 */
protected static void setPreventFolderDeepClone(IPSRequestContext req)
{
   req.setParameter("trans_folder_only", "true");
   req.setPrivateObject(DEEP_FOLDER_EXCLUSION_FLAG, "true");
}


/*
   * (non-Javadoc)
   * 
   * @see com.percussion.relationship.IPSEffect#recover(java.lang.Object[],
   * com.percussion.server.IPSRequestContext,
   * com.percussion.relationship.IPSExecutionContext,
   * com.percussion.extension.PSExtensionProcessingException,
   * com.percussion.relationship.PSEffectResult)
   */
  @SuppressWarnings("unused")
  @Override
  public void recover(Object[] params, IPSRequestContext request,
     IPSExecutionContext context, PSExtensionProcessingException e,
     PSEffectResult result) throws PSExtensionProcessingException
  {
     // noop
  }
  
  /**
   * Walks all supplied summaries and weeds out the ones which do not match
   * the supplied locale. The new parents locator list will be updated with 
   * all summaries that match the supplied locale.
   * 
   * @param summaries the summaries to walk, assumed not <code>null</code>, 
   *    may be empty.
   * @param locale the locale to filter by, assumed not <code>null</code>, 
   *    may be empty.
   * @param newParents the list of new parent locators which will be updated
   *    for all summaries that match the supplied locale, assumed not
   *    <code>null</code>, may be empty.
   * @return <code>true</code> if the supplied list of new parents was 
   *    updated, <code>false</code> otherwise.
   */
  private boolean updateNewParents(Iterator<PSComponentSummary> summaries, 
     String locale, List<PSLocator> newParents)
  {
     boolean foundTranslatedParent = false;
     log.debug("Testing translation relationships");
     while (summaries.hasNext())
     {
    	 
        PSComponentSummary summary = summaries.next();
        if (summary.getLocale().equals(locale))
        {
        	log.debug("Found summary with id "+locale);
           newParents.add((PSLocator) summary.getLocator());
           foundTranslatedParent = true;
        } else {
        	log.debug("Locale of "+summary.getContentId() +" name "+summary.getName()+" is "+summary.getLocale());
        }
     }
     
     return foundTranslatedParent;
  }
  
  
  

  /**
   * Get the current user's request.
   * 
   * @return The request, never <code>null</code>.
   * 
   * @throws IllegalStateException if the current thread has not had a request
   * initialized.
   */
  private PSRequest getRequest()
  {
     PSRequest request = (PSRequest) PSRequestInfo
        .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
     
     if (request == null)
        throw new IllegalStateException(
           "No request initialized for the current thread");
     
     return request;
  }
  
  
  /**
   * Get a new request based on the current user's session.
   * 
   * @return The request, never <code>null</code>.
   * 
   * @throws IllegalStateException if the current thread has not had a request
   * initialized.
   */
  private PSRequest getNewRequest()
  {
     return new PSRequest(getRequest().getSecurityToken());
  }

  public PSCoreItem newTranslations(IPSGuid id,
	      String locale, String relationshipType,
	      boolean enableRevisions)
	      throws PSErrorResultsException, PSErrorException
	   {
	      final int errorCode = IPSWebserviceErrors.NEWTRANSLATION_FAILED;
	      PSWebserviceUtils.validateLegacyGuids(Collections.singletonList(id));
	  		IPSContentWs cws = 
	      final  PSComponentSummary summary = cws.loadSummaries(Collections.singletonList(id));
	      

	      validateTranslationRequest(locale, summaries, id);

	      if (!StringUtils.isBlank(relationshipType))
	         validateRelationshipType(relationshipType,
	            PSRelationshipConfig.CATEGORY_TRANSLATION);

	 
	            if (summary.getLocale().equals(localeCode))
	            {
	               assert !explicitLocales;
	               // the request was to convert to all the locales,
	               // except the item's one 
	               continue;
	            }

	           
	            try
	            {
	               PSItemDefinition def = getItemDefinition(getContentTypeId(id));

	               PSLegacyGuid copyId = (PSLegacyGuid) createNewTranslation(id,
	                     relationshipType, locale,
	                     def.getInternalRequestResource());
	              
	            }
	            catch (PSException e)
	            {
	               addToErrorResults(e, id, errorCode, errorResults);
	            }
	            catch (PSORMException e)
	            {
	               addToErrorResults(e, id, errorCode, errorResults);
	            }
	         }

	         if (errorResults.hasErrors())
	            throw errorResults;

	         results.addAll(errorResults.getResults(resultIds));
	      }

	      return results;
	   }
  /**
   * Validate that the supplied relationship type exists for the specified 
   * category. Throws an <code>IllegalArgumentException</code> if the specified 
   * relationship does not exist.
   * 
   * @param type the relationship type to validate, assumed not 
   *    <code>null</code> or empty.
   * @param category the catagory for which to validate, assumed not 
   *    <code>null</code> or empty.
   * @throws PSErrorException for any unexpected error.
   */
  private void validateRelationshipType(String type, String category)
     throws PSErrorException
  {
     IPSSystemDesignWs service = PSSystemWsLocator.getSystemDesignWebservice();
     List<IPSCatalogSummary> relationships = service.findRelationshipTypes(
        type, category);

     if (relationships.size() == 0)
        throw new IllegalArgumentException("unknown relationship type \""
           + type + "\" for category \"" + category + "\".");
  }
  /**
   * Create a new translation for the identified item.
   * 
   * @param id the id of the item to create a new translation for, not 
   *    <code>null</code>.
   * @param type the relationship type, <code>null</code> or empty to use 
   *    the default system translation relationship.
   * @param locale the locale code used for the new translation, 
   *    not <code>null</code>.
   * @param resource the resource name to use to make the internal request, 
   *    not <code>null</code> or empty.
   * @return the id of the new trnalation item, never <code>null</code>.
   * @throws PSException for any error creating the new translation.
   */
  private IPSGuid createNewTranslation(IPSGuid id, String type,
     final String locale, String resource)
     throws PSException
  {
     if (locale == null)
        throw new IllegalArgumentException("Locale cannot be null");

     if (StringUtils.isBlank(type))
        type = PSRelationshipConfig.TYPE_TRANSLATION;

     Map<String, String> params = new HashMap<String, String>();
     params.put(IPSHtmlParameters.SYS_LANG, locale);

     return createRelatedItem(id, type, resource, params);
  }
  /**
   * Adds the provided error to the error results.
   * @param e the exception to add. Assumed not <code>null</code>.
   * @param id the failed item id. Assumed not <code>null</code>.
   * @param code the error code to add.
   * @param errorResults the results to add to.
   * Assumed not <code>null</code>.
   */
  private void addToErrorResults(final Exception e,
        final PSLegacyGuid id, final int code,
        final PSErrorResultsException errorResults)
  {
     final PSErrorException error = new PSErrorException(code,
           PSWebserviceErrors.createErrorMessage(code, id.getUUID(),
              e.getLocalizedMessage()),
              ExceptionUtils.getFullStackTrace(e));
     errorResults.addError(id, error);
  }
  
  private IPSGuid createRelatedItem(IPSGuid id, String type, String resource,
	      Map<String, String> parameters) throws PSException
	   {
	      if (id == null)
	         throw new IllegalArgumentException("id cannot be null");

	      if (StringUtils.isBlank(type))
	         throw new IllegalArgumentException("type cannot be null or empty");

	      if (StringUtils.isBlank(resource))
	         throw new IllegalArgumentException("resource cannot be null or empty");

	      // create new request each time
	      PSRequest request = getNewRequest();

	      IPSInternalResultHandler rh = (IPSInternalResultHandler) PSServer
	         .getInternalRequestHandler(resource);
	      if (rh == null)
	       //  throw new PSException(IPSServerErrors.CE_NEEDED_APP_NOT_RUNNING,
	       //     resource);
	    	  throw new PSException(1, resource);
	      Map<String, String> params = null;
	      if (parameters != null)
	         params = parameters;
	      else
	         params = new HashMap<String, String>();
	      params.put(IPSHtmlParameters.SYS_COMMAND,
	         PSRelationshipCommandHandler.COMMAND_NAME);
	      params.put(IPSHtmlParameters.SYS_CONTENTID, String
	         .valueOf(((PSLegacyGuid) id).getContentId()));
	      params.put(IPSHtmlParameters.SYS_REVISION, String
	         .valueOf(((PSLegacyGuid) id).getRevision()));
	      params.put(IPSHtmlParameters.SYS_RELATIONSHIPTYPE, type);
	      request.setParameters((HashMap) params);
	      PSExecutionData data = null;
	      try
	      {
	         data = rh.makeInternalRequest(request);
	      }
	      finally
	      {
	         if(data != null)
	            data.release();
	      }

	      return new PSLegacyGuid(Integer.valueOf(request
	         .getParameter(IPSHtmlParameters.SYS_CONTENTID)), Integer
	         .valueOf(request.getParameter(IPSHtmlParameters.SYS_REVISION)));
	   }

}
