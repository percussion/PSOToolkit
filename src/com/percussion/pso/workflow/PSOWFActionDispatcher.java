// Decompiled by DJ v3.2.2.67 Copyright 2002 Atanas Neshkov  Date: 4/2/2003 12:57:02 PM
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3)
// Source File Name:   WFActionDispatcher.java

package com.percussion.pso.techtarget.workflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXPathEvaluator;

public class PSOWFActionDispatcher extends PSDefaultExtension
    implements IPSWorkflowAction 
{
    private static final Log log = LogFactory.getLog(PSOWFActionDispatcher.class);
    public PSOWFActionDispatcher()
    {
        m_extensionDef = null;
        m_codeRoot = null;
    }

    public void init(IPSExtensionDef extensionDef, File codeRoot)
        throws PSExtensionException
    {
        log.debug("Initializing WFActionDispatcher...");
        m_extensionDef = extensionDef;
        m_codeRoot = codeRoot;
    }

    public void performAction(IPSWorkFlowContext wfContext, IPSRequestContext request)
        throws PSExtensionProcessingException
    {
        String sName = "performAction";
        log.debug("WFActionDispatcher::performAction executing...");
        boolean bOK = true;
        int contentId = 0;
        try
        {
            contentId = Integer.parseInt(request.getParameter("sys_contentid"));
        }
        catch(NumberFormatException nfex)
        {
            throw new PSExtensionProcessingException("WFActionDispatcher::performAction", nfex);
        }

        int transitionId = wfContext.getTransitionID();
        int workflowId = wfContext.getWorkflowID();
        log.debug("Content id: " + contentId);
        log.debug("Workflow id: " + workflowId);
        log.debug("Transition Id: " + transitionId);
        //int contentType = 0;
        try
        {
            //contentType = getContentType(contentId, request);
        }
        catch(Exception e)
        {            
            log.error("WFActionDispatcher::performAction", e);
        }
        try
        {
            //Object actions[] = getWorkflowActions(contentType, transitionId);
        	Object actions[] = getWorkflowActions(workflowId, transitionId);
            for(int i = 0; i < actions.length; i++)
            {
                log.debug("Executing " + actions[i] + "... ");
                String className = getWorkflowActionClassName(actions[i].toString());
                Class cl = Class.forName(className);
                Object action = cl.newInstance();
                Method mt[] = cl.getMethods();
                for(int j = 0; j < mt.length; j++)
                    if(mt[j].getName().equals("init"))
                    {
                        Object initArgs[] = {
                            null, null
                        };
                        mt[j].invoke(action, initArgs);
                    }

                for(int k = 0; k < mt.length; k++)
                    if(mt[k].getName().equals("performAction"))
                    {
                        Object performActionArgs[] = {
                            wfContext, request
                        };
                        mt[k].invoke(action, performActionArgs);
                    }

            }

        }
        catch(ClassNotFoundException nfx)
        {
            log.error("WFActionDispatcher::performAction",nfx);
            throw new PSExtensionProcessingException("WFActionDispatcher::performAction", nfx);
        }
        catch(InstantiationException inx)
        {
            log.error("WFActionDispatcher::performAction",inx);
            throw new PSExtensionProcessingException("WFActionDispatcher::performAction", inx);
        }
        catch(IllegalAccessException iax)
        {
            log.error("WFActionDispatcher::performAction",iax);
            throw new PSExtensionProcessingException("WFActionDispatcher::performAction", iax);
        }
        catch(InvocationTargetException itx)
        {
            log.error("WFActionDispatcher::performAction",itx);
            throw new PSExtensionProcessingException("WFActionDispatcher::performAction", itx);
        }
        log.debug("WFActionDispatcher::performAction done");
    }

    private Object[] getWorkflowActions(int workflowId, int transitionId)
        throws PSExtensionProcessingException
    {
        String PROP_DELIMITER = ",";
        String VALUE_DELIMITER = "|";
        ArrayList actions = new ArrayList();
        Properties props = new Properties();
        try
        {
            props.load(new FileInputStream("rxconfig/Workflow/dispatcher.properties"));
            String sActions = props.getProperty(workflowId + "|" + transitionId);
            if(actions != null)
            {
                for(StringTokenizer st = new StringTokenizer(sActions, ","); st.hasMoreTokens(); actions.add(st.nextToken()));
            } else
            {
                log.error("Could not find property " + workflowId + ":" + transitionId + " in " + "rxconfig/Workflow/dispatcher.properties");
            }
        }
        catch(FileNotFoundException fex)
        {
            log.error("Properties file not found: rxconfig/Workflow/dispatcher.properties", fex);
            //fex.printStackTrace();
        }
        catch(IOException ex)
        {
            log.error("Properties file could not be opened: rxconfig/Workflow/dispatcher.properties",ex);
            //ex.printStackTrace();
        }
        finally
        {
            if(props != null)
                props.clear();
        }
        return actions.toArray();
    }

    private String getCurrentExtensionVersion()
        throws PSExtensionProcessingException
    {
        String sName = "getCurrentExtensionVersion";
        Document document = null;
        String currVersion = null;
        document = parseFile(new File("Extensions/Extensions.xml"));
        currVersion = evalXPath(document, "/PSXExtensionHandlerConfiguration/Extension[@name='Java']/initParam[@name='com.percussion.extension.version']");
        return currVersion;
    }

    private String getWorkflowActionClassName(String workflowActionName)
        throws PSExtensionProcessingException
    {
        String sName = "getWorkflowActionClassName";
        Document document = null;
        String className = null;
        String currVersion = getCurrentExtensionVersion();
        document = parseFile(new File("Extensions/Handlers/Java/" + currVersion + "/" + "Extensions.xml"));
        className = evalXPath(document, "/PSXExtensionHandlerConfiguration/Extension[@name='" + workflowActionName + "']/initParam" + "[@name='className']");
        return className;
    }

    private Document parseFile(File fileName)
        throws PSExtensionProcessingException
    {
        String sName = "parseFile";
        Document document = null;
        DocumentBuilderFactory dbf = null;
        DocumentBuilder parser = null;
        try
        {
            dbf = DocumentBuilderFactory.newInstance();
            parser = dbf.newDocumentBuilder();
        }
        catch(ParserConfigurationException pcx)
        {
            log.error("WFActionDispatcher::parseFile\n",pcx);
            throw new PSExtensionProcessingException("WFActionDispatcher::parseFile", pcx);
        }
        Document doc = null;
        try
        {
            doc = parser.parse(fileName);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(SAXException e)
        {
            e.printStackTrace();
        }
        return doc;
    }

    //private int getContentType(int contentId, IPSRequestContext req)
        //throws PSExtensionProcessingException
    //{
        //String sName = "getContentType";
        //int contentTypeId = 0;
        //Map params = new HashMap();
        //params.put("sys_contentid", (new Integer(contentId)).toString());
        //IPSInternalRequest ir = req.getInternalRequest("sys_ceSupport/contentstatus", params, false);
        //if(ir == null)
            //throw new PSExtensionProcessingException("WFActionDispatcher::getContentType", new Exception("*** Error: getInternalRequest returned null"));
        //try
        //{
            //Document doc = ir.getResultDoc();
            //System.out.println(PSXmlDocumentBuilder.toString(doc));
            //String contentType = evalXPath(doc, "/ContentStatusRoot/ContentStatus/ContentTypeName/@typeId");
            //System.out.println("Content type: " + contentType);
            //contentTypeId = Integer.parseInt(contentType);
        //}
        //catch(PSInternalRequestCallException irex)
        //{
            //throw new PSExtensionProcessingException("WFActionDispatcher::getContentType", irex);
        //}
        //catch(NumberFormatException nfex)
        //{
            //throw new PSExtensionProcessingException("WFActionDispatcher::getContentType", nfex);
        //}
        //finally
        //{
            //if(ir != null)
                //ir.cleanUp();
        //}
        //return contentTypeId;
    //}

    private String evalXPath(Document doc, String expr)
        throws PSExtensionProcessingException
    {
        String sName = "evalXPath";
        String value = null;
        try
        {
            PSXPathEvaluator xe = new PSXPathEvaluator(doc);
            value = xe.evaluate(expr);
        }
        catch(Exception xle)
        {
            throw new PSExtensionProcessingException(0, xle);
        }
        return value;
    }

    private static final String CLASSNAME = "WFActionDispatcher";
    private static final String RX_DISPATCHER_FILE = "rxconfig/Workflow/dispatcher.properties";
    private static final String RX_PROP_FILE = "rxconfig/Workflow/rxworkflow.properties";
    private static final String RX_EXTVERSION_FILE = "Extensions/Extensions.xml";
    private static final String RX_EXTENSION_PATH = "Extensions/Handlers/Java";
    private static final String RX_EXTENSION_FILE = "Extensions.xml";
    IPSExtensionDef m_extensionDef;
    File m_codeRoot;
}
