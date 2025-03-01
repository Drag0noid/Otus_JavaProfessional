package homework;

import java.util.LinkedList;
import java.util.Deque;

public class CustomerReverseOrder {

    private final Deque<Customer> cus;

    public CustomerReverseOrder() {
        cus = new LinkedList<>();
    }

    public void add(Customer customer) {
        cus.add(new Customer(customer));
    }

    public Customer take() {
        return new Customer(cus.removeLast());
    }
}