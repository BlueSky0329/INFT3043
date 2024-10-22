package nuber.students; // Define the package for this class

import java.util.*; // Import the collections framework
import java.util.concurrent.*; // Import the concurrent utilities
import java.util.concurrent.atomic.AtomicInteger; // Import AtomicInteger for atomic counting

public class NuberRegion { // Define the NuberRegion class

	private final NuberDispatch dispatch; // Reference to NuberDispatch for logging events
	private final String regionName; // Name of the region
	private final int maxSimultaneousJobs; // Maximum number of simultaneous bookings
	private final BlockingQueue<Booking> bookingsQueue = new LinkedBlockingQueue<>(); // Blocking queue to store bookings
	private final ExecutorService executorService; // Thread pool to handle bookings
	private final AtomicInteger activeBookings = new AtomicInteger(0); // Current number of active bookings
	private volatile boolean isShutdown = false; // Flag to indicate if the region is shut down

	/**
	 * Constructor for NuberRegion.
	 *
	 * @param dispatch          Reference to the dispatch service.
	 * @param regionName        Name of the region.
	 * @param maxSimultaneousJobs  Maximum number of simultaneous bookings.
	 */
	public NuberRegion(NuberDispatch dispatch, String regionName, int maxSimultaneousJobs) {
		this.dispatch = dispatch; // Initialize dispatch service
		this.regionName = regionName; // Initialize region name
		this.maxSimultaneousJobs = maxSimultaneousJobs; // Initialize the maximum number of bookings
		this.executorService = Executors.newFixedThreadPool(maxSimultaneousJobs); // Create a fixed-size thread pool

		// Start a thread to process bookings in the queue
		Thread bookingProcessor = new Thread(() -> {
			while (!isShutdown) { // Until the region is shut down
				try {
					Booking booking = bookingsQueue.take(); // Take a booking from the queue
					if (activeBookings.incrementAndGet() <= maxSimultaneousJobs) { // Check if the active bookings exceed the limit
						executorService.submit(() -> { // Submit a task to handle the booking
							try {
								BookingResult result = booking.call(); // Execute the booking
								dispatch.logEvent(booking, "Booking completed: " + result); // Log the booking completion event
							} catch (Exception e) {
								dispatch.logEvent(booking, "Error in booking: " + e.getMessage()); // Log the error event
							} finally {
								activeBookings.decrementAndGet(); // Decrement active bookings after completion
							}
						});
					} else {
						// If active bookings exceed the limit, put the booking back in the queue
						bookingsQueue.put(booking); // Put the booking back in the queue
						activeBookings.decrementAndGet(); // Restore the active bookings count
					}
				} catch (InterruptedException e) { // Handle interruptions
					Thread.currentThread().interrupt(); // Restore the interrupted status
					break; // Exit the loop
				}
			}
		});
		bookingProcessor.setDaemon(true); // Set as a daemon thread
		bookingProcessor.start(); // Start the processing thread
	}

	/**
	 * Book a passenger for this region.
	 *
	 * @param waitingPassenger The waiting passenger.
	 * @return A Future representing the booking result.
	 */
	public Future<BookingResult> bookPassenger(Passenger waitingPassenger) {
		if (isShutdown) { // Check if the region is shut down
			dispatch.logEvent(null, "Booking rejected: Region " + regionName + " is shutting down."); // Log the booking rejection event
			return null; // Return null
		}

		Booking booking = new Booking(dispatch, waitingPassenger); // Create a new booking object
		CompletableFuture<BookingResult> future = new CompletableFuture<>(); // Create a CompletableFuture object

		// Add the booking to the queue
		bookingsQueue.add(booking);

		// Submit a task to handle the booking and return a Future
		executorService.submit(() -> {
			try {
				BookingResult result = booking.call(); // Execute the booking
				future.complete(result); // Complete the Future
			} catch (Exception e) {
				future.completeExceptionally(e); // Complete with an exception if an error occurs
			}
		});

		return future; // Return the Future object
	}

	/**
	 * Shut down the region and release resources.
	 */
	public void shutdown() {
		isShutdown = true; // Set the shutdown flag
		executorService.shutdown(); // Shut down the thread pool
		try {
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) { // Wait for the thread pool to terminate
				executorService.shutdownNow(); // Forcefully shut down the thread pool
			}
		} catch (InterruptedException e) { // Handle interruptions
			executorService.shutdownNow(); // Forcefully shut down the thread pool
			Thread.currentThread().interrupt(); // Restore the interrupted status
		}
	}

	/**
	 * Get the shutdown status of the region.
	 *
	 * @return true if the region is shut down, otherwise false.
	 */
	public boolean isShutdown() {
		return isShutdown; // Return the shutdown status
	}
}
