/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Airport;
  
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RefuellingTruck implements Runnable {
    private ReentrantLock lock;
    private Condition planeWaiting;
    private Airport airport;

    private boolean isRefueling = false;
    private Plane currentPlane = null;
    private Queue<Plane> planeQueue = new LinkedList<>();
    private volatile boolean shouldRun = true; // Flag indicating whether the thread should continue running

    public RefuellingTruck(Airport airport) {
        this.lock = new ReentrantLock(true);
        this.planeWaiting = lock.newCondition();
        this.airport = airport;
    }

    public void run() {
        try {
            // give buffer time for planes to be added to queue
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(RefuellingTruck.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (shouldRun) {
            lock.lock();
            try {
                // Check if there are any waiting planes
                if (planeQueue.peek()== null && currentPlane == null) {
                    // If there are no waiting planes, wait for one to arrive
                    System.out.println(Thread.currentThread().getName()+ ": Refueling truck is waiting for a plane...");
                    planeWaiting.await();
                }

                // Check if there is a plane currently being refueled
                if (currentPlane != null) {
                    // Refuel the selected plane
                    System.out.println(Thread.currentThread().getName() + ": Refueling truck is refueling Plane " + currentPlane.getID());
                    isRefueling = true;
                    
                    Thread.sleep((100-currentPlane.getFuel())/20);
                    System.out.println(Thread.currentThread().getName() + ": Refueling truck has finished refueling Plane " + currentPlane.getID());
                    currentPlane.setFuel(100);
                    currentPlane.setRefueling(false);
                    currentPlane = null;
                    isRefueling = false;
                } else {
                    // There are planes waiting in the queue, select the first one
                    currentPlane = planeQueue.remove();
                }
            } catch (InterruptedException e) {
                break;
            } finally {
                lock.unlock();
            }
        }
    }

    public void refuelPlane(Plane plane) throws InterruptedException {
        lock.lock();
        try {
            // Check if the truck is already refueling a plane
            if (isRefueling) {
                System.out.println("Refueling truck is busy refueling Plane " + currentPlane.getID());
                System.out.println("Plane " + plane.getID() + " is waiting for the refueling truck...");
                planeQueue.add(plane);
                planeWaiting.await();
            }
            // Refuel the plane
            currentPlane = plane;
            plane.setRefueling(true);
            planeWaiting.signal();
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isRefueling() {
        return isRefueling;
    }

    public void setIsRefueling(boolean isRefueling) {
        this.isRefueling = isRefueling;
    }
    
    public void setState(boolean shouldRun) {
        this.shouldRun = shouldRun;
        synchronized (lock){
            planeWaiting.signal();
        }
    }
    
    public boolean isShouldRun() {
        return shouldRun;
    }
            
    public Condition getPlaneWaiting() {
        return planeWaiting;
    }

    public void setPlaneWaiting(Condition planeWaiting) {
        this.planeWaiting = planeWaiting;
    }  
}