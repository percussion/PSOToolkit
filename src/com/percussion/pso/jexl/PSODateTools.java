package com.percussion.pso.jexl;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;

/***
 * Tools for working with date and time.
 * 
 * @author natechadwick
 *
 */
public class PSODateTools extends PSJexlUtilBase implements IPSJexlExpression {


	 /**
	    * Logger for this class
	    */
	   private static final Log log = LogFactory.getLog(PSODateTools.class);


	   public PSODateTools(){}
	   
	   
	   @IPSJexlMethod(description="Formats a date using the SimpleDateFormat. Returns todays date if date_value is null",
		         params={@IPSJexlParam(name="format", description="format string"), 
			   			 @IPSJexlParam(name="date_value", description="the date value")})
		   public String formatDate(String format, Object date_value) throws ParseException
		   {
		   		SimpleDateFormat fmt = new SimpleDateFormat(format);
		   		
		   		if(date_value==null || date_value.toString().equals(""))
		   			date_value= Calendar.getInstance().getTime();
		   
		  		
		   		return fmt.format(date_value);
		   }

	
}
