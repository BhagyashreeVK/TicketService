package com.walmart.ticketservice.validator;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.walmart.ticketservice.common.utils.CommonUtil;
import com.walmart.ticketservice.exception.ApplicationException;
import com.walmart.ticketservice.model.SeatHold;
import com.walmart.ticketservice.model.Venue;

/**
 * Validator for ticket service requests
 * @author bkulkar
 *
 */
public class Validator {
	
	private static final Logger logger = LoggerFactory.getLogger(Validator.class);
	
	/**
	 * Check if number of seats requested, customer email and venue are valid values
	 * @param numSeats
	 * @param customerEmail
	 * @param venue
	 * @throws ApplicationException
	 */
	public static synchronized void validateEmailNumberofSeats(int numSeats, String customerEmail, Venue venue) throws ApplicationException{
		
		if(CommonUtil.isInvalid(venue) || CommonUtil.isInvalid(venue.getAvailableSeatRows())) {
			logger.error("Error occured while trying to get available seat rows from venue");
			throw new ApplicationException("Unexpected error occured");
		}
		
		if(CommonUtil.isInvalid(numSeats)) {
			logger.error("Error occured while trying to process request : Invalid number of seats requested");
			throw new ApplicationException("Invalid number of seats requested!");
		}
		
		if(!CommonUtil.isValidEmailFormat(customerEmail)) {
			logger.error("Error occured while trying to process request : Invalid customer email id");
			throw new ApplicationException("Customer email is invalid.");
		}
		
	}
	
    /**
     * Check if seat hold id, customer email and current seat holds are valid values
     * @param seatHoldId
     * @param customerEmail
     * @param currentSeatHolds
     * @throws ApplicationException
     */
    public static synchronized void validateSeatHoldIdEmail(int seatHoldId, String customerEmail, Map<Integer, SeatHold> currentSeatHolds) 
    		throws ApplicationException{
    	
    	//customer Email
		if(!CommonUtil.isValidEmailFormat(customerEmail)) {
			logger.error("Error occured while trying to process request : Invalid customer email id");
			throw new ApplicationException("Customer email is invalid.");
		}
		
		//check if seat Hold expired or invalid
		if(CommonUtil.isInvalid(currentSeatHolds) || !currentSeatHolds.containsKey(seatHoldId) 
				|| CommonUtil.isInvalid(currentSeatHolds.get(seatHoldId).getSeatsHeld())) {
			logger.error("Error occured while trying to process request : SeatHold Id does not exist");
			throw new ApplicationException("SeatHold does not exist");
		}
		
		if(!currentSeatHolds.get(seatHoldId).getCustEmailId().equalsIgnoreCase(customerEmail)) {
			logger.error("Error occured while trying to process request : Email Id associated with seat hold is different");
			throw new ApplicationException("Given customer email id is different from email associated with the seat hold");
		}
		
	}
    
    
    /**
     * Check if confirmation code is valid and 
     * there exists a list of seats associated with it
     * @param venue
     * @param confirmationCode
     * @throws ApplicationException
     */
    public static synchronized void validateReservedSeatsOrder(Venue venue, String confirmationCode) throws ApplicationException {
    	if( CommonUtil.isInvalid(venue) || CommonUtil.isInvalid(venue.getSeatsReserved()) || 
    	          !venue.getSeatsReserved().containsKey(confirmationCode)) {
    		 logger.error("Error occured while processing request: No seats found");
    	     throw new ApplicationException("Unable to verify confirmation code.");
    	 }
    }

}
