package com.walmart.ticketservice.model;

import java.util.ArrayList;
import java.util.List;


/**
 * A seat row block is a block of continuous free seats  i.e. 
 * list of available seats that are adjacent to each other 
 * in a particular row
 * @author bkulkar
 *
 */
public class SeatRowBlock implements Comparable<Object> {

	/**
	 * List of seats in the current row that are 
	 * adjacent to each other and available
	 */
	private List<Seat> seats;
	/**
	 * Associated row in the queue
	 */
	private int rowId;
	
	/**
	 * @param rowId
	 * @param seatsPerRow
	 * @param seatScores
	 */
	public SeatRowBlock(int rowId, int seatsPerRow, float[] seatScores) {
		this.rowId = rowId;
		seats = new ArrayList<Seat>(seatsPerRow);
		
		for(int index=0; index < seatsPerRow; index++){
			seats.add(index, new Seat(index, seatScores[index], rowId));
		}
	}
	
	/**
	 * @param rowId2
	 * @param subList
	 */
	public SeatRowBlock(int rowId2, List<Seat> subList) {
        this.rowId = rowId2;
        this.seats = subList;
	}

	public List<Seat> getSeats() {
		return seats;
	}

	public void setSeats(List<Seat> seats) {
		this.seats = seats;
	}

	public int getRowId() {
		return rowId;
	}
	
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	
	public int getAvailableSeats() {
		return this.getSeats().size();
	}

	/* *
	 * Comparing objects based on row id as 
	 * row id is the priority deciding factor
	 */
	@Override
	public int compareTo(Object o) {
		 if (o instanceof SeatRowBlock) {
	            return this.rowId - ((SeatRowBlock) o).getRowId();
	        } else {
	            throw new IllegalArgumentException(o.getClass().getName() + " cannot be compared to " +
	            		SeatRowBlock.class.getName());
	        }
	    }
	
}
