package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantCategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantCategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

@Service
public class RestaurantService {
    @Autowired
    private  RestaurantDao restaurantDao;
    @Autowired
    private UtilityService utilityService;
    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private RestaurantCategoryDao restaurantCategoryDao;

    @Transactional
    public RestaurantEntity restaurantByUUID(String uuid) throws RestaurantNotFoundException {
        if(utilityService.isStringEmptyOrNull(uuid)){
            throw new RestaurantNotFoundException("RNF-002","Restaurant id field should not be empty");
        }
        RestaurantEntity restaurant = restaurantDao.getRestaurantByUUID(uuid);
        if(restaurant == null){
            throw new RestaurantNotFoundException("RNF-001","No restaurant by this id");
        }
        else
            return restaurant;
    }

    @Transactional
    public List<RestaurantEntity> restaurantsByRating() {
        List<RestaurantEntity> restaurantEntities = restaurantDao.restaurantsByRating();
        return restaurantEntities;
    }

    @Transactional
    public List<RestaurantEntity> restaurantsByName(String restaurantName) throws RestaurantNotFoundException {
        if (utilityService.isStringEmptyOrNull(restaurantName)) {
            throw new RestaurantNotFoundException("RNF-003", "Restaurant name field should not be empty");
        }

        List<RestaurantEntity> restaurantEntities = restaurantDao.restaurantsByName(restaurantName);
        return restaurantEntities;
    }

    @Transactional
    public List<RestaurantEntity> restaurantByCategory(String categoryId) throws CategoryNotFoundException {

        if (utilityService.isStringEmptyOrNull(categoryId)) {
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }
        CategoryEntity categoryEntity = categoryDao.getCategoryByUuid(categoryId);

        if (categoryEntity == null) {
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }
        List<RestaurantCategoryEntity> restaurantCategoryEntities = restaurantCategoryDao.getRestaurantByCategory(categoryEntity);

        List<RestaurantEntity> restaurantEntities = new LinkedList<>();
        for (RestaurantCategoryEntity rce : restaurantCategoryEntities ) {
            restaurantEntities.add(rce.getRestaurantId());
        }
        return restaurantEntities;
    }

    @Transactional
    public RestaurantEntity updateRestaurantRating(RestaurantEntity restaurantEntity, Double customerRating) throws InvalidRatingException, RestaurantNotFoundException {
        if (!utilityService.isValidCustomerRating(customerRating.toString())) {
            throw new InvalidRatingException("IRE-001", "Restaurant should be in the range of 1 to 5");
        }
        else {
            //Finding the new Customer rating and update it.
            DecimalFormat format = new DecimalFormat("##.0");
            double restaurantRating = restaurantEntity.getCustomerRating();
            Integer restaurantNoOfCustomerRated = restaurantEntity.getNumberOfCustomersRated();
            restaurantEntity.setNumberOfCustomersRated(restaurantNoOfCustomerRated + 1);

            //Calculating the new customer rating as per the given data and formula
            double newCustomerRating = (restaurantRating * (restaurantNoOfCustomerRated.doubleValue()) + customerRating) / restaurantEntity.getNumberOfCustomersRated();
            restaurantEntity.setCustomerRating(Double.parseDouble(format.format(newCustomerRating)));

            //Updating the restautant in the db using the method updateRestaurantRating of restaurantDao.
            RestaurantEntity updatedRestaurantEntity = restaurantDao.updateRestaurantRating(restaurantEntity);

            return updatedRestaurantEntity;
        }
    }

}

