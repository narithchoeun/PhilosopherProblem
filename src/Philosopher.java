import java.util.concurrent.locks.*;
import java.util.Random;

public class Philosopher extends Thread {
    private static int WAITING = 0, EATING = 1, THINKING = 2;
    private Lock lock;
    private Condition philosophers [];
    private int states []; //implicitly describes forks
    private int NUM_PHILS;
    private int id;
    private final int TURNS = 3;

    private int maxSleepTime = 100;
    private Random rand = new Random();;
    private long execTime = 0, waitTime = 0;
    private long startTime = 0, endTime = 0;
    private int stallCtr = 0;

    // constructor
    public Philosopher (Lock l, Condition p[], int st[], int num) {
        lock = l;
        philosophers = p;
        states = st;
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

            try {
                sleep(randomTime());
            } catch (Exception ex) { }
            putSticks(id);
            output();
        }

        endTime = System.nanoTime();
        execTime = endTime - startTime;
        // System.out.println(execTime + " " + id);
    }

    // if the left and right chopstick is free for the current philosopher, they can enter the EATING state
    // if one chopstick is unavailable, the philosopher waits to be notified of when the chopstick will be available
    public void takeSticks (int id) {
        lock.lock();
        // System.out.println(id + " trying to take sticks...");
        try {
            if(Main.q.peek() != null){
                // System.out.println("Peek: " + Main.q.peek());
                if ((states[leftof(id)] != EATING && states[rightof(id)] != EATING) && (Main.q.peek() == id)) {
                    // System.out.println(id + " took sticks");
                    Main.q.remove();
                    states[id] = EATING;
                }
            } else {
                Main.q.add(id);
                // System.out.println("Add: " + Main.q.peek());
                states[id] = WAITING;
                //philosopher waits until signaled or interrupted
                philosophers[id].await();
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
        for (int k = 0; k < states.length; k++) {
            System.out.print(states[k] + ",");
        }

        lock.unlock();

        System.out.println();
        System.out.println();
    }

    // philosopher is finished eating, notify the left & right philosopher if they are waiting and have a 2nd utensil available
    public void putSticks (int id) {
        lock.lock();
        try {
            states[id] = THINKING;
            //if the left philosopher is waiting to eat and the left of that philosopher is not eating,
            //signal that person that the stick is now available to use and begin eating
            if (Main.q.peek() != null){
                if ((states[leftof(id)]==WAITING && states[leftof(leftof(id))]!=EATING) && (Main.q.peek() == leftof(id))) {
                    philosophers[leftof(id)].signal();
                    states[leftof(id)] = EATING;
                    // System.out.println(states[leftof(id)] + " is now eating(left)");
                }

                //same for right philosopher
                if ((states[rightof(id)] == WAITING && states[rightof(rightof(id))] != EATING) && (Main.q.peek() == rightof(id))) {
                    philosophers[rightof(id)].signal();
                    states[rightof(id)] = EATING;
                    // System.out.println(states[leftof(id)] + " is now eating(right)");
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
