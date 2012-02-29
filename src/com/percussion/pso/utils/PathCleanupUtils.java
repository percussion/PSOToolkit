package com.percussion.pso.utils;

public class PathCleanupUtils {

	public static String cleanupPathPart(String path,boolean forceLower,boolean includesExtension) {
		String ext="";
	    path = path.replaceAll("[&]", " and ");
	    path = path.replaceAll("[^0-9a-zA-Z-_/ \\.\\\\]", "");
	    int extIndex = path.lastIndexOf(".");
	    if (forceLower) path=path.toLowerCase();
		if (extIndex > 0 && includesExtension) {
			ext=path.substring(extIndex+1,path.length());
		   path=path.substring(0,extIndex);
		   path = path.replaceAll("[-_ \\.\\\\]+", "-");
		   ext = ext.replaceAll("[-_ \\.\\\\]+", "-");
			if (forceLower) ext=ext.toLowerCase();
			path=path+"."+ext;
		} else {
			path = path.replaceAll("[-_ \\.\\\\]+", "-");
		}
	    return path;
	}
	

}
