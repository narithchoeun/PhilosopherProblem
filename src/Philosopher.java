import java.util.concurrent.locks.*;
import java.util.Random;

public class Philosopher extends Thread {
    private static int WAITING = 0, EATING = 1, THINKING = 2;
    private Lock lock;
    private Condition philosophers [];
    private int NUM_PHILS;
    private int id;
    private final int TURNS = 1;

    private int maxSleepTime = 10;
    private Random rand = new Random();
    private long execTime, waitTime = 0;
    private long startTime = 0, endTime = 0;
    private int stallCtr = 0, eatenCtr;

    // constructor
    public Philosopher (Lock l, Condition p[], int num, int eatenCtr, long execTime) {
        lock = l;
        philosophers = p;
        NUM_PHILS = num;
        this.eatenCtr = eatenCtr;
        this.execTime = execTime;
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

            try {
                sleep(randomTime());
            } catch (Exception ex) { }
            putSticks(id);
            // output();
        }

        endTime = System.nanoTime();
        this.execTime = endTime - startTime;
        printStats();
    }

    // if the left and right chopstick is free for the current philosopher, they can enter the EATING state
    // if one chopstick is unavailable, the philosopher waits to be notified of when the chopstick will be available
    public void takeSticks (int id) {
        lock.lock();
        // System.out.println(id + " wants to eat");
        try {
            Boolean state_status = (Main.states[leftof(id)] != EATING && Main.states[rightof(id)] != EATING);
            if (state_status && (Main.q.peek() == null)) {
                Main.states[id] = EATING;
                eatenCtr++;
                // System.out.println(id + " is eating.");
            } else if (state_status && (Main.q.peek() == id)) {
                Main.q.remove();
                Main.states[id] = EATING;
                eatenCtr++;
                // System.out.println(id + " is eating.");
            } else if (state_status) {
                Main.states[id] = EATING;
                eatenCtr++;
                // System.out.println(id + " is eating.");
            } else {
                Main.q.add(id);
                Main.states[id] = WAITING;
                // System.out.println(id + " is waiting.");
                long t0 = System.nanoTime();
                philosophers[id].await();
                long t1 = System.nanoTime();
                waitTime = t1 - t0;
                takeSticks(id);
            }
        } catch (Exception e) { }
        finally {
            lock.unlock();
        }
    }

    // philosopher is finished eating, notify the left & right philosopher if they are waiting and have a 2nd utensil available
    public void putSticks (int id) {
        lock.lock();
        try {
            Main.states[id] = THINKING;
            // System.out.println(id + " is thinking.");
            //if the left philosopher is waiting to eat and the left of that philosopher is not eating,
            //signal that person that the stick is now available to use and begin eating
            if (Main.q.peek() == null) {
                if ((Main.states[leftof(id)]==WAITING && Main.states[leftof(leftof(id))]!=EATING)) {
                    philosophers[leftof(id)].signal();
                    // System.out.println("Signals " + leftof(id));
                    Main.states[leftof(id)] = EATING;
                }

                //same for right philosopher
                if ((Main.states[rightof(id)] == WAITING && Main.states[rightof(rightof(id))] != EATING)) {
                    philosophers[rightof(id)].signal();
                    // System.out.println("Signals " + rightof(id));
                    Main.states[rightof(id)] = EATING;
                }
            } else {
                int head = Main.q.remove();
                philosophers[head].signal();
                // System.out.println("Signals " + head);
                Main.states[head] = EATING;
            }
        } finally {
            lock.unlock();
        }
    }

    // print the states of the philosophers
    public void output() {
        lock.lock();
        for (int k = 0; k < Main.states.length; k++) {
            System.out.print(Main.states[k] + ",");
        }
        lock.unlock();
        System.out.println("\n");
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

    private void printStats() {
        lock.lock();
        System.out.println(id + " exec: " + execTime + " wait: " + waitTime + " ");
        lock.unlock();
    }
}
