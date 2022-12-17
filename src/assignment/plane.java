package assignment;

import java.util.concurrent.BlockingQueue;


/**
 *
 * @author kohli
 */
public class plane implements Runnable {
    private int ID;
    private String fuel;
    private int passengers;
    public plane(int ID, String fuel, int passengers, BlockingQueue<gate> gates){
        this.ID = ID;
        this.fuel = fuel;
        this.passengers = passengers;
        
        System.out.println(java.time.LocalTime.now() + " A plane with the ID of " + ID + " has been created.");
    }
    
    @Override
    public void run(){
    
    }
    
    synchronized public void landing_takeoff(){

    }
        
    public void disembark(){

    }

    public void embark(){

    }

    public void supplies_refill(){

    }

    public void cleaning(){

    }

    synchronized public void refueling(){

    }
}
