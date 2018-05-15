import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/***
 * @author George
 * @since 15-May-18
 */
public class Main {


    private final Semaphore customerSempahore = new Semaphore(1);
    private final Semaphore barberSemaphore = new Semaphore(2);
    private Semaphore seatAccess = new Semaphore(3);
    private int turnedAway;
    private int trimmed;
    private int freeSeats;

    public void runSim(int customerCount, int waitingSeats, RandomCalling trimTime, RandomCalling customerWaitTime){
        turnedAway = 0;
        trimmed = 0;
        freeSeats = waitingSeats;
        Barber barber = new Barber(trimTime);
        Thread bThread = new Thread(barber);
        bThread.start();

        ArrayList<Thread> customerThreads = new ArrayList<>();

        for (int i = 0; i < customerCount; i++) {
            int n = i;
            System.out.println("Customer: " + i + " has entered the shop!");
            Thread customerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try {
                            seatAccess.acquire();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                    if(freeSeats > 0){
                        System.out.println("Customer: " + n + " has taken a seat!");
                        --freeSeats;
                        customerSempahore.release();
                        seatAccess.release();

                        while(true){
                            try {
                                barberSemaphore.acquire();
                                break;
                            } catch (InterruptedException e) {
                                continue;
                            }

                        }
                        System.out.println("Customer: " + n + " has been trimmed!");
                        ++trimmed;
                    } else {
                        seatAccess.release();
                        System.out.println("Customer: " + n + " has turned away!");
                        ++turnedAway;
                    }
                }
            });

            customerThreads.add(customerThread);
            customerThread.start();
            try {
                Thread.sleep(customerWaitTime.call());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for(Thread t : customerThreads){
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                barber.stopWorking();
                try {
                    bThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Trimmed: " + trimmed + " \n turned away: " + turnedAway);
            }
        }
    }

    private final class Barber extends Thread {
        private RandomCalling trimTime;
        private boolean working = true;

        public Barber(RandomCalling trimTime) {
            this.trimTime = trimTime;
        }

        public void stopWorking() {
            working = false;
        }

        @Override
        public void run() {
            while (working) {
                try {
                    customerSempahore.acquire();
                } catch (InterruptedException ie) {
                    continue;
                }
                try {
                    seatAccess.acquire();
                } catch (InterruptedException ie) {
                    customerSempahore.release();
                    continue;
                }
            }
            ++freeSeats;
            barberSemaphore.release();
            seatAccess.release();
            System.out.println("Barber >> Starting customer!");
            try {
                Thread.sleep(trimTime.call());
            } catch (InterruptedException e) {
                System.out.println("Barber >> Finished customer!");
            }
        }
    }


    private static final class RandomCalling {
        private int scale;
        private int offset;
        public RandomCalling(int scale, int offset) {
            this.scale = scale;
            this.offset = offset;
        }

        public int call(){
            return (int) (Math.random() * scale + offset);
        }
    }
}
