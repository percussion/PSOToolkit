/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * test.percussion.pso.transform PSOThumbnailGeneratorTest.java
 *
 */
package test.percussion.pso.transform;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.transform.PSOThumbnailGenerator;

public class PSOThumbnailGeneratorTest
{
   static Log log = LogFactory.getLog(PSOThumbnailGeneratorTest.class);
   
   TestThumbnailGenerator cut;  
   
   
   @Before
   public void setUp() throws Exception
   {
      cut = new TestThumbnailGenerator(); 
   }
   
   @Test
   public final void testComputeSizeBoth()
   {
      Dimension originalSize = new Dimension(400,300); 
      Dimension result = cut.computeSize(0, 100, 200, originalSize); 
      assertNotNull(result);
      assertEquals(100, result.width);
      assertEquals(200, result.height); 
   }
   
   @Test
   public final void testComputeSizeWidth()
   {
      Dimension originalSize = new Dimension(100,200); 
      Dimension result = cut.computeSize(0, 50, 0 , originalSize); 
      assertNotNull(result);
      assertEquals(50, result.width);
      assertEquals(100, result.height); 
   }
   @Test
   public final void testComputeSizeHeight()
   {
      Dimension originalSize = new Dimension(100,200); 
      Dimension result = cut.computeSize(0, 0 , 100 , originalSize); 
      assertNotNull(result);
      assertEquals(50, result.width);
      assertEquals(100, result.height); 
   }
   @Test
   public final void testComputeSizeNone()
   {
      Dimension result;
      try
      {
         Dimension originalSize = new Dimension(100,200); 
         result = cut.computeSize(0, 0 , 0 , originalSize); 
         
      } catch (IllegalArgumentException ex)
      {
            log.info("Expected Exception caught");
            assertTrue("expected exception", true); 
      }
   }
   
   @Test
   public final void testComputeSizeMaxdimHeight()
   {
      Dimension originalSize = new Dimension(100,200); 
      Dimension result = cut.computeSize(100, 0 , 0 , originalSize); 
      assertNotNull(result);
      assertEquals(50, result.width);
      assertEquals(100, result.height); 
   }
   
   @Test
   public final void testComputeSizeMaxdimWidth()
   {
      Dimension originalSize = new Dimension(200,100); 
      Dimension result = cut.computeSize(100, 0 , 0 , originalSize); 
      assertNotNull(result);
      assertEquals(100, result.width);
      assertEquals(50, result.height); 
   }

   @Test
   public final void testHalfImage()
   {
      BufferedImage inImage = new BufferedImage(1000, 2000, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = inImage.createGraphics();
      g2d.setColor(Color.CYAN); 
      g2d.fillRect(0, 0, 1000, 2000);
      g2d.dispose(); 
      
      BufferedImage result = cut.halfImage(inImage); 
      assertNotNull(result);
      assertEquals(500, result.getWidth());
      assertEquals(1000, result.getHeight()); 
      assertEquals(Transparency.OPAQUE, result.getTransparency());
   }
   
   private class TestThumbnailGenerator extends PSOThumbnailGenerator
   {

      @Override
      public Dimension computeSize(int maxDim, int thumbWidth,
            int thumbHeight, Dimension originalSize)
      {
         return super.computeSize(maxDim, thumbWidth, thumbHeight, originalSize);
      }

      @Override
      public BufferedImage halfImage(BufferedImage inImage)
      {
         return super.halfImage(inImage);
      }

     
   }
}
