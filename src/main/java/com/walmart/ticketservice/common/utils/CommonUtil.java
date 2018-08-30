package com.walmart.ticketservice.common.utils;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Common utility class
 * 
 * @author bkulkar
 *
 */
public class CommonUtil {
	
	// Regex for email validation
	private static final String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
	
	
	 /**
	 * Round score
	 */
	public static float round(float f) {
	        return (float) Math.round(f * 100) / 100;
	    }
	 
	 
	 
	 /**
	  * Calculate scores for every seat in a row of specified size
	  * Rows in the middle have higher score than the rows on the side
	 * @param seatsPerRow
	 * @return searScore
	 */
	public static float[] getScores(int seatsPerRow) {
		 
		 float[] seatScore = new float[seatsPerRow];
		 
		 for(int index=0; index < seatsPerRow; index++){
			    int mid = seatsPerRow / 2;
			    
			    //to handle division by zero
			    if(seatsPerRow <= 2 && 0 < seatsPerRow) {
			    	++mid;
			    }
			    
				if (seatsPerRow % 2 == 0) {
		            if (index < mid) {
		            	seatScore[index] = (float)(index + 1) * (float)(10.0f / (float)(mid - 1));
		            } else {
		            	seatScore[index] = (float)(seatsPerRow - index) * (float)(10.0f / (float)(mid - 1));
		            }
		        } else {
		           if (index <= mid) {
		        	   seatScore[index] = (float)(index + 1) * (float)(10.0f / (float)(mid));
		            } else { 
		            	seatScore[index] = (float)(seatsPerRow - index) * (float)(10.0f / (float)(mid));
		            }
		        }
				
				seatScore[index] = round(seatScore[index]);
			}
		 
		 return seatScore;
	 }
	 

	 
		/**
		 * Check if object is null, empty 
		 * or zero (in case of integer)
		 * @param obj
		 * @return
		 */
		@SuppressWarnings("rawtypes")
		public static synchronized boolean isInvalid(Object obj)
		   {
		      boolean isInValid = false;
		      if(null == obj) {
		    	  isInValid = true;
		      } 
		      else if(obj instanceof Integer){
		    	  isInValid = ((((Integer) obj).intValue()) == 0);
	          }
		      else if(obj instanceof Object[])
		      {
		         isInValid = (((Object[])obj).length == 0);
		      }
		      else if(obj instanceof Collection)
		      {
		         isInValid = ((Collection)obj).isEmpty();
		      }
		      else if(obj instanceof Map)
		      {
		         isInValid = (((Map)obj).isEmpty());
		      }
		      else
		      {
		         isInValid = (obj.toString().trim().length() == 0);
		      }
		      return isInValid;
		   }
		
		/**
		 * Check if email string is valid
		 * @param email
		 * @return boolean 
		 */
		public static boolean isValidEmailFormat(String email) {
		      if(null == email)
		              return false;
		       Pattern pat = Pattern.compile(EMAIL_REGEX);
		       return pat.matcher(email).matches();
					
		 }

}
