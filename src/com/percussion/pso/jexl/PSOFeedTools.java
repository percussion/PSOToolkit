package com.percussion.pso.jexl;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.utils.HTTPProxyClientConfig;
import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.module.content.ContentModule;
import com.sun.syndication.feed.module.mediarss.MediaEntryModule;
import com.sun.syndication.feed.module.mediarss.MediaModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.module.mediarss.types.MediaGroup;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/***
 * Provides tools to work with Feeds such as RSS or Atom
 * @author natechadwick
 *
 */
public class PSOFeedTools extends PSJexlUtilBase implements IPSJexlExpression {

	  /**
	    * Logger for this class
	    */
	private static final Log log = LogFactory.getLog(PSOFeedTools.class);
	private static final String HTTP_LAST_MODIFIED="Last-Modified";
	private static final String HTTP_ETAG="ETag";
	private static final String HTTP_IFNONEMATCH="If-None-Match";
	
	public PSOFeedTools(){
		super();
	}
	
	 @IPSJexlMethod(description="Returns a Map of Feed parameters from a Jquery formatted feed. For example a post of url: 'http://rss.news.yahoo.com/rss/yahoonewsroom', targetFolder: '//Sites/EnterpriseInvestments/News'", 
	         params={@IPSJexlParam(name="params", description="The feed parameters")})
	   public Map<String,String> getFeedParameters(String params) throws IllegalArgumentException, FeedException, IOException {

		 log.debug(params);
		 URLCodec decode = new URLCodec();
		 HashMap<String,String> map = new HashMap<String,String>();
		 
		 
		 String[] opts = params.split("&");
		 String[] param;
		 for(String s : opts){
			 param = s.split("=");
			 
			 try {
				map.put(decode.decode(param[0].trim()), decode.decode(param[1].trim()));
			} catch (DecoderException e) {
				log.debug(params);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		 
		return map;
	 }

	
	 @IPSJexlMethod(description="Returns a ROME SyndFeed instance for the given URL", 
	         params={@IPSJexlParam(name="url", description="The URL of the feed to download.")})
	   public SyndFeed getFeed(String urlString) throws IllegalArgumentException, FeedException, IOException {

		 	HttpClient client = new HttpClient();
			
			//Set up the proxy server if there is one.
			HTTPProxyClientConfig proxy = new HTTPProxyClientConfig();
			
			if(!proxy.getProxyServer().equals("")){
				client.getHostConfiguration().setProxy(proxy.getProxyServer(), Integer.parseInt(proxy.getProxyPort()));
			}
				
			HttpMethod get = new GetMethod(urlString);

			try{
				int code = client.executeMethod(get);
				
				SyndFeedInput input = new SyndFeedInput();
				log.debug("Requesting feed from " + urlString);
				SyndFeed feed = input.build(new XmlReader(get.getResponseBodyAsStream()));
				return feed;	
			}finally{
				get.releaseConnection();
			}
	 }
	 
	 @IPSJexlMethod(description="Returns a ROME SyndFeed instance for the given URL.  Supports Optional GET based upon cached ETAG and LastModified Date.  Do not use unless cacheing these values.", 
	         params={@IPSJexlParam(name="url", description="The URL of the feed to download."),
			 		 @IPSJexlParam(name="eTag", description="A cached HTTP ETAG Headerfor the feed. "),
			 		 @IPSJexlParam(name="lastModified", description="A cached HTTP LastModified Header for the feed. ")})
	   public SyndFeed getFeed(String urlString, String eTag, String lastModified) throws IllegalArgumentException, FeedException, IOException {

		 	
			HttpClient client = new HttpClient();
		
			//Set up the proxy server if there is one.
			HTTPProxyClientConfig proxy = new HTTPProxyClientConfig();
			
			if(!proxy.getProxyServer().equals("")){
				client.getHostConfiguration().setProxy(proxy.getProxyServer(), Integer.parseInt(proxy.getProxyPort()));
			}
				
			HttpMethod get = new GetMethod(urlString);
			
			try{
			//Set the eTag and Last Modified header.  The server will not serve the page if these haven't changed. 
			get.addRequestHeader(HTTP_IFNONEMATCH,eTag);
			get.addRequestHeader(HTTP_LAST_MODIFIED, lastModified);
			
			int code = client.executeMethod(get);
			
			if(code==HttpStatus.SC_NOT_MODIFIED){
				log.debug("Feed URL not modified.");
				return null;
			}else if(code==HttpStatus.SC_OK){
				SyndFeedInput input = new SyndFeedInput();
				log.debug("Requesting feed from " + urlString);
				SyndFeed feed = input.build(new XmlReader(get.getResponseBodyAsStream()));
				return feed;	
			}else{
				log.debug("Unexpected response from server for url" + urlString + " Response Code:" + code);
				return null;
			}
			}finally{
				get.releaseConnection();
			}

	 }
	 
 	 
	}