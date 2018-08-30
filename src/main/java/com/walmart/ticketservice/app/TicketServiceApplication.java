package com.walmart.ticketservice.app;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.walmart.ticketservice.common.utils.CommonUtil;
import com.walmart.ticketservice.model.Seat;
import com.walmart.ticketservice.model.SeatHold;
import com.walmart.ticketservice.model.Venue;
import com.walmart.ticketservice.service.TicketServiceImpl;

/**
 * Ticket service application creates a Venue by taking number of rows and number of seats per row as input from user
 * Based on user's choices it 
 *  --finds and holds seats
 *  --reserves seats
 *  --retrieves seat numbers of reserved seats if confirmation number is provided
 * Seats are held for 60 seconds before they become available again.
 * 
 * @author bkulkar
 *
 */
public class TicketServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(TicketServiceApplication.class);
	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
	int numOfRows = 0, seatsPerRow = 0, attempts = 0;
	  
	//Three attempts to intialize venue with valid number of rows and seats per row
	 while(true) {
		  
		 if(attempts >= 3) {
			System.out.println("Maximum attempts to initialize app reached!");  
			System.out.println("Exiting app....");
			logger.error("Could not initialize app");
		    System.exit(0);
	      }
	  
	  try{
		    System.out.println("************** TICKET RESERVATION APP ********************");
		  	System.out.println("Please enter the number of rows:");
	    	Scanner rowScanner = new Scanner(System.in);
	    	numOfRows = rowScanner.nextInt();
	    	if(CommonUtil.isInvalid(numOfRows)) {
	    		throw new IllegalArgumentException();
	    	}
	    	System.out.println("Please enter the number of seats per row:");
	    	Scanner colScanner = new Scanner(System.in);
	    	seatsPerRow = colScanner.nextInt();
	    	if(CommonUtil.isInvalid(seatsPerRow)) {
	    		throw new IllegalArgumentException();
	    	}
	    } catch (InputMismatchException | IllegalArgumentException ex){
	    	logger.error("Error occured while initializing app - " + ex.getMessage());
	        System.out.println("Could not initialize app. Please try again with a valid integer value greater than 0");
	    	++attempts;
	        continue;
	        
	    }
	  
	  logger.info("Initialized venue with " + numOfRows + " rows and " + seatsPerRow + " seats per row" );
	  break;
	}
		   //Instantiate with valid venue
		    TicketServiceImpl ticketService = new TicketServiceImpl(new Venue(numOfRows, seatsPerRow));
		    ticketService.getVenue().printRowsWithScores(seatsPerRow);
		    System.out.println("");
	
	    //Present choices to user until user chooses to exit
	   while (true) {
	        System.out.println("");
	        System.out.println("Please enter option number from choices below");
	        System.out.println("1. Get number of available seats");
	        System.out.println("2. Find best available seats and hold");
	        System.out.println("3. Reserve seats");
	        System.out.println("4. Show my reserved seats");
	        System.out.println("5. Exit");
	        System.out.println("");
	
	        Scanner scanner = new Scanner(System.in);
	        int input;
	        try{
	            input = scanner.nextInt();
	        }
	        catch (InputMismatchException e){
	        	logger.error("Invalid choice number input from user");
	            System.out.println("Please enter a valid choice.");
	            break;
	        }
	
	        switch (input) {
	
	            case (1): {
	                System.out.println("Number of available seats at the venue: " + ticketService.numSeatsAvailable());
	                break;
	            }
	
	            case (2): {
	            	
	            	if(ticketService.numSeatsAvailable() == 0) {
	            		System.out.println("\nNo free seats available right now. Please try again in some time.");
	            		break;
	            	}
	            	
	                System.out.println("Please enter your email address:");
	                Scanner scanner1 = new Scanner(System.in);
	                String email;
	                try {
	                    email = scanner1.next();
	                    
	                    if (!CommonUtil.isValidEmailFormat(email)) {
	                    	throw new IllegalArgumentException();
	                    }
	
	                } catch (InputMismatchException | IllegalArgumentException e) {
	                	logger.error("Invalid email input from user " + e.getMessage());
	                    System.out.println("Please enter a valid email.");
	                    break;
	                }
	
	                System.out.println("Please select number of seats to hold:");
	                Scanner scanner2 = new Scanner(System.in);
	                Integer numberOfSeats;
	                try {
	                    numberOfSeats = scanner2.nextInt();
	                    
	                    if(CommonUtil.isInvalid(numberOfSeats)) {
			        		throw new IllegalArgumentException();
			        	}
	                    
	                    if (numberOfSeats > ticketService.numSeatsAvailable()) {
	                        System.out.format(
	                                "Sorry, there are only %d seats available.",
	                                ticketService.numSeatsAvailable());
	                        break;
	                    }
	
	                } catch (InputMismatchException | IllegalArgumentException e) {
	                	logger.error("Invalid number of seats input by user " + e.getMessage());
	                    System.out.println("Please enter a valid number of seats.");
	                    break;
	                }
	
	                SeatHold seatHold;
					try {
						seatHold = ticketService.findAndHoldSeats(numberOfSeats, email);
					
	                if (!CommonUtil.isInvalid(seatHold)) {
	                	logger.info("Seats held successfully");
	                    System.out.println("Successfully found seats");
	                    seatHold.printSeatsHeld();
	                    System.out.println( "\nSeats will be held for you for " + (ticketService.getHoldTimeout() / 1000) + " seconds, "
	                    		+ "please reserve before seat hold expires.");
	                } else {
	                    System.out.println("Adjacent seats in the same row are not available! Please try again with fewer seats");
	                }
	                
					} catch (Exception e) {
						logger.error("Unexpected error occured - " + e.getMessage());
						System.out.println("Oops something went wrong! Please try again.");
					}
	
	                break;
	            }
	
	            case (3): {
	            	
	            	if(CommonUtil.isInvalid(ticketService.getCurrentSeatHolds())) {
	            		System.out.println("\nNo seat holds exist in the system. Please hold seats first and try again.");
	                	break;
	                 }
	            	
	                System.out.println("Please enter your seat hold ID:");
	                Scanner scanner1 = new Scanner(System.in);
	                Integer holdId;
	                try {
	                    holdId = scanner1.nextInt();
	                } catch (InputMismatchException e) {
	                	logger.error("Invalid seat hold id input by user " + e.getMessage());
	                    System.out.println("Please enter a valid seat hold ID (should be a number).");
	                    break;
	                }
	                
	                System.out.println("Please enter your email address:");
	                
	                Scanner scanner2 = new Scanner(System.in);
	                String email;
	                try {
	                    email = scanner2.next();
	                    if (!CommonUtil.isValidEmailFormat(email)) {
	                       throw new IllegalArgumentException();
	                    }
	
	                } catch (InputMismatchException | IllegalArgumentException e) {
	                	logger.error("Invalid email input by user");
	                    System.out.println("Please enter a valid email.");
	                    break;
	                }
	
	                String confirmationCode = null;
					try {
						confirmationCode = ticketService.reserveSeats(holdId, email);
						if (!CommonUtil.isInvalid(confirmationCode)) {
	                        System.out.println("Congratulations! You have successfully reserved your seats!"
	                        		+ "\nYour confirmation code is " + confirmationCode);
	                    } 
					} catch ( Exception e) {
						logger.error("Unexpected error occured - " + e.getMessage());
						System.out.println("Oops! Something went wrong.");
						System.out.println("Hold request no longer exists, please try again.");
					}
	
	                break ;
	            }
	
	            case (4): {
	                System.out.println("Please enter your confirmation code:");
	                Scanner scanner1 = new Scanner(System.in);
	                String code = scanner1.next();
	                List<Seat> seatsReserved = null;
	              
	                try {
	                	seatsReserved = ticketService.getReservedSeats(code);
	                	//seats not found
	                	if(CommonUtil.isInvalid(seatsReserved)) {
	                		throw new NullPointerException();
	                	}
	                	//seats found
	                	System.out.print("\nYour seats are in ROW " + seatsReserved.get(0).getRowNum() 
	                			+"\nSeat numbers are:");
	                	seatsReserved.stream().forEach(seat -> {
	 	                	System.out.print(" " + seat.getId());
	 	                });
	                	System.out.println("");
					   } catch (Exception ex) {
						logger.error("Error occured - " + ex.getMessage());
						System.out.println("Sorry, unable to find your order!");
					 }
	                break;
	            }
	
	            case (5): {
	            	try {
	            	   ticketService.shutdown();
	            	} catch (Exception e) {
	            		logger.error("Exception while shutting down thread" + e.getMessage());
	            	} finally {
	                  System.out.println("Exiting app....");
	                  System.exit(0);
	            	}
	            }
	
	            default: {
	                System.out.println("Wrong input. Please try again between the given choices.");
	            }
	
	        }
	    }

	}

}
