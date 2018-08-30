package com.walmart.ticketservice;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.walmart.ticketservice.exception.ApplicationException;
import com.walmart.ticketservice.model.SeatHold;
import com.walmart.ticketservice.model.Venue;
import com.walmart.ticketservice.service.TicketServiceImpl;

/**
 * Tests for ticket service
 * @author bkulkar
 *
 */
public class TicketServiceApplicationTest {

	private static Venue venue;
	private static TicketServiceImpl ticketService;
	
	/**
	 * Initialize ticket service with a valid venue
	 * and time out for seat holds
	 */
	@BeforeClass
	public static void setup() {
		venue = new Venue(7, 5);
		ticketService =  new TicketServiceImpl(venue, 5000);
	}
	
	
	@Test
	public void seatsAvailable() {
		int noOfSeatsAvailable = ticketService.numSeatsAvailable();
		assertTrue(noOfSeatsAvailable > 0);
	}
	
	@Test
	public void findAndHoldSeats() {
		SeatHold seatHold = null;
		try {
			seatHold = ticketService.findAndHoldSeats(2, "user@yahoo.com");
		} catch (ApplicationException e) {
			//application exception occured
			Assert.fail("Unexpected error occured");
		}
		assertNotNull(seatHold);
		assertNotNull(seatHold.getSeatsHeld());
		assertTrue(seatHold.getSeatHoldId() > 0);
		assertTrue(seatHold.getSeatsHeld().size() > 0);
	}
	
	@Test
	public void findAndHoldSeatsInvalidInput() {
		
    	SeatHold seatHold = null;
		try {
			seatHold = ticketService.findAndHoldSeats(0, "user@yahoo.com");
		    Assert.fail("Exception expected");
		} catch (Exception ex) {
			assertNull(seatHold);
		}
		
		try {
			seatHold = ticketService.findAndHoldSeats(5, "user@.com");
			Assert.fail("Exception expected");
		} catch (Exception ex) {
			assertNull(seatHold);
		}
		
    }
    
	@Test
	public void findAndHoldSeats_NoContinuousSeatsAvailable() {
    	SeatHold seatHold = null;
		try {
			seatHold = ticketService.findAndHoldSeats(6, "user@yahoo.com");
		} catch (ApplicationException e) {
			Assert.fail("Unexpected error occured");
		}
		assertNull(seatHold);
    }
	
	@Test
	public void reserveSeats() {
		String confirmationCode = null;
		SeatHold seatHold = null;
		
		try {
			seatHold = ticketService.findAndHoldSeats(3, "user@yahoo.com");
			assertNotNull(seatHold);
			assertNotNull(seatHold.getSeatHoldId());
			assertNotNull(seatHold.getSeatsHeld());
		} catch (Exception e) {
			Assert.fail("Unexpected error occured");
		}
		
		try {
			confirmationCode = ticketService.reserveSeats(seatHold.getSeatHoldId(), "user@yahoo.com");
		} catch (Exception e) {
			Assert.fail("Unexpected error occured");
		}
		assertNotNull(confirmationCode);
	}
    
	@Test
	public void reserveSeatsInvalidInput() {
		
		String confirmationCode = null;
		SeatHold seatHold = null;
		
		try {
			seatHold = ticketService.findAndHoldSeats(3, "user@yahoo.com");
			assertNotNull(seatHold);
			assertNotNull(seatHold.getSeatHoldId());
			assertNotNull(seatHold.getSeatsHeld());
		} catch (Exception e) {
			Assert.fail("Unexpected error occured");
		}
		
		try {
			confirmationCode = ticketService.reserveSeats(seatHold.getSeatHoldId(), "user@.com");
			Assert.fail("Exception expected");
		} catch (Exception e) {
			assertNull(confirmationCode);
		}
		
		try {
			confirmationCode = ticketService.reserveSeats(seatHold.getSeatHoldId(), "user1@yahoo.com");
			Assert.fail("Exception expected");
		} catch (Exception e) {
			assertNull(confirmationCode);
		}
		
		try {
			confirmationCode = ticketService.reserveSeats(seatHold.getSeatHoldId(), "user1@yahoo.com");
			Assert.fail("Exception expected");
		} catch (Exception e) {
			assertNull(confirmationCode);
		}
		
	}
    
	@Test
	public void reserveSeats_ExpiredHold() {
		
		String confirmationCode = null;
		SeatHold seatHold = null;
		
		try {
			seatHold = ticketService.findAndHoldSeats(3, "user@yahoo.com");
			assertNotNull(seatHold);
			assertNotNull(seatHold.getSeatHoldId());
			assertNotNull(seatHold.getSeatsHeld());
			//sleep until timeout
			TimeUnit.MILLISECONDS.sleep(ticketService.getHoldTimeout()+1000);
		} catch (Exception e) {
			Assert.fail("Unexpected error occured");
		}
		
		try {
			confirmationCode = ticketService.reserveSeats(seatHold.getSeatHoldId(), "user@yahoo.com");
			Assert.fail("Error expected");
		} catch (Exception e) {
			assertNull(confirmationCode);
		}
		
   	}
    
	/**
	 * Clean up after tests are completed
	 */
	@AfterClass
	public static void teardown() {
		ticketService = null;
		venue = null;
	}
	
}
