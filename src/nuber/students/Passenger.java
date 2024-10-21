package nuber.students; // Define the package for this class

/**
 * The Passenger class represents a passenger, inheriting from the Person class.
 */
public class Passenger extends Person {

	/**
	 * Constructor to create a new instance of Passenger.
	 *
	 * @param name     The name of the passenger.
	 * @param maxSleep The maximum sleep time (travel time) for the passenger.
	 */
	public Passenger(String name, int maxSleep) {
		super(name, maxSleep); // Call the parent class Person's constructor
	}

	/**
	 * Get the passenger's travel time.
	 *
	 * @return The passenger's travel time, a random integer between 0 and maxSleep.
	 */
	public int getTravelTime() {
		return (int) (Math.random() * maxSleep); // Generate and return a random travel time
	}
}
