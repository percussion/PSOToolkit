/**************************************
 * com.precussion.pso.validation PSOUniqueFieldWithInFoldersValidator
 *  
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * @author agent
 *
 */
package com.percussion.pso.validation;

import static java.text.MessageFormat.*;
import static java.util.Arrays.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * This is a field validator that checks whether or not a 
 * field is unique for all the folders that the item resides
 * (or to be created) in.
 * See the <code>Extensions.xml</code> for more information.
 * @author adamgent
 *
 */
public class PSOUniqueFieldWithInFoldersValidator implements IPSFieldValidator {

    private IPSExtensionDef extensionDef;
    private IPSContentWs contentWs;
    private IPSGuidManager guidManager;
    private IPSContentMgr contentManager;
    
    public Boolean processUdf(Object[] params, IPSRequestContext request)
            throws PSConversionException {
        String actionType = request.getParameter("DBActionType");
        if(actionType == null || 
           !(actionType.equals("INSERT") || actionType.equals("UPDATE")))
           return true;
        
        PSOExtensionParamsHelper h = new PSOExtensionParamsHelper(getExtensionDef(), params, request, log);
        String fieldName = h.getRequiredParameter("fieldName");
        
        String fieldValue = request.getParameter(fieldName);
        if (fieldValue == null) {
            log.debug("Field value was null for field: " + fieldName);
            return true;
        }
        try {
            boolean rvalue = true;
            if (actionType.equals("UPDATE")) {
                Number contentId = h.getRequiredParameterAsNumber("sys_contentid");
                rvalue = isFieldValueUniqueInFolderForExistingItem(contentId.intValue(), fieldName, fieldValue);
            }
            else {
                Number folderId = getFolderId(request);
                if (folderId != null)
                    rvalue = isFieldValueUniqueInFolder(folderId.intValue(), fieldName, fieldValue);
                else
                    rvalue = false;
            }
            return rvalue;
        } catch (Exception e) {
            log.error(format("An error happend while checking if " +
            		"fieldName: {0} was unique for " +
            		"contentId: {1} with " +
            		"fieldValue: {2}",
            		fieldName, request.getParameter("sys_contentid"), fieldValue), e);
            return false;
        }
    }
    
    /**
     * See if a field value is unique in all the folders that the given existing item resides. 
     * @param contentId id of the item.
     * @param fieldName field name to check for uniqueness.
     * @param fieldValue the value of the field.
     * @return true if its unique
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public boolean isFieldValueUniqueInFolderForExistingItem(int contentId, String fieldName, String fieldValue) 
        throws PSErrorException, InvalidQueryException, RepositoryException {
        
        boolean unique = true;
        IPSGuid guid = guidManager.makeGuid(new PSLocator(contentId, -1));
        String[] paths = contentWs.findFolderPaths(guid);

        if (paths != null && paths.length != 0) {
            String jcrQuery = getQueryForValueInFolders(contentId, fieldName, fieldValue, paths);
            log.trace(jcrQuery);
            Query q = contentManager.createQuery(jcrQuery, Query.SQL);
            QueryResult results = contentManager.executeQuery(q, -1, null, null);
            RowIterator rows = results.getRows();
            long size = rows.getSize();
            unique = size > 0 ? false : true;
        }
        else {
            log.debug("The item: " + contentId + " is not in any folders");
        }
        
        return unique;
    
    }
    
    /**
     *  See if a field value is unique in the given folder for a new item.
     * @param folderId id of the folder
     * @param fieldName name of the field
     * @param fieldValue the desired value of the field for the new item.
     * @return true if its unique.
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     * @throws PSErrorResultsException
     */
    public boolean isFieldValueUniqueInFolder(int folderId, String fieldName, String fieldValue)
        throws PSErrorException, InvalidQueryException, RepositoryException, PSErrorResultsException {
        boolean unique = true;
        IPSGuid guid = guidManager.makeGuid(new PSLocator(folderId, -1));
        List<PSFolder> folders = contentWs.loadFolders(asList(guid));
        String path = ! folders.isEmpty() ? folders.get(0).getFolderPath() : null;
        if (path != null) {
            String jcrQuery = getQueryForValueInFolder(fieldName, fieldValue, path);
            log.trace(jcrQuery);
            Query q = contentManager.createQuery(jcrQuery, Query.SQL);
            QueryResult results = contentManager.executeQuery(q, -1, null, null);
            RowIterator rows = results.getRows();
            long size = rows.getSize();
            unique = size > 0 ? false : true;
        }
        else {
            log.error("The folder id: " + folderId + " did not have a path (BAD)");
        }
    
        return unique;
    }
    
    public String getQueryForValueInFolders(
            int contentId, 
            String fieldName, 
            String fieldValue, 
            String[] paths) {
        ArrayList<String> jcrPaths = new ArrayList<String>();
        for (String p : paths) { jcrPaths.add("jcr:path = '" + p + "'"); };
        String jcrQuery = format(
                "select rx:sys_contentid, rx:{0} " +
                "from nt:base " +
                "where " +
                "rx:sys_contentid != {1} " +
                "and " +
                "rx:{0} = ''{2}'' " +
                "and " +
                "({3})", 
                fieldName, ""+contentId, 
                fieldValue, StringUtils.join(jcrPaths.iterator(), " or "));
        return jcrQuery;
    }
    
    public String getQueryForValueInFolder(String fieldName, String fieldValue, String path) {
        return format(
                "select rx:sys_contentid, rx:{0} " +
                "from nt:base " +
                "where " +
                "rx:{0} = ''{1}'' " +
                "and " +
                "jcr:path = ''{2}''", 
                fieldName, fieldValue, path);
    }
    
    protected Integer getFolderId(IPSRequestContext request) {
        // get the target parent folder id from the redirect url
        String folderId = null;
        Integer rvalue = null;
        String psredirect = request.getParameter(
           IPSHtmlParameters.DYNAMIC_REDIRECT_URL);
        if (psredirect != null && psredirect.trim().length() > 0)
        {
           int index = psredirect.indexOf(IPSHtmlParameters.SYS_FOLDERID);
           if(index >= 0)
           {
              folderId = psredirect.substring(index +
                 IPSHtmlParameters.SYS_FOLDERID.length() + 1);
              index = folderId.indexOf('&');
              if(index > -1)
                 folderId = folderId.substring(0, index);
           }
        }
        if (StringUtils.isNumeric(folderId) && StringUtils.isNotBlank(folderId)) {
            rvalue = Integer.parseInt(folderId);
        
        }
        
        return rvalue;
    }

    public void init(IPSExtensionDef extensionDef, File arg1)
            throws PSExtensionException {
        setExtensionDef(extensionDef);
        if (contentManager == null) setContentManager(PSContentMgrLocator.getContentMgr());
        if (contentWs == null) setContentWs(PSContentWsLocator.getContentWebservice());
        if (guidManager == null) setGuidManager(PSGuidManagerLocator.getGuidMgr());
    }

    public IPSExtensionDef getExtensionDef() {
        return extensionDef;
    }

    public void setExtensionDef(IPSExtensionDef extensionDef) {
        this.extensionDef = extensionDef;
    }

    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(PSOUniqueFieldWithInFoldersValidator.class);

    public void setContentWs(IPSContentWs contentWs) {
        this.contentWs = contentWs;
    }

    public void setGuidManager(IPSGuidManager guidManager) {
        this.guidManager = guidManager;
    }

    public void setContentManager(IPSContentMgr contentManager) {
        this.contentManager = contentManager;
    }
}
