package nuber.students; // Define the package for this class

import java.util.Date; // Import the Date class to get the current time
import java.util.concurrent.Callable; // Import Callable interface for defining asynchronous tasks
import java.util.concurrent.atomic.AtomicInteger; // Import AtomicInteger for thread-safe counting

public class Booking implements Callable<BookingResult> { // Define the Booking class that implements Callable interface, returns BookingResult

	final NuberDispatch dispatch; // Declare the final variable dispatch to represent the dispatch object
	private final Passenger passenger; // Declare the final variable passenger to represent the passenger object
	private long startTime; // Declare a variable startTime to record the booking start time
	private long endTime; // Declare a variable endTime to record the booking end time
	private static AtomicInteger jobCounter = new AtomicInteger(0); // Static counter to generate unique job IDs

	// Constructor to initialize dispatch and passenger, and record the booking start time
	public Booking(NuberDispatch dispatch, Passenger passenger) {
		this.dispatch = dispatch; // Assign the provided dispatch to the class member
		this.passenger = passenger; // Assign the provided passenger to the class member
		this.startTime = new Date().getTime(); // Get the current timestamp and assign it to startTime
	}

	@Override
	public BookingResult call() throws Exception { // Implement the call method from Callable interface
		// 1. Ask Dispatch for an available driver
		Driver driver = null; // Declare a Driver variable driver and initialize it as null
		// Loop to get an available driver, retrying until successful
		while ((driver = dispatch.getAvailableDriver()) == null) {
			synchronized (this) { // Enter synchronized block to ensure thread safety
				wait(); // Wait until an available driver is found
			}
		}

		// Pass the current Booking object to the Driver
		driver = new Driver(driver.getName(), driver.getMaxSleep()); // Create a new Driver object, copying the driver's name and max sleep time

		// 2. Call Driver.pickUpPassenger()
		driver.pickUpPassenger(passenger); // Call the driver's pickUpPassenger method to pick up the passenger

		// 3. Call Driver.driveToDestination()
		driver.driveToDestination(); // Call the driver's driveToDestination method to drive to the destination

		// 4. Record the end time
		endTime = new Date().getTime(); // Get the current timestamp and assign it to endTime, recording the end of the booking

		// 5. Add the driver back to the available list
		dispatch.addAvailableDriver(driver); // Add the driver back to the available drivers list in dispatch

		// 6. Return the BookingResult with a unique jobID
		return new BookingResult( // Create and return a BookingResult object
				jobCounter.incrementAndGet(),  // Generate a unique job ID using the counter
				passenger, // Pass the passenger object to BookingResult
				driver, // Pass the driver object to BookingResult
				(endTime - startTime) // Calculate and pass the booking duration (in milliseconds)
		);
	}
}
