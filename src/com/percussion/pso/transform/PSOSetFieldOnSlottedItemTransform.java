package com.percussion.pso.transform;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/***
 * Extension for setting the field on an item when it is saved and has an item in the specified slot. 
 * 
 * @author natechadwick
 *
 */
public class PSOSetFieldOnSlottedItemTransform  implements IPSItemInputTransformer, IPSRequestPreProcessor {

	private static Log log = LogFactory.getLog(PSOSetFieldOnSlottedItemTransform.class); 
	private IPSGuidManager mGmgr;
	private IPSContentWs mCws;
	
	
	/***
	 * Inner class for handling the user configured parameters for the extension
	 * on the Relationship Effect parameters dialog.
	 * @author natechadwick
	 *
	 */
	private class ConfiguredParams{

		protected String fieldName;
		protected String valueIfEmpty;
		protected String valueIfNotEmpty;		
		protected String slotName;
		
		/***
		 * Constructor to initialize a new parameter object
		 * @param params
		 */
		protected ConfiguredParams(Object[] params){
			
			if(params!=null){
				
				if(params[1]!=null){
					fieldName=params[0].toString();
					log.debug("fieldName=" + params[0]);
				}else{
					fieldName=null;
				}
				
				if(params[1]!=null){
					valueIfEmpty=params[1].toString();
					log.debug("value=" + params[1]);
				}else{
					valueIfEmpty=null;
				}
				
				if(params[2]!=null){
					valueIfNotEmpty=params[2].toString();
					log.debug("value=" + params[2]);
				}else{
					valueIfNotEmpty=null;
				}
				
				if(params[3]!=null){				
					slotName = params[3].toString().trim();
					log.debug("slotName=" + params[3]);
				}else{
					slotName=null;
				}
			}
			
			if(params == null || fieldName== null || slotName==null)
			{
				throw new IllegalArgumentException("Field Name, and SlotName parameters must be set.");
			}
		}
	}

	
	
	@Override
	public void preProcessRequest(Object[] params, IPSRequestContext request)
			throws PSAuthorizationException, PSRequestValidationException,
			PSParameterMismatchException, PSExtensionProcessingException {
		
		IPSGuidManager gmgr = getGuidManager();
		IPSContentWs cws = getContentWebService();
		ConfiguredParams configParams = new ConfiguredParams(params);
		
		PSRelationshipFilter filter = new PSRelationshipFilter();
		String contentid = request.getParameter("sys_contentid");
		if(contentid==null){
			return;
		}
		
		IPSGuid itemGuid = gmgr.makeGuid(contentid,PSTypeEnum.LEGACY_CONTENT);   
		
		
		PSLocator ownerLoc = gmgr.makeLocator(itemGuid);
		filter.setOwner(ownerLoc);
		filter.setName(PSRelationshipFilter.FILTER_NAME_ACTIVE_ASSEMBLY);
		
		boolean found = false;
		try{
			//Get all AA relationships and if we find one including the specified slot
			//set the specified field to the specified value;
			for (PSAaRelationship rel : cws.loadContentRelations(filter,true)) {
				if(rel.getSlotName().equals(configParams.slotName)){
					request.setParameter(configParams.fieldName, (configParams.valueIfNotEmpty + ""));
					found=true;
					log.debug("Setting " + configParams.fieldName + " to " + (configParams.valueIfNotEmpty + ""));
					break;
				}
		  }
			if(!found){
				request.setParameter(configParams.fieldName, (configParams.valueIfEmpty + ""));
				log.debug("Setting " + configParams.fieldName + " to " + (configParams.valueIfEmpty + ""));
			}
		} catch (PSErrorException e) {
			log.debug("Error processing slot relationships for item " + "" );
		}finally{} 
	         
	}

	
	private IPSGuidManager getGuidManager(){
		if(mGmgr == null){
			mGmgr = PSGuidManagerLocator.getGuidMgr();	
		}
		return mGmgr;
	}

	
	private IPSContentWs getContentWebService(){
		if(mCws == null){
			mCws = PSContentWsLocator.getContentWebservice();	
		}
		return mCws;
	}


	@Override
	public void init(IPSExtensionDef arg0, File arg1)
			throws PSExtensionException {
		log.info("Extension Initialized.");		
	}
	
}
