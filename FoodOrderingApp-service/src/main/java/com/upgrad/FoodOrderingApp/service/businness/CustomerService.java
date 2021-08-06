package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class CustomerService {

    @Autowired
    private UtilityService utilityService;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private PasswordCryptographyProvider cryptoProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity saveCustomer(CustomerEntity customerEntity) throws SignUpRestrictedException {
        //'SGR-005' To validate of there are any empty fields except for lastname
        if (utilityService.isStringEmptyOrNull(customerEntity.getFirstName()) ||
                utilityService.isStringEmptyOrNull(customerEntity.getEmail()) ||
                utilityService.isStringEmptyOrNull(customerEntity.getContactNumber()) ||
                utilityService.isStringEmptyOrNull(customerEntity.getPassword())
                ) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }

        if (customerDao.getUserByContactNumber(customerEntity.getContactNumber()) != null) {
            throw new SignUpRestrictedException("SGR-001", "This contact number is already registered! Try other contact number.");
        }
        /* 'SGR-002' To validate emailID. This does not allow emails that have . or _ (underscore) in the address as the project statement asks to validate
              in the format of xxx@xx.xx where x is number or letter */
        if (!customerEntity.getEmail().matches("^(([A-Za-z0-9]*))(@)(([A-Za-z0-9]*))(?<!\\.)\\.(?!\\.)(([A-Za-z0-9]*))")) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        } // 'SGR-003' Validates contact no for length = 10 and must contain only digits
        if (!utilityService.isPhoneNumberValid(customerEntity.getContactNumber())) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        } // 'SGR-004' Validates if the password criteria is met
        if (!utilityService.isPasswordValid(customerEntity.getPassword())) {
            throw new SignUpRestrictedException("SGR-004", "Weak password!");
        } else {

            String[] encryptedText = cryptoProvider.encrypt(customerEntity.getPassword());
            customerEntity.setSalt(encryptedText[0]);
            customerEntity.setPassword(encryptedText[1]);
            return customerDao.createNewCustomer(customerEntity);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(String username, String password) throws AuthenticationFailedException {
        CustomerEntity customer = customerDao.getUserByContactNumber(username);
        //ATH-001 The below checks if the entered user exists in the data base
        if (customer == null) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }

        //The entered password is encrypted with the same salt of the user and then compared to see if the encrypted password matches that present in the DB
        final String encryptedPassword = cryptoProvider.encrypt(password, customer.getSalt());
        if (encryptedPassword.equals(customer.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
            customerAuthEntity.setUuid(UUID.randomUUID().toString());
            customerAuthEntity.setCustomer(customer);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customer.getUuid(), now, expiresAt));
            customerAuthEntity.setLoginAt(now);
            customerAuthEntity.setLogoutAt(null);
            customerAuthEntity.setExpiresAt(expiresAt);
            return customerDao.createAuthToken(customerAuthEntity);

        }
        //'ATH-002' An error is thrown is credentials do not match
        else {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity logout(String authtoken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = validateAccessToken(authtoken);
        customerAuthEntity.setLogoutAt(ZonedDateTime.now());
        customerDao.updateCustomerAuthEntity(customerAuthEntity);
        //CustomerEntity customer = customerAuthEntity.getCustomer();
        return customerAuthEntity;
    }

    //Common method that will be used by all endpoints to validate the accessToken
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity validateAccessToken(String authtoken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(authtoken);
        if (customerAuthEntity == null) { // "ATHR-001" to check if authtoken is valid or present in the DB
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }

        if (customerAuthEntity.getLogoutAt() != null) { // "ATHR-002" To check if the authtoken is no more valid since user has logged out
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }
        //"ATHR-003" To check if token has expired, if yes then thelogged out time is updated to current time
        boolean isTokenExpired = utilityService.hasTokenExpired(customerAuthEntity.getExpiresAt().toString());
        if (isTokenExpired) {
            customerAuthEntity.setLogoutAt(ZonedDateTime.now());
            customerDao.updateCustomerAuthEntity(customerAuthEntity);
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
        return customerAuthEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity getCustomer(String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity entity = this.validateAccessToken(accessToken);
        return entity.getCustomer();
    }

    //Update the customer name
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomer(CustomerEntity customer) throws UpdateCustomerException {
        customerDao.updateCustomer(customer);
        return customer;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomerPassword(String oldPassword, String newPassword, CustomerEntity customer) throws UpdateCustomerException {
        //Check if new Password is weak
        if (!utilityService.isPasswordValid(newPassword)) {
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }
        //Check if old password is correct
        final String encryptedPassword = cryptoProvider.encrypt(oldPassword, customer.getSalt());
        if (!encryptedPassword.equals(customer.getPassword())) {
            throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
        }
        String[] encryptedText = cryptoProvider.encrypt(newPassword);
        customer.setSalt(encryptedText[0]);
        customer.setPassword(encryptedText[1]);
        customerDao.updateCustomerPassword(customer);
        return customer;
    }

}
