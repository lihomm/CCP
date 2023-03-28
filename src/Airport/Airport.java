package Airport;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kohli
 */

// add busy scenario

public class Airport {
    
    private final int PLANE_SEMAPHORE = 1;        
    private static final int NUM_GATES = 2;
    private static final int PLANES_COUNT = 6;
    private static final int MIN_PASSENGERS = 10;       // minimum passengers
    private static final int MAX_PASSENGERS = 50;       // maximum passengers
    private static final int MIN_FUEL = 10;              // minimum fuel percentage
    private static final int MAX_FUEL = 80;             // maximum fuel percentage
    private final Semaphore runwaySemaphore = new Semaphore(PLANE_SEMAPHORE, true);
    private final Object lock = new Object();
    private LinkedBlockingDeque<Plane> queue;
       
    AtomicInteger maximumWaitingTime = new AtomicInteger(Integer.MIN_VALUE);         
    AtomicInteger minimumWaitingTime = new AtomicInteger(Integer.MAX_VALUE);
    AtomicInteger totalWaitingTime = new AtomicInteger(0);
    AtomicInteger planeCount = new AtomicInteger(0);
    AtomicInteger passengerCount = new AtomicInteger(0);

    Gate[] gates; 
    Plane[] planes;
    Runway[] runway;
    RefuellingTruck[] truck;
    
    CountDownLatch airportLatch = new CountDownLatch(6);
    
    Map<Integer, Integer> myMap = new HashMap<>();
    
    public Airport() throws InterruptedException {            
        Random rand = new Random();        
            
        queue = new LinkedBlockingDeque<Plane>();;
        
        // Constructing Airport
        // Runway, Gate and Refueling Truck are shared resources
        // Storing objects in array for easy access later
        runway = new Runway[1];
        for (int i = 0; i < runway.length; i++) {
            runway[i] = new Runway(i+1, true, PLANE_SEMAPHORE, this);
        }
        
        ExecutorService truck_pool  = Executors.newFixedThreadPool(1);
        truck = new RefuellingTruck[1];
        for (int i = 0; i < truck.length; i++) {
            truck[i] = new RefuellingTruck(this);
            truck_pool.submit(truck[i]);
        }
        
        gates = new Gate[2];
        for (int i = 0; i < gates.length; i++) {
            gates[i] = new Gate(i+1, this);
        }
           
        // Create a thread pool for the aircraft objects.
        // The size is fixed to 2 as there cannot be more than 2 planes executing at the same time as it is limited to the number of gate objects.
        ExecutorService plane_pool  = Executors.newFixedThreadPool(PLANES_COUNT);

        planes = new Plane[6];
        // Create 6 planes for simulation
        int fuelPercentage = 0; 
        for(int i = 1; i <= PLANES_COUNT; i++) {
            // Add generated aircraft into queue.
            int passengersNumber = MIN_PASSENGERS + rand.nextInt(MAX_PASSENGERS-MIN_PASSENGERS) + 1;  //added 1 to the fuel and pasengers so it is inclusive of upper bound
            passengerCount.getAndAdd(passengersNumber);
            if(i==5){
                fuelPercentage = MIN_FUEL;
            }
            else if(i==6 || i < 5){
                fuelPercentage = 25 + rand.nextInt(MAX_FUEL-MIN_FUEL) + 1;
            }
            planes[i-1] = new Plane(i, fuelPercentage, passengersNumber, this, runway[0], truck[0], gates);       
            System.out.printf("ID : %d\t\t Fuel Percentage : %d%%\t\tPassenger Count : %d\n", planes[i-1].getID(), planes[i-1].getFuel(), planes[i-1].getPassengers());
            plane_pool.submit(planes[i-1]);
            try {
                // Wait between 0 to 3 seconds to generate a new aircraft.
                Thread.sleep(rand.nextInt(3000));
            } catch (InterruptedException ex) {
                Logger.getLogger(Airport.class.getName()).log(Level.SEVERE, null, ex);
            }       
        }               
        airportLatch.await(); 
        plane_pool.shutdown();
        truck_pool.shutdownNow();   
        try {
            // wait for all tasks to complete
            plane_pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            truck_pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // handle the exception
        }
        System.out.println("Sanity Check");
        // check all gates are empty
        System.out.println("Gate Status\t\t\t (true = empty, false = occupied)\n");   
        for (Gate gate : gates) {
            System.out.println("Gate "+ gate.getID()+": "+ gate.isGateAvailability());           
        }
        
        System.out.println("\nStatistics");           
        // print out statistics
        //max, average, min waiting time
        // Iterate through the TreeMap in ID order
        for (Map.Entry<Integer, Integer> entry : myMap.entrySet()) {
            int id = entry.getKey();
            int value = entry.getValue();
            totalWaitingTime.getAndAdd(value);
            if(value < minimumWaitingTime.get()){
                minimumWaitingTime.set(value);
            }
            else if(value > maximumWaitingTime.get()){
                maximumWaitingTime.set(value);
            }
            System.out.println("ID: " + id + "\t\t\tElapsed waiting time for runway: " + value+" ms");
        }
        // min
        System.out.println("\nMinimum waiting time: "+minimumWaitingTime.get()+" ms");
        
        // max
        System.out.println("Maximum waiting time: "+maximumWaitingTime.get()+" ms");
        
        // average
        System.out.println("Average waiting time: "+ totalWaitingTime.get()/PLANES_COUNT+" ms\n");
        
        //num of planes served        
        System.out.println("Total plane/s served: "+PLANES_COUNT);
      
        //passengers boarded
        System.out.println("Total passenger/s served: "+passengerCount+"\n");       
        
        // to show running threads
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        for (Thread t : threads.keySet()) {
            System.out.println("Thread: " + t.getName() + " | State: " + t.getState());
        } 
        System.out.println("\n");
        return;
    }   
      
    // This method is called by the plane threads to request access to the runway.
    public void requestRunway(Plane plane, Runway runway, int action) {
        try {
            if(action == 0){
                // Add the plane thread to the queue.
                if(plane.getFuel() <= 25){
                    System.out.println(Thread.currentThread().getName()+ ": Plane "+ plane.getID() +" has low fuel at " + plane.getFuel() + "%. This plane has been added to the priority, demanding emergency landing soon.\n");
                    queue.putFirst(plane);
                }
                else{
                    queue.put(plane);
                    System.out.println(Thread.currentThread().getName()+ ": Plane " + plane.getID() + " is added to the runway queue awaiting for approval.\n");
                }      
                // Wait for the runway to become available.
                synchronized (plane) {
                    while (!queue.peek().equals(plane) || planeCount.get() >= 2 || runway.runwaySemaphore.availablePermits() == 0) {
                        plane.wait();
                    }
                }
            }       
            else if(action == 1){
                // Add the plane thread to the queue.
                synchronized (lock) {
                    queue.putFirst(plane);
                }
            }
            // Acquire the runway.
            synchronized (lock) {
                queue.remove(plane);

                // If the plane is taking off, decrement the plane count.
                if (action == 1) {
                    // Perform takeoff operation
                    runway.takeoff(plane);
                    planeCount.getAndDecrement();
                }
                // If there is space on the ground, let the plane land.
                else if (action == 0 && planeCount.get() < 2) {                  
                    // Perform landing operation
                    runway.landing(plane);
                    planeCount.getAndIncrement();
                }
            }
            if(queue.isEmpty()){
                return;
            }                         
            // Notify the next plane in the queue.
            synchronized (queue) {
                Plane nextPlane = queue.peek();               
                if (nextPlane != null) {
                    synchronized (nextPlane) {
                        nextPlane.notify();
                    }
                }
            }
        } catch (InterruptedException e) {
            // Handle the interrupted exception.
            e.printStackTrace();
        }
    }    
      
    public LinkedBlockingDeque<Plane> getQueue() {
        return queue;
    }        
}
