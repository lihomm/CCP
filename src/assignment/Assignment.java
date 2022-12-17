package assignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author kohli
 */
public class Assignment {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        List<String> fuel = new ArrayList<>();
        fuel.add("Full");
        fuel.add("Medium");
        fuel.add("Low");
        
        Random rand = new Random();
        
        BlockingQueue<gate> gates = new ArrayBlockingQueue<gate>(2);
		
        // Create a thread pool for the aircraft objects.
        // The size is fixed to 3 as there cannot be more than 3 aircrafts executing at the same time as it is limited to the number of runway objects.
        ExecutorService apool  = Executors.newFixedThreadPool(2);

        // Generate 3 runway objects and add it to the runway queue.
        for(int i = 0; i<2;i++){
            gates.add(new gate(i));
        }

        for(int i = 1; i <= 10; i++) {
            try {
                    // Wait between 0 to 5 seconds to generate a new aircraft.
                    Thread.sleep(rand.nextInt(3000));
                    // Add generated aircraft into queue.
                    // Pass a random boolean to decide if the aircraft wants to land or depart.
                    apool.submit(new plane(i, fuel.get(rand.nextInt(list.size())), 500, gates));
            } catch (InterruptedException e) {
                    e.printStackTrace();
            }
        }		
    }   
}
