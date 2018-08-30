package com.walmart.ticketservice.model;

import java.util.List;

/**
 * Seat hold contains information 
 * related to seats held for a particular customer
 * @author bkulkar
 *
 */
public class SeatHold {
	
	/**
	 * List of seats currently held
	 * for the seat hold id
	 */
	private List<Seat> seatsHeld;
	/**
	 * Row in the venue that
	 * the seats held belong to 
	 */
	private int rowNum;
	/**
	 * Seat hold creation time
	 */
	private long timeCreated;
	/**
	 * Email id of customer for 
	 * whom the seats are held
	 */
	private String custEmailId;
	/**
	 * Unique identifier for the seat hold
	 */
	private Integer seatHoldId;
	
	public SeatHold(int rowNum, List<Seat> seatsHeld, long timeCreated, String custEmailId, Integer seatHoldId) {
		this.seatsHeld = seatsHeld;
		this.timeCreated = timeCreated;
		this.custEmailId = custEmailId;
		this.seatHoldId = seatHoldId;
		this.rowNum = rowNum;
	}

	public List<Seat> getSeatsHeld() {
		return seatsHeld;
	}

	public void setSeatsHeld(List<Seat> seatsHeld) {
		this.seatsHeld = seatsHeld;
	}

	public long getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(long timeCreated) {
		this.timeCreated = timeCreated;
	}

	public String getCustEmailId() {
		return custEmailId;
	}

	public void setCustEmailId(String custEmailId) {
		this.custEmailId = custEmailId;
	}

	public Integer getSeatHoldId() {
		return seatHoldId;
	}

	public void setSeatHoldId(Integer seatHoldId) {
		this.seatHoldId = seatHoldId;
	}

	public int getRowNum() {
		return rowNum;
	}
	
	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}

	public void printSeatsHeld(){
		 System.out.print("\nSeats held in ROW " + this.getRowNum() + "\nSeat Numbers: ");
		 this.getSeatsHeld().forEach(seat -> {
		   	  System.out.print(" " + seat.getId() + " ");
		  });
		 System.out.println("\nYour seat hold id: " + this.getSeatHoldId());
	}
}
