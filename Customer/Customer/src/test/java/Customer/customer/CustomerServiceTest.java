package Customer.customer;

import Customer.exception.DuplicateResourceException;
import Customer.exception.RequestValidationException;
import Customer.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerDao customerDao;
    private CustomerService underTest;

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(customerDao);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAllCustomers() {
        //When
        underTest.getAllCustomers();

        //Then
        verify(customerDao).selectAllCustomers();
    }

    @Test
    void canGetCustomer() {
        //Given
        int id = 1;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail.com", 19
        );
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        //When
        Customer actual = underTest.getCustomer(id);

        //Then
        assertThat(actual).isEqualTo(customer);
    }

    @Test
    void willThrowWhenGetCustomerReturnEmptyOptional() {
        //Given
        int id = 10;

        when(customerDao.selectCustomerById(id)).thenReturn(Optional.empty());

        //When
        //Then
        assertThatThrownBy(() -> underTest.getCustomer(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "customer with id [%s] not found".formatted(id));
    }

    @Test
    void addCustomer() {
        //Given
        String email = "alex@gmail.com";

        when(customerDao.existsCustomerWithEmail(email)).thenReturn(false);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Alex", email, 19
        );

        //When
        underTest.addCustomer(request);

        //Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);

        verify(customerDao).insertCustomer(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isNull();
        assertThat(capturedCustomer.getName()).isEqualTo(request.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(request.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(request.age());

    }

    @Test
    void addCustomerWillThrowWhenEmailExists() {
        //Given
        String email = "alex@gmail.com";

        when(customerDao.existsCustomerWithEmail(email)).thenReturn(true);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Alex", email, 19
        );

        //When
        assertThatThrownBy(() -> underTest.addCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Customer with email [%s] exists".formatted(email));

        //Then
        verify(customerDao, never()).insertCustomer(any());
    }


    @Test
    void deleteCustomerById() {
        //Given
        int id = 1;

        when(customerDao.existsCustomerWithId(id)).thenReturn(true);

        //When
        underTest.deleteCustomerById(id);

        //Then
        verify(customerDao).deleteCustomerById(id);
    }

    @Test
    void deleteCustomerByIdWillThrowNotExists() {
        //Given
        int id = 1;

        when(customerDao.existsCustomerWithId(id)).thenReturn(false);

        //When
        assertThatThrownBy(() -> underTest.deleteCustomerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] does not exists".formatted(id));

        //Then
        verify(customerDao, never()).deleteCustomerById(id);
    }

    @Test
    void canUpdateAllCustomerProperties() {
        //Given
        int id = 1;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail.com", 20
        );
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        String newEmail = "alexandro@gmail.com";
        CustomerRegistrationRequest updateRequest = new CustomerRegistrationRequest("Alexandro",
                newEmail, 22);

        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(false);

        //When
        underTest.updateCustomer(id, updateRequest);

        //Then
        ArgumentCaptor<Customer> customerArgumentCaptor =
                ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(updateRequest.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(updateRequest.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(updateRequest.age());

    }

    @Test
    void canUpdateCustomerName() {
        //Given
        int id = 1;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail.com", 20
        );
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));
        CustomerRegistrationRequest updateRequest = new CustomerRegistrationRequest("Alexandro",
                null, null);

        //When
        underTest.updateCustomer(id, updateRequest);

        //Then
        ArgumentCaptor<Customer> customerArgumentCaptor =
                ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(updateRequest.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());

    }

    @Test
    void canUpdateCustomerEmail() {
        //Given
        int id = 1;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail.com", 20
        );
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));
        String newEmail = "alexandro@gmail.com";
        CustomerRegistrationRequest updateRequest = new CustomerRegistrationRequest(null,
                newEmail, null);
        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(false);

        //When
        underTest.updateCustomer(id, updateRequest);

        //Then
        ArgumentCaptor<Customer> customerArgumentCaptor =
                ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getEmail()).isEqualTo(newEmail);
        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());

    }

    @Test
    void canUpdateCustomerAge() {
        //Given
        int id = 1;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail.com", 20
        );
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));
        CustomerRegistrationRequest updateRequest = new CustomerRegistrationRequest(null,
                null, 23);

        //When
        underTest.updateCustomer(id, updateRequest);

        //Then
        ArgumentCaptor<Customer> customerArgumentCaptor =
                ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getAge()).isEqualTo(updateRequest.age());

    }

    @Test
    void willThrowWhenTryingToUpdateCustomerEmailWhenAlreadyTaken() {
        //Given
        int id = 1;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail.com", 20
        );
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));
        String newEmail = "alexandro@gmail.com";
        CustomerRegistrationRequest updateRequest = new CustomerRegistrationRequest(null,
                newEmail, null);
        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(true);

        //When
        assertThatThrownBy(() -> underTest.updateCustomer(id, updateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("email already taken");

        //Then
        verify(customerDao, never()).updateCustomer(any());
    }

    @Test
    void willThrowWhenCustomerUpdateHasNoChanges() {
        //Given
        int id = 1;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail.com", 20
        );
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerRegistrationRequest updateRequest =
                new CustomerRegistrationRequest(
                        customer.getName(),
                        customer.getEmail(),
                        customer.getAge());

        //When
        assertThatThrownBy(() -> underTest.updateCustomer(id, updateRequest))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("No data changes found");

        //Then
        verify(customerDao, never()).updateCustomer(any());

    }
}