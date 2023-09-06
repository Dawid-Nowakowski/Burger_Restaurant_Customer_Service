package Customer.customer;

import Customer.exception.DuplicateResourceException;
import Customer.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService(@Qualifier("jpa") CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public List<Customer> getAllCustomers() {
        return customerDao.selectAllCustomers();
    }

    public Customer getCustomer(Integer id) {
        return customerDao.selectCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "customer with id [%s] not found".formatted(id)
                ));
    }

    public void addCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
        String email = customerRegistrationRequest.email();
        if (customerDao.existsPersonWithEmail(email)) {
            throw new DuplicateResourceException("Customer with email [%s] exists".formatted(email));
        }
        Customer customer = new Customer(
                customerRegistrationRequest.name(),
                customerRegistrationRequest.email(),
                customerRegistrationRequest.age()
        );
        customerDao.insertCustomer(customer);
    }

}
