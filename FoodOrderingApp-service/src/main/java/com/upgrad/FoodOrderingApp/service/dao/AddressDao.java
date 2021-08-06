package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AddressDao {
    @PersistenceContext
    private EntityManager entityManager;

    public StateEntity getStateByUuid(String uuid) {
        try {
            return entityManager.createNamedQuery("getStateByUuid", StateEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nRE) {
            return null;
        }
    }

    public AddressEntity saveAddress(final AddressEntity address) {
        entityManager.persist(address);
        return address;
    }

    public List<AddressEntity> getAllAddresses() {
        List<AddressEntity> addressList = entityManager.createNamedQuery("getAllAddresses", AddressEntity.class).getResultList();
        return addressList;
    }

    public AddressEntity getAddressByUUID(String uuid){
        try {
            return entityManager.createNamedQuery("getAddressByUuid", AddressEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nRE) {
            return null;
        }
    }

    public CustomerAddressEntity getEntityByAddress(int address_id){
        try{
            return entityManager.createNamedQuery("getEntityByAddress", CustomerAddressEntity.class).setParameter("address_id",address_id).getSingleResult();
        }catch (NoResultException nRE) {
            return null;
        }
    }

    public void deleteAddressEntityByUuid(String uuid) {
        entityManager.createQuery("DELETE FROM AddressEntity u WHERE uuid=:uuid").setParameter("uuid", uuid).executeUpdate();
    }

    public List<StateEntity> getAllStates() {
       try{
           List<StateEntity> listOfStates = entityManager.createNamedQuery("getAllStates", StateEntity.class).getResultList();
           return listOfStates;
       }catch (NoResultException nRE){
           return null;
       }
    }

}
