package com.percussion.pso.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.server.PSServer;

/***
 * Contains the basic proxy client configuration
 * 
 * This file is read from rxconfig/Server/clientproxy.properties
 * proxyserver=
 * proxyport=
 * 
 * @author natechadwick
 *
 */
public class HTTPProxyClientConfig {

	 private static Log log = LogFactory.getLog(HTTPProxyClientConfig.class);
	   
	private String proxyServer;
	private String proxyPort;
	
	
	public void setProxyServer(String proxyServer) {
		this.proxyServer = proxyServer;
	}
	public String getProxyServer() {
		return proxyServer;
	}
	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}
	public String getProxyPort() {
		return proxyPort;
	}

	/***
	 * Loads the properties file and initializes the properties.  This could be made more efficient by
	 * moving it out of the constructor but this way the file can be changed without restarting the server.
	 */
	public HTTPProxyClientConfig(){
		 String propFile = PSServer.getRxFile(PSServer.BASE_CONFIG_DIR + "/Server/clientproxy.properties");
		 Properties props = new Properties();
		try {
				props.load(new FileInputStream(propFile));
		
				if(props.containsKey("proxyserver")){
					this.proxyServer = props.getProperty("proxyserver").trim();
				}else{
					this.proxyServer = "";
				}
				
				if(props.containsKey("proxyport")){
					this.proxyPort = props.getProperty("proxyport").trim();
				}else{
					this.proxyPort = "";
				}
				
		} catch (FileNotFoundException e) {
				log.debug(PSServer.BASE_CONFIG_DIR + "/Server/clientproxy.properties Configuration file not found.");
			} catch (Exception e) {
				e.printStackTrace();
				log.debug(e.getMessage());
			}
	}
	
}
