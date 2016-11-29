import java.util.concurrent.locks.*;
import java.util.Random;

public class Philosopher extends Thread {
    private static int WAITING = 0, EATING = 1, THINKING = 2;
    private Lock lock;
    private Condition philosophers [];
    private int NUM_PHILS;
    private int id;
    private final int TURNS = 5;

    private int maxSleepTime = 100;
    private Random rand = new Random();;
    private long execTime = 0, waitTime = 0;
    private long startTime = 0, endTime = 0;
    private int stallCtr = 0;

    // constructor
    public Philosopher (Lock l, Condition p[], int num) {
        lock = l;
        philosophers = p;
        NUM_PHILS = num;
    }

    // as soon as thread starts, assign its id, and takeSticks()/putSticks() for amount of TURNS
    public void run () {
        id = ThreadID.get();
        startTime = System.nanoTime();

        for (int k = 0; k < TURNS; k++) {
            try {
                sleep(randomTime());
            } catch (Exception ex) { /* lazy */ }
            takeSticks(id);
            output();

            try {
                sleep(randomTime());
            } catch (Exception ex) { }
            putSticks(id);
        }

        endTime = System.nanoTime();
        execTime = endTime - startTime;
        // System.out.println(execTime + " " + id);
    }

    // if the left and right chopstick is free for the current philosopher, they can enter the EATING state
    // if one chopstick is unavailable, the philosopher waits to be notified of when the chopstick will be available
    public void takeSticks (int id) {
        lock.lock();
        try {
            Boolean state_status = (Main.states[leftof(id)] != EATING && Main.states[rightof(id)] != EATING);
            if (state_status && (Main.q.peek() == null)) {
                Main.states[id] = EATING;
            } else if (state_status && (Main.q.peek() == id)) {
                Main.q.remove();
                Main.states[id] = EATING;
            } else {
                Main.q.add(id);
                Main.states[id] = WAITING;
                //philosopher waits until signaled or interrupted
                philosophers[id].await();
                takeSticks(id);
            }
        } catch (Exception e) { }
        finally {
            lock.unlock();
        }
    }

    // print the states of the philosophers
    public void output() {
        lock.lock();
        
        System.out.println("id: " + id);
        for (int k = 0; k < Main.states.length; k++) {
            System.out.print(Main.states[k] + ",");
        }

        lock.unlock();

        System.out.println();
        System.out.println();
    }

    // philosopher is finished eating, notify the left & right philosopher if they are waiting and have a 2nd utensil available
    public void putSticks (int id) {
        lock.lock();
        try {
            Main.states[id] = THINKING;
            //if the left philosopher is waiting to eat and the left of that philosopher is not eating,
            //signal that person that the stick is now available to use and begin eating
            if (Main.q.peek() != null) {
                int head = Main.q.remove();

                if ((Main.states[leftof(head)] != EATING && Main.states[rightof(head)] != EATING)) {
                    philosophers[head].signal();
                    Main.states[head] = EATING;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private int leftof (int id) { // clockwise
        int retval = id - 1;
        if (retval < 0) // not valid id, cycle through again
        retval = NUM_PHILS - 1;
        return retval;
    }

    private int rightof (int id) {
        int retval = id + 1;
        if (retval == NUM_PHILS) // not valid id, recycle
        retval = 0;
        return retval;
    }

    //return a random int to be used for the sleep function
    private int randomTime() {
        return rand.nextInt(maxSleepTime);
    }
}
