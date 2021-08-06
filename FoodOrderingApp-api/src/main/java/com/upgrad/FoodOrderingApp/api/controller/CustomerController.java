package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.UtilityService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import com.upgrad.FoodOrderingApp.service.util.FoodOrderingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@Controller
@RequestMapping("/")
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private UtilityService utilityService;

    //Implementation of signup - "/customer/signup" API endpoint
    @CrossOrigin
    @RequestMapping(method = POST, path = "/customer/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        customerEntity.setPassword(signupCustomerRequest.getPassword());
        CustomerEntity customer = customerService.saveCustomer(customerEntity);
        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse();
        try {
            signupCustomerResponse.id(customer.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        } catch (Exception e) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }
        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);
    }

    //Implementation of login- "/customer/login" API
    @CrossOrigin
    @RequestMapping(method = POST, path = "/customer/login", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {
        //'ATH-003' The below try-catch block is used to see if the authrorization is in the correct format
        String username;
        String password;
        try {
            byte[] decode = Base64.getDecoder().decode(authorization.split(FoodOrderingUtil.BASIC_TOKEN)[1]);
            String decodedText = new String(decode);
            String[] decodedArray = decodedText.split(FoodOrderingUtil.COLON);
            username = decodedArray[0];
            password = decodedArray[1];
        } catch (Exception e) {
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }
        CustomerAuthEntity customerAuthEntity = customerService.authenticate(username, password);
        CustomerEntity customerEntity = customerAuthEntity.getCustomer();
        LoginResponse loginResponse = new LoginResponse().id(customerEntity.getUuid()).message("LOGGED IN SUCCESSFULLY");
        loginResponse.contactNumber(customerEntity.getContactNumber());
        loginResponse.emailAddress(customerEntity.getEmail());
        loginResponse.firstName(customerEntity.getFirstName());
        loginResponse.lastName(customerEntity.getLastName());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("access-token", customerAuthEntity.getAccessToken());
        return new ResponseEntity<LoginResponse>(loginResponse, httpHeaders, HttpStatus.OK);
    }

    //Implementation of "/customer/logout" API
    @CrossOrigin
    @RequestMapping(method = POST, path = "/customer/logout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        String accessToken = utilityService.splitAuthorization(authorization);
        CustomerAuthEntity authEntity = customerService.logout(accessToken);
        LogoutResponse logoutResponse = new LogoutResponse();
        logoutResponse.setId(authEntity.getCustomer().getUuid());
        logoutResponse.setMessage("LOGGED OUT SUCCESSFULLY");
        return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
    }

    //Implementation of "/customer" API
    @CrossOrigin
    @RequestMapping(method = PUT, path = "/customer", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> updateName(@RequestHeader("authorization") final String authorization, @RequestBody UpdateCustomerRequest updateCustomerRequest) throws AuthorizationFailedException, UpdateCustomerException {
        String accessToken = utilityService.splitAuthorization(authorization);
        CustomerEntity customer = customerService.getCustomer(accessToken);
        String newFirstName = updateCustomerRequest.getFirstName();
        String newLastName = updateCustomerRequest.getLastName();
        //  Check if the firstname entered is empty
        if (utilityService.isStringEmptyOrNull(newFirstName)) {
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        } else {
            customer.setFirstName(newFirstName);
            customer.setLastName(newLastName);
            CustomerEntity updateCustomer = customerService.updateCustomer(customer);

            UpdateCustomerResponse response = new UpdateCustomerResponse();
            response.setId(updateCustomer.getUuid());
            response.setFirstName(updateCustomer.getFirstName());
            response.setLastName(updateCustomer.getLastName());
            response.setStatus("CUSTOMER DETAILS UPDATED SUCCESSFULLY");
            return new ResponseEntity<UpdateCustomerResponse>(response, HttpStatus.OK);
        }

    }

    //Implementation of "/customer/password" API
    @CrossOrigin
    @RequestMapping(method = PUT, path = "/customer/password", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> updatePassword(@RequestHeader("authorization") final String authorization, @RequestBody UpdatePasswordRequest updatePasswordRequest) throws AuthorizationFailedException, UpdateCustomerException {
        String oldPassword = updatePasswordRequest.getOldPassword();
        String newPassword = updatePasswordRequest.getNewPassword();
        if (utilityService.isStringEmptyOrNull(oldPassword) || utilityService.isStringEmptyOrNull(newPassword)) {
            throw new UpdateCustomerException("UCR-003", "No field should be empty");
        }
        String accessToken = utilityService.splitAuthorization(authorization);
        CustomerEntity customer = customerService.getCustomer(accessToken);
        CustomerEntity updateCustomer = customerService.updateCustomerPassword(oldPassword, newPassword, customer);

        UpdatePasswordResponse response = new UpdatePasswordResponse();
        response.setId(updateCustomer.getUuid());
        response.setStatus("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");
        return new ResponseEntity<UpdatePasswordResponse>(response, HttpStatus.OK);
    }

}
