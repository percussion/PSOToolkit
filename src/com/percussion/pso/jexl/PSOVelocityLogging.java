package com.percussion.pso.jexl;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSJexlUtilBase;

/***
 * This class adds JEXL methods to allow
 * a Velocity template to write to the server log.
 * 
 * This is intended to facilitate the debugging of templates.
 * 
 * @author natechadwick
 *
 */
public class PSOVelocityLogging extends PSJexlUtilBase {

	public static final String DEBUG="DEBUG";
	public static final String INFO="INFO";
	public static final String ERROR="ERROR";
	
	private static final Log log = LogFactory.getLog(PSOVelocityLogging.class);

	
	 @IPSJexlMethod(description = "Writes an entry to the server log", params =
		    {
		          @IPSJexlParam(name = "template", description = "the template generating the log"),
		          @IPSJexlParam(name = "message", description = "the message to write to the server log"),
		          @IPSJexlParam(name = "level", description = "the logging level to use.  ERROR, DEBUG, INFO"),		          
		    }, returns = "nothing")
	public void log(String template, String message, String level){
		
		 if(level.equals(DEBUG)){
			 if(log.isDebugEnabled()){
				 log.debug(String.format("[{0}]-{1}", template,message));
			 }
		 }else if(level.equals("INFO")){
			 if(log.isInfoEnabled()){
				 log.info(String.format("[{0}]-{1}", template,message));
			 }
		 }else{//TODO: Finish ME}
	}

	 }
	
	
}
