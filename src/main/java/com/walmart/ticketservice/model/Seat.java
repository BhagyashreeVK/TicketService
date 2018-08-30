package com.walmart.ticketservice.model;

/**
 * Seats with higher scores have higher preference
 * Seats in the middle have higher scores than those 
 * at the ends of the row
 * @author bkulkar
 *
 */
@SuppressWarnings("rawtypes")
public class Seat implements Comparable{
	
	/**
	 * Seat number 
	 */
	private int id;
	/**
	 * Score for the seat
	 */
	private float score;
	/**
	 * Row to which the seat belongs to
	 */
	private int rowNum;
	
	public Seat(int id, float score, int rowNum) {
		this.id = id;
		this.score = score;
		this.rowNum = rowNum;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}

	/**
	 *  Comparing seats based on seat id
	 *  as they need to be sorted from left to right 
	 */
	@Override
	public int compareTo(Object o) {
		if (o instanceof Seat) {
            return this.id - ((Seat) o).getId();
        } else {
            throw new IllegalArgumentException(o.getClass().getName() + " cannot be compared to " +
            		Seat.class.getName());
        }
    }

	@Override
	public String toString() {
		return "Seat id = " + id + ", row number = " + rowNum + "]";
	}

}
