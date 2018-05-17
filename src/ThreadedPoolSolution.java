import java.util.Random;
import java.util.concurrent.*;

/***
 * @author George
 * @since 17-May-18
 */
public class ThreadedPoolSolution {


    private void runSim(int customerCount, int waitingSeats, RandomCalling trimTime, RandomCalling waitTime) throws InterruptedException, ExecutionException {
        //general information
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        ArrayBlockingQueue<Customer> waitingChairs = new ArrayBlockingQueue<>(waitingSeats);
        ArrayBlockingQueue<Customer> toShop = new ArrayBlockingQueue<>(customerCount);
        ArrayBlockingQueue<SuccessfulCustomer> fromChair = new ArrayBlockingQueue<>(customerCount);
        ArrayBlockingQueue<Object> fromShop = new ArrayBlockingQueue<>(customerCount);
        Future<Integer> barber = executor.submit(new Callable<Integer>() {


            //lets run this shit
            @Override
            public Integer call() throws Exception {
                int trimmed = 0;
                while(true){
                    try {
                        Customer customer = waitingChairs.take();

                        if(customer.getId() < 0){
                            fromChair.put(new SuccessfulCustomer(customer));
                            break;
                        }
                        System.out.println("Barber >> Starting new customer with ID " + customer.getId());
                        try {
                            Thread.sleep(trimTime.call());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Barber >> Finished customer with ID " + customer.getId());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Barber >> Day over, finished with " + trimmed + " customers trimmed!");
                }
                return trimmed;
            }
        });

        Future<Integer> shop = executor.submit(new Runnable() {
            @Override
            public void run() {
                int turnedAway = 0;
                int trimmed = 0;

                while(true){
                    Customer customer = toShop.poll();
                    if(customer != null){
                        if(customer.getId() == -1) {
                            try {
                                waitingChairs.put(customer);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if(waitingChairs.offer(customer)) {
                                System.out.println("Shop >> Customer " + customer.getId() + " has taken a seat! " + waitingChairs.size() + " are currently in use!");
                            } else {
                                ++turnedAway;
                                System.out.println("Shop >> Customer " + customer.getId() + " has turned away!");

                                try {
                                    fromShop.put(customer);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        SuccessfulCustomer successfulCustomer = fromChair.poll();

                        if(successfulCustomer != null){
                            if(successfulCustomer.customer.getId() == -1){
                                break;
                            } else {
                                ++trimmed;
                                System.out.println("Shop >> Customer " + successfulCustomer.customer.getId() + " is leaving with a fresh cut!");
                                try {
                                    fromShop.put(successfulCustomer);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        System.out.println("Shop >> Closing now with " + trimmed + " customers trimmed and " + turnedAway + " customers turned away");
                    }
                }
            }
        }, 0);

        for(int n = 0; n < customerCount; ++n){
            Thread.sleep(waitTime.call());
            System.out.println("World >> Customer " + n + " has entered the shop!");
            toShop.put(new Customer(n));
        }
        waitingChairs.put(new Customer(-1));
        int trimmed = 0;
        int turnedAway = 0;

        for(int n = 0; n < customerCount; ++n){
            Object customer = fromShop.take();
            int id;

            if(customer instanceof SuccessfulCustomer){
                ++trimmed;

                id = ((SuccessfulCustomer) customer).customer.getId();
            } else {
                throw new RuntimeException("World >> Non customer has entered the shop, tie up your fucking dog!");
            }
            System.out.println("World >> customer " + id + " has left the shop!");
        }
        System.out.println("World >> Closing time");
        int barberCount = barber.get();

        if(barberCount != trimmed){
            System.out.println("World >> Barbers claimed " + barberCount + " the world has counted " + trimmed);
        }
        System.out.println("Trimmed " + barberCount + " and turned away " + turnedAway + " today");
        executor.shutdown(); //fuck off
    }
//fucking git

    private static final class Customer {
        final int id;

        private Customer(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    private static final class SuccessfulCustomer {
        final Customer customer;

        private SuccessfulCustomer(Customer customer) {
            this.customer = customer;
        }
    }

    private static final class RandomCalling {
        private int scale;
        private int offset;

        public RandomCalling(int scale, int offset) {
            this.scale = scale;
            this.offset = offset;
        }

        public int call() {
            return (int) (Math.random() * scale + offset);
        }
    }
}
