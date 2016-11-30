/*
Modify the dining philosopher program to be starvation free.
The solution may exhibit brief busy wait loops, if necessary.
An implementation that does not use multiple locks in meaningful ways will
receive at most 45 points.

Experiment by executing your program on a 2-core (or more) machine with
10 philosophers, 25 philosophers, 50 philosophers & 100 philosophers.
Compare their execution times, waiting times, number of times each thread stalls
& other meaningful metrics (e.g. minimum number of turns someone has eaten).
How well does your solution scale? Do you observe any bottleneck?

Submit your program, cover sheet & a summary/discussion of the 4 experiments.
*/
import java.util.concurrent.locks.*;
import java.util.LinkedList;
import java.util.Queue;

public class Main {
    private static int WAITING = 0, EATING = 1, THINKING = 2;
    private static final int NUM_PHILS = 10;
    private static Lock lock = new ReentrantLock();
    private static Condition philosophers [] = new Condition[NUM_PHILS];
    public static int states [] = new int[NUM_PHILS];
    public static Queue<Integer> q = new LinkedList<Integer>();

    // init all philosophers to THINKING
    public static void init () {
        for (int k = 0; k < NUM_PHILS; k++) {
            philosophers[k] = lock.newCondition();
            states[k] = THINKING;
        }
    }

    public static void main(String[] args) {
        init();
        Philosopher p[] = new Philosopher[NUM_PHILS];
        for (int k = 0; k < p.length; k++) {
            p[k] = new Philosopher(lock, philosophers, NUM_PHILS, 0, 0);
            p[k].start();
        }
    }
}
