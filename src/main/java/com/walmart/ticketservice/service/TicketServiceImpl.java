package com.walmart.ticketservice.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.walmart.ticketservice.common.utils.CommonUtil;
import com.walmart.ticketservice.exception.ApplicationException;
import com.walmart.ticketservice.model.Seat;
import com.walmart.ticketservice.model.SeatHold;
import com.walmart.ticketservice.model.SeatRowBlock;
import com.walmart.ticketservice.model.Venue;
import com.walmart.ticketservice.validator.Validator;

/**
 * Ticket service implementation provides functionality for 
 * finding and holding best seats
 * reserving seats
 * removing seat holds that have expired
 * @author bkulkar
 *
 */
public class TicketServiceImpl implements TicketService {

	/**
	 * Venue that has been initialized with
	 * rows and seat scores
	 */
	private Venue venue;
	/**
	 * Thread to check seat holds that have expired 
	 * at regular intervals
	 */
	private Thread checkExpiredHolds;
	/**
	 * Seats currently held and are not available 
	 * until hold expires
	 * Mapped with seat hold id as key
	 */
	private Map<Integer, SeatHold> currentSeatHolds;
	/**
	 * Seat holds sorted and mapped with creation time as key value
	 * 
	 */
	private SortedMap<Long, Integer> timeoutToHolds;
	/**
	 * Default time out value (in milliseconds) 
	 * for a seat hold
	 */
	private static final long TIMEOUT_VALUE = 60 * 1000;
	/**
	 * Expiry time for a seat hold
	 */
	private long  holdTimeout;
	/**
	 * Atomic integer that incremented everytime to create a seat hold id
	 */
	private static final AtomicInteger count = new AtomicInteger(0); 
	
	private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);
	
	public TicketServiceImpl(Venue venue) {
		this(venue, TIMEOUT_VALUE);
	}
	
	public TicketServiceImpl(Venue venue, long timeout) {
		this.venue = venue;
	    this.holdTimeout = timeout;
		this.currentSeatHolds = new HashMap<>();
		this.timeoutToHolds = Collections.synchronizedSortedMap(new TreeMap<Long, Integer>());
		
		this.checkExpiredHolds = new Thread(() -> {
	            while (true) {
	                List<SeatHold> expiredHolds = new ArrayList<SeatHold>();
	                Iterator<Entry<Long, Integer>> mapIterator = this.timeoutToHolds.entrySet().iterator();
	                while(mapIterator.hasNext()){
	                	Entry<Long, Integer> entry = mapIterator.next();
	                    long expireTime = entry.getKey() + this.holdTimeout;
	                    if (System.currentTimeMillis() >= expireTime) {
	                        // This entry has expired. Add to list of holds to be released 
	                    	//and remove from current seat holds
	                    	if(!CommonUtil.isInvalid(currentSeatHolds) &&
	                    			currentSeatHolds.containsKey(entry.getValue())) {
	                           expiredHolds.add(currentSeatHolds.get(entry.getValue()));
	                           logger.info("Removing seat hold #" + entry.getValue() + "from current seat holds");
	                           currentSeatHolds.remove(entry.getValue());
	                           mapIterator.remove();
	                    	}
	                    } else {
	                        // No more holds have expired at the current time.
	                        break; // exit
	                    }
	                }

	                // Remove the expired holds.
	                if (expiredHolds.size() > 0) {
	                    this.removeExpiredHolds(expiredHolds);
	                }

	                try {
	                    Thread.sleep(1000);
	                } catch (InterruptedException e) {
	                    break; // exit
	                }
	            }
	        });
	        
		this.checkExpiredHolds.start();
	}
	
	/* 
	 * Returns number of available seats in the venue
	 */
	public synchronized int numSeatsAvailable() {
		
		if(CommonUtil.isInvalid(this.venue) || CommonUtil.isInvalid(this.venue.getAvailableSeatRows())) {
			logger.info("No seats available or venue is not valid");
			return 0;
		}
		
		logger.info("Calculating total available seats in Venue");
		return venue.getAvailableSeatRows().stream().
		        mapToInt(seatRowBlock -> seatRowBlock.getSeats().size()).sum();
	}

	/**
	 * This method will try to find best possible seats by looking for a seat row 
	 * that has high priority (i.e.from front to back) and continuous block of free seats i.e. available seats that are adjacent
	 * If it finds a free block in the row, large enough for requested number of seats, 
	 * it will find seats with high scores within that block
	 * If it doesn't find any free block large enough for requested number of seats, customer is requested to try again 
	 * with fewer number of seats
	 * 
	 * @param numSeats
	 * @param customerEmail
	 * @return seatHold
	 * 
	 */
	public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) throws ApplicationException{
		
		logger.info("Validating request parameters before finding best seats");
		Validator.validateEmailNumberofSeats(numSeats, customerEmail, this.getVenue());
		logger.info("Validation successful. Continue finding best seats");
		
		SeatHold seatHold = null;
		PriorityQueue<SeatRowBlock> availableSeats = this.venue.getAvailableSeatRows();
		List<SeatRowBlock> addBack = new ArrayList<>();
		
	  while(!availableSeats.isEmpty()){
			
	    //get highest priority element from queue
	    SeatRowBlock seatRow = availableSeats.poll();
			
		if(!CommonUtil.isInvalid(seatRow) && !CommonUtil.isInvalid(seatRow.getSeats())) {
			if(numSeats <= seatRow.getAvailableSeats()) {
				int seatCount = seatRow.getAvailableSeats();
				if(seatCount == numSeats) {
					//best seats found, get the row block and assign
					List<Seat> seatsToBeHeld = seatRow.getSeats();
					seatHold = new SeatHold(seatRow.getRowId(), seatsToBeHeld, System.currentTimeMillis(), customerEmail, count.incrementAndGet());
					break;		
				} else if( numSeats < seatCount) {
					//find best possible seats within the row
					List<Seat> seatsToBeHeld = findBestSeats(seatRow, numSeats, availableSeats);
					seatHold = new SeatHold(seatRow.getRowId(), seatsToBeHeld, System.currentTimeMillis(), customerEmail, count.incrementAndGet());
					break;		
				} 
			} else {
				//need to add this row back to original
				addBack.add(seatRow);
			}
		  }
	   }
		
		//add back rows that were removed while iteration and were not split
		if(!CommonUtil.isInvalid(addBack)) {
		   availableSeats.addAll(addBack);
		}
		
		//if seats found
		if(!CommonUtil.isInvalid(seatHold)) {
			currentSeatHolds.put(seatHold.getSeatHoldId(), seatHold);
		    timeoutToHolds.put(seatHold.getTimeCreated(), seatHold.getSeatHoldId());
		 }
		
		return seatHold;
	}

	/**
	 * 
	 * This method will get the seat row block to be searched for best seats as input
	 * Based on seat scores (seats in the middle have higher scores than those on the sides)
	 * it will find best seats within the row block 
	 * and split the rest of the row block into smaller row blocks of continuous free seats
	 * 
	 * @param seatRow
	 * @param numSeats
	 * @param availableSeats
	 * @return seatsToHold
	 */
	private synchronized List<Seat> findBestSeats(SeatRowBlock seatRow, int numSeats, PriorityQueue<SeatRowBlock> availableSeats) {
		
			//continuous seats in the current seat row
			List<Seat> seats = seatRow.getSeats();
			//Seats to be held
			List<Seat> seatsToHold = new ArrayList<Seat>(numSeats);
		
		    float maxScore = 0;
		    int startIndex = 0;
		    
		    //calculate scare of seats from 0 to numSeats i.e. seats from left to right in the current row
		   logger.info("Calculating scores for first " + numSeats+ " no. of seats");
	        for (int i=0; i<numSeats; i++) {
	                maxScore += seats.get(i).getScore();
	                seatsToHold.add(seats.get(i));
	        }
	        // Compute sums of remaining windows by
	        // removing first element of previous
	        // window and adding last element of 
	        // current window
	        float curr_sum = maxScore;
	        logger.info("Finding best seats with max scores");
	        for (int i=numSeats; i<seats.size(); i++)
	        {
	        		float prevSeatScore =  seats.get(i - numSeats).getScore();
	        		curr_sum += seats.get(i).getScore() - prevSeatScore;
	        		
	        		//update with better seat found based on the score
	        		if(curr_sum >= maxScore){
	        			maxScore = curr_sum;
	        			startIndex = i-numSeats+1;
	        			seatsToHold.add(seats.get(i));
	        			if(seatsToHold.size() > numSeats){
	        				seatsToHold.remove(0);
	        			}
	        	    }
	        }
	    
	    //split current seat row into separate blocks based on startIndex and number of seats to be held in the row  
	    //and add to queue
	   if(!CommonUtil.isInvalid(seatsToHold)) {     
			if (startIndex == 0) {
				availableSeats.add(new SeatRowBlock(seatRow.getRowId(), seats.subList(numSeats, seats.size())));
			} else if (startIndex > 0) {
				availableSeats.add(new SeatRowBlock(seatRow.getRowId(), seats.subList(0, startIndex)));
				if (startIndex + numSeats < seats.size())
					availableSeats.add(new SeatRowBlock(seatRow.getRowId(), seats.subList(startIndex + numSeats, seats.size())));
			}  
	   }
	   
		return seatsToHold;
		
	}

	/**
	 * This method will check whether the seat hold still exists or if it has expired
	 * If it exists and the customer email provided matches with the email id associated 
	 * with the seat hold, it will reserve the seats and return a confirmation code
	 * 
	 * @param seatHoldId
	 * @param customerEmail
	 * @return confirmationCode
	 * 
	 */
	public synchronized String reserveSeats(int seatHoldId, String customerEmail) throws ApplicationException{
		
		logger.info("Validating request parameters before reserving seats");
		Validator.validateSeatHoldIdEmail(seatHoldId, customerEmail, currentSeatHolds);
		logger.info("Validation successful. Continue reserving seats");
		//Generate confirmation code
		String confirmationCode = RandomStringUtils.random(8, true, true).toUpperCase();
		//add to seats reserved successfully
		this.venue.getSeatsReserved().put(confirmationCode, currentSeatHolds.remove(seatHoldId).getSeatsHeld());
		logger.info("Seats successfully reserved. Confirmation code : " + confirmationCode);
		return confirmationCode;
		
	}

	/**
	 * List of seat holds that have expired and need to be released
	 * @param holdsToRemove
	 */
	public synchronized void removeExpiredHolds(List<SeatHold> holdsToRemove) {
		for(SeatHold seatHold : holdsToRemove) {
			if(!CommonUtil.isInvalid(seatHold) && !CommonUtil.isInvalid(seatHold.getSeatsHeld())) {
				logger.info("Removing seat hold id: " + seatHold.getSeatHoldId());
			    //release seats held and check if it formers a larger continuous block 
			    //with currently available seats in the seatRow
			    //merge into the seat row 
			    mergeSeatRowBlocks(seatHold.getRowNum(), seatHold.getSeatsHeld());
			}
		}
	}
	
	/**
	 * This method will look for available row blocks (within the row to which it belongs) that could form a larger block of 
	 * continuously available seats if merged with the current list of seats to be released
	 * This is to avoid creating smaller blocks within the row, which could lead to seat unavailability despite 
	 * having adjacent seats available within the row
	 * If no such larger block is found, simply adds the current row block to the row to which it belongs
	 * to make it available for further holds
	 * 
	 * @param rowId
	 * @param seats
	 */
	public synchronized void mergeSeatRowBlocks(int rowId, List<Seat> seats) {

	//first and last index of seat block to be released	
	int firstIndex = seats.get(0).getId();
	int lastIndex = seats.get(seats.size()-1).getId();
	
	//get seatRowBlocks for the row id
	List<SeatRowBlock> seatRowBlocks = venue.getAvailableSeatRows().
			 stream().filter(s->s.getRowId() == rowId).sorted().collect(Collectors.toList());
	
	//to check if the seats to be released can form a continous row block with any other row block
	  for(int i=0; i < seatRowBlocks.size() ; i++) {
			SeatRowBlock seatRow = seatRowBlocks.get(i);
			if (!CommonUtil.isInvalid(seatRow.getSeats())) {
				List<Seat> seatsFrmRow = new CopyOnWriteArrayList<Seat>(seatRow.getSeats());
				Seat firstSeat = seatsFrmRow.get(0);
				Seat lastSeat = seatsFrmRow.get(seatsFrmRow.size() - 1);
				if (lastIndex == (firstSeat.getId() - 1)) {
					seats.addAll(seatsFrmRow);
					this.venue.getAvailableSeatRows().remove(seatRow);
				} else if (firstIndex == (1 + lastSeat.getId())) {
					seatsFrmRow.addAll(seats);
					seats = seatsFrmRow;
					this.venue.getAvailableSeatRows().remove(seatRow);
				}
			}
	  }
	
	  logger.info("Releasing seats and adding back to the queue ");
	  //add the released seats back to the queue
	  this.venue.getAvailableSeatRows().add(new SeatRowBlock(rowId, seats));
		
	}
	
 	/**
 	 * Returns a list of reserved seats associated with the confirmation code
 	 * @param confirmationCode
 	 * @throws ApplicationException
 	 */
 	public List<Seat> getReservedSeats(String confirmationCode) throws ApplicationException {
 		
 		logger.info("Validating request parameters before finding reserved seats");
		Validator.validateReservedSeatsOrder(this.getVenue(),confirmationCode);
		logger.info("Validation successful. Continue finding reserved seats");
	      
		return this.getVenue().getSeatsReserved().get(confirmationCode);
 	}
	
	 /**
	 * to shut down thread
	 */
	public void shutdown() {
	        try {
	            this.checkExpiredHolds.interrupt();
	            this.checkExpiredHolds.join(1000);
	        } catch (InterruptedException e) {
	            logger.warn("Exception while shutting down: " + e.toString());
	        }
	  }
	
	public Venue getVenue() {
		return venue;
	}

	public Map<Integer, SeatHold> getCurrentSeatHolds() {
		return currentSeatHolds;
	}

	public SortedMap<Long, Integer> getTimeoutToHolds() {
		return timeoutToHolds;
	}

	public long getHoldTimeout() {
		return holdTimeout;
	}
	
}
