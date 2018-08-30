package com.walmart.ticketservice.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.walmart.ticketservice.common.utils.CommonUtil;

/**
 * A venue is a queue of seat rows
 * Seats at front have higher priority than those at the back
 * @author bkulkar
 *
 */
public class Venue {
	
	
	/**
	 * Priority queue to store rows in descending order of priority
	 * Rows start with 0 (front row) to N (last row)
	 * i.e. front rows have higher priority - the greater the row number, lower is the priority
	 */
	PriorityQueue<SeatRowBlock> availableSeatRows;
	/**
	 * Storing seatScore so that repetitive calculations are not required
	 */
	static float[] seatScores;
	/**
	 * Seats that are successfully reserved mapped with confirmation code as the key
	 */
	Map<String, List<Seat>> seatsReserved;

     /**
     * @param numOfRows
     * @param seatsPerRow
     */
    public Venue(int numOfRows, int seatsPerRow) {
    	 
    	 availableSeatRows = new PriorityQueue<SeatRowBlock>(new Comparator<SeatRowBlock>() {
			@Override
			public int compare(SeatRowBlock o1, SeatRowBlock o2) {
				return o1.getRowId() - o2.getRowId();
			}
    	 }) ;
    	 
    	 seatScores = CommonUtil.getScores(seatsPerRow);
    	 
    	 for(int i = 0 ; i < numOfRows ; i ++) {
    		 availableSeatRows.add(new SeatRowBlock(i, seatsPerRow, seatScores));
    	 }
    	 
    	 this.seatsReserved = new HashMap<String, List<Seat>>();
    }
      
	public PriorityQueue<SeatRowBlock> getAvailableSeatRows() {
		return availableSeatRows;
	}

	public void setAvailableSeatRows(PriorityQueue<SeatRowBlock> availableSeatRows) {
		this.availableSeatRows = availableSeatRows;
	} 
	
	public Map<String, List<Seat>> getSeatsReserved() {
		return seatsReserved;
	}

	public void setSeatsReserved(Map<String, List<Seat>> seatsReserved) {
		this.seatsReserved = seatsReserved;
	}

	
	/**
	 * Printing rows from queue 
	 * @param seatsPerRow
	 */
	public synchronized void printRowsWithScores(int seatsPerRow) {
		   
		   System.out.println("\nSeats in each row are numbered as : \n");
		   for(int i = 0 ; i< seatsPerRow ; i++) {
			   System.out.print(" | " + i  + " |");
		   }
		   System.out.println("");
		   System.out.println("\nEach seat in row is scored as  : "  );
	       this.availableSeatRows.stream().sorted().forEach(seatRowBlock-> {
	    	  System.out.println();
	    	  System.out.print("ROW "+ seatRowBlock.getRowId() + " : ");
	    	  seatRowBlock.getSeats().forEach(seat -> {
	    		  System.out.print(" | " + seat.getScore() + " | ");
	    	  });
	      });
	}
	
}
