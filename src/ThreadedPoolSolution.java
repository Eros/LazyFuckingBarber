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
                }
                return null;
            }
        });
    }

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
