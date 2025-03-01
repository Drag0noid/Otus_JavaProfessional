package homework;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class CustomerService {
    private final TreeMap<Customer, String> cus;

    public CustomerService() {
        cus = new TreeMap<>(Comparator.comparingLong(Customer::getScores));
    }

    public Map.Entry<Customer, String> getSmallest() {
        Map.Entry<Customer, String> smallestCustomerService = cus.firstEntry();
        return new AbstractMap.SimpleEntry<>(
                new Customer(smallestCustomerService.getKey()), smallestCustomerService.getValue());
    }

    public Map.Entry<Customer, String> getNext(Customer customer) {
        Customer customerNext = cus.higherKey(customer);
        if (customer == null || customerNext == null) {
            return null;
        }
        return new AbstractMap.SimpleEntry<>(new Customer(customerNext), cus.get(customerNext));
    }

    public void add(Customer customer, String data) {
        cus.put(customer, data);
    }
}