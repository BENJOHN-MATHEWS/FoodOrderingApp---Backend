package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressDao addressDao;
    @Autowired
    private PasswordCryptographyProvider cryptoProvider;
    @Autowired
    private UtilityService utilityService;
    @Autowired
    CustomerDao customerDao;
    @Autowired
    CustomerAddressDao customerAddressDao;


    public void validateAddressRequest(String buildName, String locality, String city, String pincode , String stateID ) throws SaveAddressException {
        if(utilityService.isStringEmptyOrNull(buildName) ||
                utilityService.isStringEmptyOrNull(locality) ||
                utilityService.isStringEmptyOrNull(city) ||
                utilityService.isStringEmptyOrNull(pincode) ||
                utilityService.isStringEmptyOrNull(stateID)) {
            throw new SaveAddressException("SAR-001","No field can be empty");
        }
    }


    public StateEntity getStateByUUID(String uuid) throws AddressNotFoundException {
        StateEntity state = addressDao.getStateByUuid(uuid);
        if (state == null) {
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        }
        return state;
    }

    @Transactional
    public AddressEntity saveAddress(AddressEntity addressEntity, StateEntity state) throws AddressNotFoundException, SaveAddressException {
        if (state == null) {
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        }
        //Check if pincode is valid
        if(!utilityService.isPincodeValid(addressEntity.getPincode())){
            throw new SaveAddressException("SAR-002","Invalid pincode");
        }
        addressEntity.setStateId(state);
        addressEntity.setActive(1);
        return  addressDao.saveAddress(addressEntity);
    }

    @Transactional
    public void saveCustomerAddress(AddressEntity addressEntity, CustomerEntity customer){

        //Create a customer address entity
        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setCustomer(customer);
        customerAddressEntity.setAddress(addressEntity);

        // Set entry in the customer_address table
        customerAddressDao.createNewCustomerAddressEntry(customerAddressEntity);
    }

    @Transactional
    public List<AddressEntity> getAllAddress(CustomerEntity customerEntity){
        return addressDao.getAllAddresses();
    }

    @Transactional
    public AddressEntity getAddressByUUID(String addressUUID, CustomerEntity customerEntity) throws AddressNotFoundException, AuthorizationFailedException {
        //Check if address entered is empty or null
        if(utilityService.isStringEmptyOrNull(addressUUID)){
            throw new AddressNotFoundException("ANF-005","Address id can not be empty");
        }
        AddressEntity dbAddress = addressDao.getAddressByUUID(addressUUID);
        //check if address is present in DB
        if(dbAddress == null){
            throw new AddressNotFoundException("ANF-003","No address by this id");
        }
        //check if user is authorized to delete
        CustomerAddressEntity customerAddressEntity = addressDao.getEntityByAddress(dbAddress.getId());
        if(!customerAddressEntity.getCustomer().equals(customerEntity)){
            throw new AuthorizationFailedException("ATHR-004","You are not authorized to view/update/delete any one else's address");
        }
        return dbAddress;
    }

    @Transactional
    public AddressEntity deleteAddress(AddressEntity addressEntity){
        String uuid = addressEntity.getUuid();
        addressDao.deleteAddressEntityByUuid(uuid);
        return addressEntity;
    }

    @Transactional
    public List<StateEntity> getAllStates(){
        List<StateEntity> listOfStates = addressDao.getAllStates();
        if(listOfStates == null){
            return null;
        }
        return  listOfStates;
    }
}
