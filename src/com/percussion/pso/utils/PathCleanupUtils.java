package com.percussion.pso.utils;

import org.apache.commons.lang.StringUtils;

public class PathCleanupUtils {

	public static String cleanupPathPart(String path,boolean forceLower,boolean includesExtension, boolean stripExtension, String prefix, String suffix, String forceExtension) {
		String ext=null;
	    path = path.replaceAll("[&]", " and ");
	    path = path.replaceAll("[^0-9a-zA-Z-_/ \\.\\\\]", "");
	 
	    int extIndex = path.lastIndexOf(".");
	    if (forceLower) path=path.toLowerCase();
		if (StringUtils.isNotEmpty(forceExtension)) {
			ext = forceExtension;
		}
		if (extIndex > 0 && includesExtension) {
		   path=path.substring(0,extIndex);
		   path = path.replaceAll("[-_ \\.\\\\]+", "-");
		   if (!stripExtension && ext==null) {
			   ext=path.substring(extIndex+1,path.length());
			   ext = ext.replaceAll("[-_ \\.\\\\]+", "-");
			   if (forceLower) ext=ext.toLowerCase();
		   }
		} else {
			path = path.replaceAll("[-_ \\.\\\\]+", "-");
		}
		
	
		
		path=prefix + path+ suffix;
		
	    if (ext!=null) {
	    	path+="."+ext;
	    }
		
	    return path;
	}
	

}
