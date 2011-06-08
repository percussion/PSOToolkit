package com.percussion.pso.transform;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldInputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.server.IPSRequestContext;

/***
 * A field input transform for setting the value of a field
 * to the java @see String#hashCode() hashCode of the specified
 * source values.
 * 
 * An example use case for this may be where you want to index something
 * like a remote URL by it's hashCode.
 * 
 * @author natechadwick
 *
 */
public class HashCodeFieldTransform extends PSDefaultExtension
implements
IPSFieldInputTransformer
{
private static Log log = LogFactory.getLog(HashCodeFieldTransform.class);

private IPSExtensionDef extDef = null; 
	
	@Override
	public Object processUdf(Object[] params, IPSRequestContext request)
			throws PSConversionException {
		
	
		PSOExtensionParamsHelper helper = new PSOExtensionParamsHelper(extDef,
		            params, request, log);
		
		 return helper.getParameter("source").hashCode();
	}

	@Override
	public void init(IPSExtensionDef def, File codeRoot)
			throws PSExtensionException {
		  super.init(def, codeRoot);
	      extDef = def; 

	}

}
