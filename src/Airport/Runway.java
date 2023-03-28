 /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Airport;

import java.time.LocalTime;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author kohli
 */
public class Runway {

    private int ID;
    private boolean runwayAvailability;

    Semaphore runwaySemaphore;
    
    Airport airport;
    
    public Runway(int ID, boolean runwayAvailability, int runwayCount, Airport airport){
        this.ID = ID;
        this.runwayAvailability = runwayAvailability;
        this.runwaySemaphore = new Semaphore(runwayCount, true); 
        this.airport = airport;

        System.out.println("Runway has been created.");
    }
  
    synchronized public void landing(Plane plane) throws InterruptedException{
        runwaySemaphore.acquire();
        Random rand = new Random();
        int low = 1;
        int high = 5;
        int duration = rand.nextInt(high-low) + low;
        System.out.println("\n" + Thread.currentThread().getName()+ ": " + LocalTime.now() + " Plane "+ plane.getID() +" is landing at the runway");
        Thread.sleep(rand.nextInt(duration*1000));
        TimeUnit.SECONDS.sleep(duration);
        System.out.println("\n" + Thread.currentThread().getName()+ ": " + LocalTime.now() + " Plane "+ plane.getID()+" successfully made landing at the runway in " + duration + " seconds.");
        plane.setOnGround(true);
        runwaySemaphore.release();
    }
    
    synchronized public void takeoff(Plane plane) throws InterruptedException{
        runwaySemaphore.acquire();
        Random rand = new Random();
        int low = 1;
        int high = 5;
        int duration = rand.nextInt(high-low) + low;
        System.out.println("\n" + Thread.currentThread().getName()+ ": " + LocalTime.now() + " Plane "+ plane.getID() +" is taking off at the runway");
        TimeUnit.SECONDS.sleep(duration);
        System.out.println("\n" + Thread.currentThread().getName()+ ": " + LocalTime.now() + " Plane "+ plane.getID()+" successfully took off from the runway in " + duration + " seconds.\n");
        plane.setOnGround(false);
        runwaySemaphore.release();
    }
    
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public boolean isRunwayAvailability() {
        return runwayAvailability;
    }

    public void setRunwayAvailability(boolean runwayAvailability) {
        this.runwayAvailability = runwayAvailability;
    }
    
}
