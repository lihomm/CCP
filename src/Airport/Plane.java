package Airport;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Deque;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kohli
 */
public class Plane implements Runnable {
    private int ID;
    private int fuel;
    private int passengers_count;
    private boolean onGround = false;
    private boolean isRefueling = false;
    private boolean isRefueled = false;
    
    CountDownLatch arrivalLatch;
    CountDownLatch disembarkLatch;
    CountDownLatch embarkLatch;
    
    Airport airport;
    Runway runway;
    RefuellingTruck truck;
    Gate[] gates;
    Gate gate;
    Passenger[] passengers;
    Semaphore runwaySem, passengerSem;    

    ExecutorService passenger_pool  = Executors.newFixedThreadPool(50);
    
    public Plane(int ID, int fuel, int passengers_count, Airport airport, Runway runway, RefuellingTruck truck, Gate[] gates){
        this.ID = ID;
        this.fuel = fuel;
        this.passengers_count = passengers_count;
        this.airport = airport;
        this.runway = runway;
        this.truck = truck;
        this.gates = gates;
        this.disembarkLatch = new CountDownLatch(passengers_count);
        this.embarkLatch = new CountDownLatch(passengers_count);
        this.passengerSem = new Semaphore(passengers_count, true);           //Creates a Semaphore with the given number of permits and the given fairness setting.
        passengers = new Passenger[passengers_count];
        for (int i = 1; i <= passengers.length; i++) {                  // create passengers in the plane
            passengers[i-1] = new Passenger(i, airport, this, this.disembarkLatch, this.embarkLatch);
        }
        System.out.println("\n" + LocalTime.now() + " A plane with the ID of " + ID + " has been created.");
    }

    @Override
    public void run(){       
        Random rand = new Random();
        long duration = 3;                 

        System.out.println(Thread.currentThread().getName()+ ": " + LocalTime.now() + " Plane " + ID + " is entering the airfield.");   
                
        Instant start = Instant.now(); // Get the start time
        airport.requestRunway(this, runway, 0);
        Instant end = Instant.now(); // Get the end time
        
        Duration elapsedTime = Duration.between(start, end); // Calculate the elapsed time
        long elapsedMillis = elapsedTime.toMillis();
        int intValue = (int) elapsedMillis;
        
        airport.myMap.put(ID, intValue);                  // append the plane ID and time pair to hashmap
        System.out.println("Plane " + ID + " has waited for "+intValue+" ms to make a landing." );

        // gate assignment
        for (Gate gate : gates) {
            if (gate.isGateAvailability() == true) {
                this.gate = gate;
                gate.setGateAvailability(false);
                break;
            }
        }
        
        // coasting to gate
        System.out.println("Plane " + ID + " has been assigned to Gate " + gate.getID());
        System.out.println("Plane " + ID + " is now coasting to the gate.");
        try {
            TimeUnit.SECONDS.sleep(duration);
        } catch (InterruptedException ex) {
            Logger.getLogger(Plane.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Plane " + ID + " arrived at the gate.");
        System.out.println("Plane " + ID + " docking to gate.");
        
        // docking to gate
        try {
            TimeUnit.SECONDS.sleep(duration);
        } catch (InterruptedException ex) {
            Logger.getLogger(Plane.class.getName()).log(Level.SEVERE, null, ex);
        }       
        System.out.println("Plane " + ID + " successfully docked to Gate " + gate.getID());
        gate.setPlaneDocked(true);
        System.out.println("Plane " + ID + " will now start disembarking passengers from plane.");
        
        for (int i = 1; i <= passengers.length; i++) {          // start all passengers threads in the plane
            passenger_pool.submit(passengers[i-1]);             // start disembarking process as threads run 
        }   
        System.out.println("Plane " + ID + " is refilling its supplies and getting cleaned.");
        try {
            TimeUnit.SECONDS.sleep(duration);
        } catch (InterruptedException ex) {
            Logger.getLogger(Plane.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Plane " + ID + " is requesting refueling.");
        try {
            // Request refueling from the refueling truck
            truck.refuelPlane(this);

            // Wait for the refueling process to complete
            while (fuel<100) {
                System.out.println("Plane " + ID + " is waiting to be refueled...");
                Thread.sleep(1000);
            }

            // Signal that the refueling process is complete
            isRefueled = false;
            System.out.println("Plane " + ID + " has been refueled.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         
         
        // undocking from gate
        try {
            System.out.println("Plane " + ID + " is now undocking from Gate "+ gate.getID());
            gate.setGateAvailability(true);
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Plane.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // takeoffs
        System.out.println("Plane " + ID + " is now coasting to the runway.");
        try {
            TimeUnit.SECONDS.sleep(duration);
        } catch (InterruptedException ex) {
            Logger.getLogger(Plane.class.getName()).log(Level.SEVERE, null, ex);
        }       
        airport.requestRunway(this, runway, 1);
        passenger_pool.shutdown();    
        try {
            // wait for all tasks to complete
            passenger_pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // handle the exception
        }
        airport.airportLatch.countDown();
    }
      
    public synchronized void requestRefuel() throws InterruptedException {
        while (isRefueling) {
            System.out.println("Plane " + ID + " is waiting for refueling truck to finish refueling.");
            wait();
        }
        System.out.println("Plane " + ID + " is requesting refueling.");
        truck.refuelPlane(this);

        while (isRefueling) {
            System.out.println("Plane " + ID + " is waiting for refueling to complete.");
            wait();
        }
        System.out.println("Plane " + ID + " has finished refueling. Current fuel level: " + fuel + "%");
    }
    
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getFuel() {
        return fuel;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    public int getPassengers() {
        return passengers_count;
    }

    public void setPassengers(int passengers_count) {
        this.passengers_count = passengers_count;
    }
    
    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
    
    public void setRefuelingTruck(RefuellingTruck truck) {
        this.truck = truck;
    }

    public void setRefueling(boolean isRefueling) {
        this.isRefueling = isRefueling;
    }
    
    public boolean isRefueling() {
        return isRefueling;
    }
}
