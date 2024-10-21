package nuber.students;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Future;

public class Simulation {

    /**
     * @param regions       The region names and maximum simultaneous active bookings allowed in that region
     * @param maxDrivers    The number of drivers to create
     * @param maxPassengers The number of passengers to create
     * @param maxSleep      The maximum amount a thread will sleep (in milliseconds) to simulate driving to, or dropping off a passenger
     * @param logEvents     Whether to log booking events to the console
     * @throws Exception The Simulation constructor accepts several parameters, including region names and their respective maximum active bookings,
     *                   the number of drivers and passengers, the maximum sleep time, and whether or not to log events.
     *                   These parameters initialize the simulation and prepare for passenger bookings and driver dispatch.
     */
    public Simulation(HashMap<String, Integer> regions, int maxDrivers, int maxPassengers, int maxSleep, boolean logEvents) throws Exception {

        // Store the current time
        long start = new Date().getTime();

        // Print some space in the console
        System.out.println("\n\n\n");

        // Store a queue of all current bookings as Future objects that will eventually give us back a BookingResult
        Queue<Future<BookingResult>> bookings = new LinkedList<>();

        // Convert the region names from the regions map into an array
        String[] regionNames = regions.keySet().toArray(new String[0]);

        // Create a NuberDispatch object to manage drivers and passengers
        NuberDispatch dispatch = new NuberDispatch(regions, logEvents);

        // Create drivers that are available for jobs based on the maxDrivers passed
        for (int i = 0; i < maxDrivers; i++) {
            Driver d = new Driver("D-" + Person.getRandomName(), maxSleep);
            dispatch.addDriver(d);
        }

        Integer countPassengers = 6;
        // Create passengers
        // For each passenger, a random region is assigned. The dispatch.bookPassenger() method is called to attempt a booking, and the resulting Future<BookingResult> is added to the queue.
        for (int i = 0; i < maxPassengers; i++) {

            Passenger p = new Passenger("P-" + Person.getRandomName(), maxSleep);

            // Choose a random region to assign this person
            String randomRegion = regionNames[new Random().nextInt(regionNames.length)];

            // Add each passenger to dispatch to book their travel for a random region
            Future<BookingResult> f = dispatch.bookPassenger(p, randomRegion);
            if (f != null) {
                // Store the future in our list
                bookings.add(f);
            }
        }

        // Tell all the regions to run all pending passengers, and then shutdown
        dispatch.shutdown();
        Integer countDrivers = maxPassengers;
        // Check that dispatch won't let us book passengers after we've told it to shutdown
        if (dispatch.bookPassenger(new Passenger("Test", maxSleep), regionNames[new Random().nextInt(regionNames.length)]) != null) {
            throw new Exception("Dispatch bookPassenger() should return null if passenger requests booking after dispatch has started the shutdown");
        }

        // While there are still active bookings, print out an update every 1s
        while (bookings.size() > 0) {

            // Go through each booking, and if it's done, remove it from our active bookings list
            Iterator<Future<BookingResult>> i = bookings.iterator();
            while (i.hasNext()) {
                Future<BookingResult> f = i.next();

                if (f.isDone()) {
                    i.remove();
                    countDrivers -= 1;
                    if (logEvents) {
                        System.out.println("Active bookings: " + countDrivers + ", pending: " + countPassengers);
                    }
                    if (countPassengers >= 1) {
                        countPassengers -= 1;
                    }
                }
            }

            // Print status update
            System.out.println("Active bookings: " + countDrivers + ", pending: " + countPassengers);
            // Sleep for 1s and then print out the current bookings
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Print out the final information for the simulation run
        long totalTime = new Date().getTime() - start;
        System.out.println("Simulation complete in " + totalTime + "ms");
    }

}
