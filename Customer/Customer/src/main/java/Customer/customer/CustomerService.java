package Customer.customer;

import Customer.exception.DuplicateResourceException;
import Customer.exception.RequestValidationException;
import Customer.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService(@Qualifier("jdbc") CustomerDao customerDao) {
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
        if (customerDao.existsCustomerWithEmail(email)) {
            throw new DuplicateResourceException("Customer with email [%s] exists".formatted(email));
        }
        Customer customer = new Customer(
                customerRegistrationRequest.name(),
                customerRegistrationRequest.email(),
                customerRegistrationRequest.age()
        );
        customerDao.insertCustomer(customer);
    }

    public void deleteCustomerById(Integer id) {
        if (!customerDao.existsCustomerWithId(id)) {
            throw new ResourceNotFoundException("Customer with id [%s] does not exists".formatted(id));
        }
        customerDao.deleteCustomerById(id);
    }

    public void updateCustomer(Integer customerId,
                               CustomerRegistrationRequest updateRequest) {
        Customer customer = getCustomer(customerId);

        boolean changes = false;

        if (updateRequest.name() != null && !updateRequest.name().equals(customer.getName())) {
            customer.setName(updateRequest.name());
            changes = true;
        }

        if (updateRequest.age() != null && !updateRequest.age().equals(customer.getAge())) {
            customer.setAge(updateRequest.age());
            changes = true;
        }

        if (updateRequest.email() != null && !updateRequest.email().equals(customer.getEmail())) {
            if (customerDao.existsCustomerWithEmail(updateRequest.email())) {
                throw new DuplicateResourceException("email already taken");
            }
            customer.setEmail(updateRequest.email());
            changes = true;
        }
        if (!changes) {
            throw new RequestValidationException("No data changes found");
        }
        customerDao.updateCustomer(customer);
    }
}