package nuber.students; // Define the package for this class

import java.util.*; // Import the collections framework
import java.util.concurrent.*; // Import the concurrent utilities
import java.util.concurrent.atomic.AtomicInteger; // Import AtomicInteger for atomic counting

public class NuberDispatch { // Define the NuberDispatch class

	private final int MAX_DRIVERS = 999; // Define the maximum number of drivers
	private boolean logEvents = false; // Flag to indicate if event logging is enabled
	private final BlockingQueue<Driver> idleDrivers = new LinkedBlockingQueue<>(); // Blocking queue to store idle drivers
	private final HashMap<String, NuberRegion> regions = new HashMap<>(); // HashMap to store region information
	private final AtomicInteger bookingsAwaitingDriver = new AtomicInteger(0); // Counter to track bookings waiting for drivers
	private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_DRIVERS); // Create a fixed thread pool

	/**
	 * Constructor for the NuberDispatch class.
	 *
	 * @param regionInfo HashMap containing region names and their maximum booking limits.
	 * @param logEvents  Flag to enable or disable event logging.
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents) {
		this.logEvents = logEvents; // Initialize the log events flag
		for (Map.Entry<String, Integer> entry : regionInfo.entrySet()) { // Iterate over region information
			String regionName = entry.getKey(); // Get the region name
			int maxBookings = entry.getValue(); // Get the maximum booking limit
			regions.put(regionName, new NuberRegion(this, regionName, maxBookings)); // Create and store NuberRegion instances
		}
	}

	/**
	 * Add a new driver to the idle drivers queue.
	 *
	 * @param newDriver The driver to add
	 * @return true if successfully added, otherwise false.
	 */
	public synchronized boolean addDriver(Driver newDriver) {
		if (idleDrivers.size() < MAX_DRIVERS) { // Check if the number of idle drivers exceeds the maximum
			idleDrivers.add(newDriver); // Add the driver to the idle queue
			return true; // Return success
		} else {
			return false; // Return failure
		}
	}

	/**
	 * Get an available driver, blocking if no driver is available.
	 *
	 * @return The available driver
	 * @throws InterruptedException If the thread is interrupted while waiting.
	 */
	public synchronized Driver getAvailableDriver() throws InterruptedException {
		while (idleDrivers.isEmpty()) { // If no idle drivers are available
			wait(); // Wait until a driver becomes available
		}
		notifyAll(); // Wake up all waiting threads
		return idleDrivers.poll(); // Return an idle driver
	}

	/**
	 * Re-add a driver to the idle queue and notify waiting threads.
	 *
	 * @param driver The driver to add
	 */
	public synchronized void addAvailableDriver(Driver driver) {
		idleDrivers.add(driver); // Add the driver back to the idle queue
		notifyAll(); // Wake up all waiting threads
	}

	/**
	 * Log an event if logging is enabled.
	 *
	 * @param booking  The associated booking information.
	 * @param message  The message to log.
	 */
	public void logEvent(Booking booking, String message) {
		if (!logEvents) return; // If logging is not enabled, return immediately
		System.out.println(booking + ": " + message); // Print the log message
	}

	/**
	 * Book a passenger in the specified region.
	 *
	 * @param passenger Passenger information.
	 * @param region    The booking region.
	 * @return A Future object representing the booking result.
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
		NuberRegion nuberRegion = regions.get(region); // Get the specified region
		if (nuberRegion == null || nuberRegion.isShutdown()) { // Check if the region is valid
			return null; // Return null if the region is invalid or shut down
		}
		bookingsAwaitingDriver.incrementAndGet(); // Increment the count of bookings waiting for drivers
		Callable<BookingResult> callable = () -> { // Create a Callable object to handle the booking
			Booking booking = new Booking(this, passenger); // Create a new Booking object
			return booking.call(); // Execute the booking and return the result
		};
		return executorService.submit(callable); // Submit the task to the thread pool and return the Future
	}

	/**
	 * Get the current number of bookings awaiting a driver.
	 *
	 * @return The number of bookings waiting for a driver.
	 */
	public int getBookingsAwaitingDriver() {
		return bookingsAwaitingDriver.get(); // Return the current count of bookings waiting for drivers
	}

	/**
	 * Shut down the dispatch service, stopping all regions and shutting down the thread pool.
	 */
	public void shutdown() {
		for (NuberRegion region : regions.values()) {
			region.shutdown(); // Shut down all regions
		}
		executorService.shutdown(); // Shut down the thread pool
		try {
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) { // Wait for the thread pool to terminate
				executorService.shutdownNow(); // Forcefully shut down the thread pool if not terminated in time
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow(); // Handle interruption and forcefully shut down the thread pool
			Thread.currentThread().interrupt(); // Re-set the thread's interrupted status
		}
	}
}
