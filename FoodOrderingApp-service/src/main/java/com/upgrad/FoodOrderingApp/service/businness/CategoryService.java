package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantCategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantCategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private RestaurantDao restaurantDao;
    @Autowired
    private RestaurantCategoryDao restaurantCategoryDao;
    @Autowired
    private UtilityService utilityService;


    public List<CategoryEntity> getCategoriesByRestaurant(String restaurantUuid){
        //Get the restaurant entity and all the categories of the restaurant
        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUUID(restaurantUuid);
        List<RestaurantCategoryEntity> restaurantCategoryEntities = restaurantCategoryDao.getCategoriesByRestaurant(restaurantEntity);

        //Create Category list
        List<CategoryEntity> categoryEntities = new LinkedList<>();
        for ( RestaurantCategoryEntity rce : restaurantCategoryEntities) {
            categoryEntities.add(rce.getCategoryId());
        }

        return categoryEntities;

    }

    public List<CategoryEntity> getAllCategoriesOrderedByName() {
        //Calls getAllCategoriesOrderedByName of categoryDao to get list of CategoryEntity
        List<CategoryEntity> categoryEntities = categoryDao.getAllCategoriesOrderedByName();
        return categoryEntities;
    }

    public CategoryEntity getCategoryById(String categoryUuid) throws CategoryNotFoundException {
        if (utilityService.isStringEmptyOrNull(categoryUuid)) {
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }
        CategoryEntity categoryEntity = categoryDao.getCategoryByUuid(categoryUuid);
        if (categoryEntity == null) {
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }
        return categoryEntity;
    }
}


