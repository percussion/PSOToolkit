/*
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * test.percussion.pso.utils MutableHttpServletRequestWrapperTest.java
 *
 */
package test.percussion.pso.utils;

import static org.junit.Assert.*;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;


import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.percussion.pso.utils.MutableHttpServletRequestWrapper;

public class MutableHttpServletRequestWrapperTest
{
   
   MutableHttpServletRequestWrapper cut;
   
   @Before
   public void setUp() throws Exception
   {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setParameter("foo", "foo");
      request.setParameter("bar", "bar");
      request.setParameter("baz", new String[]{"baz","baz"});
      cut = new MutableHttpServletRequestWrapper(request);
   }
   @Test
   public final void testGetParameterString()
   {
      cut.setParameter("bar", "barbar"); 
      assertEquals("foo", cut.getParameter("foo")); 
      assertEquals("barbar", cut.getParameter("bar")); 
      assertNull(cut.getParameter("fizz")); 
      cut.setParameter("fizz", "fizzfizz");
      assertEquals("fizzfizz", cut.getParameter("fizz"));
      cut.setParameter("bar", new String[]{"bat","ball"});
      assertEquals("bat", cut.getParameter("bar")); 
      String [] b = cut.getParameterValues("bar"); 
      assertNotNull(b);
      assertEquals(2,b.length); 
      assertEquals("ball",b[1]); 
      
   }
   @Test
   @SuppressWarnings("unchecked")
   public final void testGetParameterNames()
   {
      cut.setParameter("biz", "bzzzz");
      Set<String> v = new HashSet<String>();
      v.add("foo");
      v.add("bar");
      v.add("baz");
      v.add("biz"); 
      int i = 0; 
      Enumeration<String> nms = cut.getParameterNames();
      while(nms.hasMoreElements())
      {
         String n = nms.nextElement(); 
         assertTrue(v.contains(n));
         i++; 
      }
      assertEquals(4,i); 
   }
}
