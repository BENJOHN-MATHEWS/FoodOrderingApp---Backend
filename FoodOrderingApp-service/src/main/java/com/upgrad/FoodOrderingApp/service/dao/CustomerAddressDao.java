package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerAddressDao {
    @PersistenceContext
    private EntityManager entityManager;

    // Updates the customer auth entity in the 'customer_auth' table
    public void createNewCustomerAddressEntry(CustomerAddressEntity customerAddressEntity) {
        this.entityManager.persist(customerAddressEntity);
    }
}
