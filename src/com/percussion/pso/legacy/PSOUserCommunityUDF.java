/*
 * COPYRIGHT (c) 1999 - 2010 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.legacy PSOUserCommunityUDF.java
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 */
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.legacy;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.server.IPSRequestContext;

/**
 * A UDF to get the user community name. 
 * This function, which has previously been available 
 * only in JEXL, can now be accessed in an XML application. 
 *
 * @author davidbenua
 *
 */
public class PSOUserCommunityUDF extends PSSimpleJavaUdfExtension
      implements
         IPSUdfProcessor
{
   /**
    * Gets the user community from user session.  
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] arg0, IPSRequestContext arg1)
         throws PSConversionException
   {
      PSOObjectFinder finder = new PSOObjectFinder(); 
      return finder.getUserCommunity(); 
   }
}
