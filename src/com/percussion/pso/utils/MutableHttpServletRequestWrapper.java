/*
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.utils MutableHttpServletRequestWrapper.java
 *
 */
package com.percussion.pso.utils;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.percussion.utils.collections.PSFacadeMap;

/**
 * An HttpServletRequestWrapper that allows modification of the request parameters.
 * This is generally useful when forwarding to servlets or building filter
 * chains.  
 *
 * @author DavidBenua
 *
 */
public class MutableHttpServletRequestWrapper extends HttpServletRequestWrapper
      implements
         HttpServletRequest
{
   PSFacadeMap<String, String[]> localParams; 
   /**
    * Constructs a new wrapper based on an existing request.
    * @param request the request to wrap. 
    */
   @SuppressWarnings("unchecked")
   public MutableHttpServletRequestWrapper(HttpServletRequest request)
   {
      super(request);
      localParams = new PSFacadeMap<String, String[]>(request.getParameterMap());
    
   }
   
   /**
    * Add a parameter with multiple values
    * @param key the parameter name
    * @param values the values to add; 
    */
   public void setParameter(String key, String[]values)
   {
      localParams.put(key, values); 
   }
   
   /**
    * Add a parameter with a single value
    * @param key the parameter name.
    * @param value the new value. 
    */
   public void setParameter(String key, String value)
   {
      String[] values = new String[]{value};
      setParameter(key, values); 
   }

   /**
    * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
    */
   @Override
   public String getParameter(String name)
   {
     String[] vals = getParameterValues(name);
     if(vals == null || vals.length == 0)
     {
        return null;
     }
     return vals[0]; 
   }

   /**
    * @see javax.servlet.ServletRequestWrapper#getParameterMap()
    */
   @Override
   @SuppressWarnings("unchecked")
   public Map getParameterMap()
   {
      return Collections.unmodifiableMap(localParams); 
   }

   /**
    * @see javax.servlet.ServletRequestWrapper#getParameterNames()
    */
   @Override
   @SuppressWarnings("unchecked")
   public Enumeration getParameterNames()
   {
      return Collections.<String>enumeration(
            localParams.keySet());
   }

   /**
    * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
    */
   @Override
   public String[] getParameterValues(String name)
   {  
      return localParams.get(name); 
   }
   
   
}
