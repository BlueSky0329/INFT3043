package nuber.students; // Define the package for this class

public class BookingResult { // Define the BookingResult class

	public int jobID; // Declare an integer variable jobID to store the job ID
	public Passenger passenger; // Declare a Passenger variable passenger to store passenger information
	public Driver driver; // Declare a Driver variable driver to store driver information
	public long tripDuration; // Declare a long variable tripDuration to store the trip duration (in milliseconds)

	// Constructor to initialize the properties of the BookingResult object
	public BookingResult(int jobID, Passenger passenger, Driver driver, long tripDuration) {
		this.jobID = jobID; // Assign the passed jobID to the member variable jobID
		this.passenger = passenger; // Assign the passed passenger to the member variable passenger
		this.driver = driver; // Assign the passed driver to the member variable driver
		this.tripDuration = tripDuration; // Assign the passed tripDuration to the member variable tripDuration
	}
}
