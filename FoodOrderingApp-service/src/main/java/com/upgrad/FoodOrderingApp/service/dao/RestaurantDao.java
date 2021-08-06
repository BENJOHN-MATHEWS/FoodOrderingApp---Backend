package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class RestaurantDao {
    @PersistenceContext
    private EntityManager entityManager;

    public RestaurantEntity getRestaurantByUUID(String uuid){
        try {
            return entityManager.createNamedQuery("getRestaurantByUuid", RestaurantEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<RestaurantEntity> restaurantsByRating() {
        try {
            List<RestaurantEntity> restaurantEntities = entityManager.createNamedQuery("restaurantsByRating", RestaurantEntity.class).getResultList();
            return restaurantEntities;
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<RestaurantEntity> restaurantsByName(String restaurantName) {
        try {
            String restaurantNameLowerCase = "%" + restaurantName.toLowerCase() + "%";
            List<RestaurantEntity> restaurantEntities = entityManager.createNamedQuery("restaurantsByName", RestaurantEntity.class).setParameter("restaurant_lowercase", restaurantNameLowerCase).getResultList();
            return restaurantEntities;
        } catch (NoResultException nre) {
            return null;
        }

    }

    public RestaurantEntity updateRestaurantRating(RestaurantEntity restaurantEntity) {
        entityManager.merge(restaurantEntity);
        return restaurantEntity;
    }
}
