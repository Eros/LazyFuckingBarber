/***
 * @author George
 * @since 17-May-18
 */
public class ThreadedPoolSolution {


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
