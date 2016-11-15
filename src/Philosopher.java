import java.util.concurrent.locks.*;

public class Philosopher extends Thread {
  private static int WAITING = 0, EATING = 1, THINKING = 2;
  private Lock lock;
  private Condition phil [];
  private int states [];
  private int NUM_PHILS;
  private int id;
  private final int TURNS = 20;

  // con
  public Philosopher (Lock l, Condition p[], int st[], int num) {
    lock = l;
    phil = p;
    states = st;
    NUM_PHILS = num;
  }

  public void run () {
    id = ThreadID.get();
    for (int k = 0; k < TURNS; k++) {
      try {
        sleep(100);
      } catch (Exception ex) { /* lazy */}
      takeSticks(id);

      try {
        sleep(20);
      } catch (Exception ex) { }
      putSticks(id);
    }
    output();
  }

  // if the left and right chopstick is free for the current philosopher, they can enter the EATING state
  // if one chopstick is unavailable, the philosopher waits to be notified of when the chopstick will be available
  public void takeSticks (int id) {
    lock.lock();
    try {
      if (states[leftof(id)] != EATING && states[rightof(id)] != EATING)
        states[id] = EATING;
      else {
        states[id] = WAITING;
        //philosopher waits until signalled or interrupted
        phil[id].await();
      }
    } catch(InterruptedException e){
      System.exit(-1);
    } finally {
      lock.unlock();
    }
  }

  public void output() {
    lock.lock();
    for (int k = 0; k < states.length; k++)
      System.out.print(states[k]+",");
    lock.unlock();
    System.out.println();
    System.out.println();
  }

  public void putSticks (int id) {
    lock.lock();
    try {
      states[id] = THINKING;
      if (states[leftof(id)]==WAITING && states[leftof(leftof(id))]!=EATING) {
        phil[leftof(id)].signal();
        states[leftof(id)] = EATING;
      }
      if (states[rightof(id)] == WAITING && states[rightof(rightof(id))] != EATING) {
        phil[rightof(id)].signal(); states[rightof(id)] = EATING;
      }
    } finally {
      lock.unlock();
    }
  }

  private int leftof (int id) { // clockwise
    int retval = id-1;
    if (retval < 0) // not valid id
      retval = NUM_PHILS-1;
    return retval;
  }

  private int rightof (int id) {
    int retval = id+1;
    if (retval == NUM_PHILS) // not valid id
      retval=0;
    return retval;
  }
}
