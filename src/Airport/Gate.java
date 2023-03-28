/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Airport;

import java.util.concurrent.locks.ReentrantLock;
/**
 *
 * @author kohli
 */
public class Gate {

    private int ID;
    private boolean gateAvailability;
    private boolean planeDocked;

  
    ReentrantLock gateLock;
    Plane plane;
    Airport airport;

    
    public Gate(int ID, Airport airport){
        this.ID = ID;     
        this.airport = airport;       
        this.gateAvailability = true;        
        this.planeDocked = false; 
    }
    
    
    public int getID(){
        return ID;
    }
    
    
    public boolean isGateAvailability() {
        return gateAvailability;
    }

    public void setGateAvailability(boolean gateAvailability) {
        this.gateAvailability = gateAvailability;
    }

    public boolean isPlaneDocked() {
        return planeDocked;
    }

    public void setPlaneDocked(boolean planeDocked) {
        this.planeDocked = planeDocked;
    }
}

