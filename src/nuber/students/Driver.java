package nuber.students; // Define the package for this class

import java.util.concurrent.ThreadLocalRandom; // Import the class used to generate random numbers

public class Driver extends Person { // Define the Driver class, which inherits from the Person class

	private Passenger currentPassenger; // Declare a Passenger variable currentPassenger to store the current passenger
	private Booking booking; // Declare a Booking variable booking to store the booking information related to the driver

	/**
	 * Constructor for the Driver class.
	 *
	 * @param driverName The name of the driver.
	 * @param maxSleep   The maximum sleep time to simulate delay (in milliseconds).
	 */
	public Driver(String driverName, int maxSleep) {
		super(driverName, maxSleep); // Call the superclass (Person) constructor to initialize name and maximum sleep time
	}

	/**
	 * Stores the provided passenger as the driver's current passenger, then sleeps the thread for a time between 0 and maxDelay milliseconds.
	 *
	 * @param newPassenger The passenger to be picked up
	 * @throws InterruptedException If the thread is interrupted during sleep.
	 */
	public void pickUpPassenger(Passenger newPassenger) throws InterruptedException {
		this.currentPassenger = newPassenger; // Set the new passenger as the current passenger
		// Simulate delay (between 0 and maxSleep milliseconds)
		int delay = ThreadLocalRandom.current().nextInt(0, maxSleep + 1); // Generate a random delay time
		logEvent("Picking up passenger: " + newPassenger.name + " with delay: " + delay + " ms"); // Log the pickup event
		Thread.sleep(delay); // Sleep the thread for the specified delay time
	}

	/**
	 * Sleeps the thread for a time specified by the current passenger's getTravelTime() function.
	 *
	 * @throws InterruptedException If the thread is interrupted during sleep.
	 */
	public void driveToDestination() throws InterruptedException {
		if (currentPassenger == null) { // Check if there is a current passenger
			throw new IllegalStateException("No passenger to drive to destination."); // Throw an exception if no passenger is present
		}
		int travelTime = currentPassenger.getTravelTime(); // Get the travel time of the current passenger
		logEvent("Driving to destination: " + currentPassenger.name + " with travel time: " + travelTime + " ms"); // Log the driving event
		Thread.sleep(travelTime); // Sleep the thread for the specified travel time
	}

	/**
	 * Logs an event if logging is enabled.
	 *
	 * @param message The message to be displayed.
	 */
	private void logEvent(String message) {
		if (booking != null && booking.dispatch != null) { // Check if booking and dispatch are not null
			booking.dispatch.logEvent(booking, message); // Log the event through dispatch
		} else {
			System.out.println(message); // If no booking is available, print the message directly
		}
	}
}
