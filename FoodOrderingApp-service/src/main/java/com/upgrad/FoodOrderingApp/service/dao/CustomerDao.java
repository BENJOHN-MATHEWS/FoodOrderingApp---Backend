package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerDao {
    @PersistenceContext
    private EntityManager entityManager;

    public CustomerEntity getUserByContactNumber(String contactNumber) {
        try {
            return this.entityManager.createNamedQuery("getUserByContactNumber", CustomerEntity.class)
                    .setParameter("contactNumber", contactNumber).getSingleResult();
        } catch (NoResultException nRE) {
            return null;
        }
    }

    public CustomerEntity createNewCustomer(CustomerEntity customer) {
        this.entityManager.persist(customer);
        return customer;
    }

    public CustomerAuthEntity createAuthToken(final CustomerAuthEntity customerAuthEntity) {
        entityManager.persist(customerAuthEntity);
        return customerAuthEntity;
    }

    public CustomerAuthEntity getCustomerAuthToken(final String accessToken) {
        try {
            return entityManager.createNamedQuery("getCustomerAuthByAccessToken", CustomerAuthEntity.class).setParameter("accessToken", accessToken).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    // Checks to see if a customer entity exists
    public CustomerEntity getCustomerEntityById(int customerId) {
        try {
            return this.entityManager.createNamedQuery("getUserByCustomerId", CustomerEntity.class)
                    .setParameter("id", customerId).getSingleResult();
        } catch (NoResultException nRE) {
            return null;
        }
    }

    //Update customer_auth table
    public void updateCustomerAuthEntity(CustomerAuthEntity authEntity) {
        entityManager.merge(authEntity);
    }

    //Update customer table
    public void updateCustomer(CustomerEntity customer) {
        entityManager.merge(customer);
    }

    //update customer table with the new password and salt
    public void updateCustomerPassword(CustomerEntity customer) {
        entityManager.merge(customer);
    }

    public CustomerEntity getCustomerByUuid(String customerUuid) {
        try {
            CustomerEntity customer =
                    entityManager.createNamedQuery("getUserByCustomerUUID", CustomerEntity.class).setParameter("customerId", customerUuid).getSingleResult();
            return customer;
        } catch (NoResultException nre) {
            return null;
        }
    }

}
