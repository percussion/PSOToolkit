/*
 * test.percussion.pso.utils SimplifyParameterMapTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.percussion.pso.utils.SimplifyParameters;
import junit.framework.TestCase;

public class SimplifyParameterMapTest extends TestCase
{
   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(SimplifyParameterMapTest.class);
   }
  
   public void testSimplifyValue()
   {
     assertEquals("xyzzy",SimplifyParameters.simplifyValue("xyzzy")); 
     assertEquals("foo", SimplifyParameters.simplifyValue(new String[]{"foo", "bar"}));
     assertEquals("", SimplifyParameters.simplifyValue(new String[0])); 
     ArrayList<String> l = new ArrayList<String>();
     //empty list test
     assertEquals("",SimplifyParameters.simplifyValue(l)); 
     l.add("a");
     l.add("b");
     l.add("c"); 
     assertEquals("a",SimplifyParameters.simplifyValue(l)); 
     assertNull(SimplifyParameters.simplifyValue(null)); 
   }
   
   /*
    * Test method for 'com.percussion.pso.utils.SimplifyParameters.simplify(Map<String, Object>)'
    */
   public void testSimplifyMap()
   {
      Map<String,Object> inmap = new HashMap<String, Object>(); 
      List<String> inlist = new ArrayList<String>();
      inlist.add("X");
      inlist.add("Y");
      inlist.add("Z"); 
      inmap.put("list", inlist); 
      inmap.put("string", "xyzzy"); 
      inmap.put("array", new String[]{"fee","fie","fo","fum"});
      
      Map<String,String> outmap = SimplifyParameters.simplifyMap(inmap); 
      assertEquals("xyzzy",outmap.get("string")); 
      assertEquals("X", outmap.get("list")); 
      assertEquals("fee",outmap.get("array")); 
      
   }
}
