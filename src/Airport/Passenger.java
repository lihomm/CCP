/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Airport;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class Passenger implements Runnable{
    private int ID;
    private final CountDownLatch disembarkLatch;
    private final CountDownLatch embarkLatch;
    
    Airport airportLanded;
    Plane planeBoarded;
    
    public Passenger(int ID, Airport airportLanded, Plane planeBoarded, CountDownLatch disembarkLatch, CountDownLatch embarkLatch){
        this.ID = ID;
        this.airportLanded = airportLanded;
        this.planeBoarded = planeBoarded;
        this.disembarkLatch = disembarkLatch;
        this.embarkLatch = embarkLatch;
    }

    public void boarding(){
        System.out.println(Thread.currentThread().getName()+ ": Passenger " + ID + " is boarding Plane " + planeBoarded.getID() + " via Gate " + planeBoarded.gate.getID());
    }
    
    public void exiting(){
       System.out.println(Thread.currentThread().getName()+ ": Passenger " + ID + " is exiting via Gate " + planeBoarded.gate.getID()+ " from Plane "+ planeBoarded.getID());
    }
        
    @Override
    public void run(){
        try{

            Thread.sleep((int) (Math.random() * 1000));
            exiting();
            planeBoarded.disembarkLatch.countDown();
            planeBoarded.disembarkLatch.await();
            Thread.sleep((int) (Math.random() * 1000));
            boarding();
            planeBoarded.embarkLatch.countDown();

        } catch (InterruptedException ex) {
            Logger.getLogger(Passenger.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
