package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

@Controller
@RequestMapping("/")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UtilityService utilityService;

    //Implementation of Get All Restaurants - "/restaurant" API
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/restaurant", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getAllRestaurants() {
        //Get restaurants by rating
        List<RestaurantEntity> restaurantEntities = restaurantService.restaurantsByRating();
        //Get the response list
        List<RestaurantList> restaurantLists = restaurantList(restaurantEntities);
        RestaurantListResponse restaurantListResponse = new RestaurantListResponse().restaurants(restaurantLists);
        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);

    }

    //Implementation of Get Restaurant/s by Name - “/restaurant/name/{reastaurant_name}” API
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/restaurant/name/{restaurant_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantByName(@PathVariable(value = "restaurant_name") final String restaurantName) throws RestaurantNotFoundException {

        List<RestaurantEntity> restaurantEntities = restaurantService.restaurantsByName(restaurantName);
        //Check made to see if restaurant is empty
        if (!restaurantEntities.isEmpty()) {
            List<RestaurantList> restaurantLists = restaurantList(restaurantEntities);
            RestaurantListResponse restaurantListResponse = new RestaurantListResponse().restaurants(restaurantLists);
            return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);

        } else {
            return new ResponseEntity<RestaurantListResponse>(new RestaurantListResponse(), HttpStatus.OK);
        }
    }

    //Implementation of Get Restaurants by Category Id “/restaurant/category/{category_id}” API
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/restaurant/category/{category_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantByCategoryId(@PathVariable(value = "category_id") String categoryId) throws CategoryNotFoundException {

        //Calls restaurantByCategory method of restaurantService to get the list of restaurant entity.
        List<RestaurantEntity> restaurantEntities = restaurantService.restaurantByCategory(categoryId);
        List<RestaurantList> restaurantLists = restaurantList(restaurantEntities);

        //Creating the RestaurantListResponse by adding the list of RestaurantList
        RestaurantListResponse restaurantListResponse = new RestaurantListResponse().restaurants(restaurantLists);
        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);

    }

    //Implementation of Get Restaurant by Restaurant ID - “/api/restaurant/{restaurant_id}” API
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/api/restaurant/{restaurant_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantDetailsResponse> getRestaurantByRestaurantId(@PathVariable(value = "restaurant_id") final String restaurantUuid) throws RestaurantNotFoundException {
        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(restaurantUuid);
        List<CategoryEntity> categoryEntities = categoryService.getCategoriesByRestaurant(restaurantUuid);

        List<CategoryList> categoryLists = new LinkedList<>();
        for (CategoryEntity categoryEntity : categoryEntities) {  //Looping for each CategoryEntity in categoryEntities

            //Calls getItemsByCategoryAndRestaurant of itemService to get list of itemEntities.
            List<ItemEntity> itemEntities = itemService.getItemsByCategoryAndRestaurant(restaurantUuid, categoryEntity.getUuid());
            //Creating Item List for the CategoryList.
            List<ItemList> itemLists = new LinkedList<>();
            itemEntities.forEach(itemEntity -> {
                ItemList itemList = new ItemList()
                        .id(UUID.fromString(String.valueOf(itemEntity.getUuid())))
                        .itemName(itemEntity.getItemName())
                        .price(itemEntity.getPrice());
                ItemList.ItemTypeEnum itemTypeEnum = null;
                try {
                    itemTypeEnum =
                            (Integer.valueOf(itemEntity.getType().toString()) == 0)
                                    ? ItemList.ItemTypeEnum.VEG
                                    : ItemList.ItemTypeEnum.NON_VEG;
                } catch (NumberFormatException e) {
                    String type = itemEntity.getType().getValue();
                    itemTypeEnum = ItemList.ItemTypeEnum.valueOf(type);
                }

                itemList.setItemType(itemTypeEnum);

                itemLists.add(itemList);
            });

            //Creating new category list to add listof category list
            CategoryList categoryList = new CategoryList()
                    .itemList(itemLists)
                    .id(UUID.fromString(categoryEntity.getUuid()))
                    .categoryName(categoryEntity.getCategoryName());

            //adding to the categoryLists
            categoryLists.add(categoryList);

        }
        RestaurantDetailsResponseAddressState restaurantDetailsResponseAddressState = getRestaurantDetailsResponseAddressState(restaurantEntity);
        RestaurantDetailsResponseAddress restaurantDetailsResponseAddress = getRestaurantDetailsResponseAddress(restaurantEntity, restaurantDetailsResponseAddressState);
        RestaurantDetailsResponse restaurantDetailsResponse = new RestaurantDetailsResponse()
                .restaurantName(restaurantEntity.getRestaurantName())
                .address(restaurantDetailsResponseAddress)
                .averagePrice(restaurantEntity.getAveragePriceForTwo())
                .customerRating(BigDecimal.valueOf(restaurantEntity.getCustomerRating()))
                .numberCustomersRated(restaurantEntity.getNumberOfCustomersRated())
                .id(UUID.fromString(restaurantEntity.getUuid()))
                .photoURL(restaurantEntity.getPhotoUrl())
                .categories(categoryLists);

        return new ResponseEntity<RestaurantDetailsResponse>(restaurantDetailsResponse, HttpStatus.OK);

    }


    //Implementation of Update Restaurant Details- “/api/restaurant/{restaurant_id}” API
    @CrossOrigin
    @RequestMapping(method = RequestMethod.PUT, path = "/api/restaurant/{restaurant_id}", params = "customer_rating", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantUpdatedResponse> updateRestaurantDetails(@RequestHeader("authorization") final String authorization, @PathVariable(value = "restaurant_id") final String restaurantUuid, @RequestParam(value = "customer_rating") final Double customerRating)
            throws AuthorizationFailedException, RestaurantNotFoundException, InvalidRatingException {
        String accessToken = utilityService.splitAuthorization(authorization);
        customerService.getCustomer(accessToken);

        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(restaurantUuid);
        restaurantService.updateRestaurantRating(restaurantEntity, customerRating);
        RestaurantUpdatedResponse restaurantUpdatedResponse = new RestaurantUpdatedResponse()
                .id(UUID.fromString(restaurantUuid))
                .status("RESTAURANT RATING UPDATED SUCCESSFULLY");

        return new ResponseEntity<RestaurantUpdatedResponse>(restaurantUpdatedResponse, HttpStatus.OK);
    }

    //The below private methods are methods with common code used in the above API methods

        private List<RestaurantList> restaurantList(List<RestaurantEntity> restaurantEntities){
        List<RestaurantList> restaurantLists = new LinkedList<>();
        for (RestaurantEntity restaurantEntity : restaurantEntities) {

            //Calls  getCategoriesByRestaurant to get categories of the corresponding restaurant.
            List<CategoryEntity> categoryEntities = categoryService.getCategoriesByRestaurant(restaurantEntity.getUuid());
            String categories = "";
            //To concat the category names.
            ListIterator<CategoryEntity> listIterator = categoryEntities.listIterator();
            while (listIterator.hasNext()) {
                categories = categories + listIterator.next().getCategoryName();
                if (listIterator.hasNext()) {
                    categories = categories + ", ";
                }
            }

            RestaurantDetailsResponseAddressState restaurantDetailsResponseAddressState = getRestaurantDetailsResponseAddressState(restaurantEntity);
            RestaurantDetailsResponseAddress restaurantDetailsResponseAddress = getRestaurantDetailsResponseAddress(restaurantEntity, restaurantDetailsResponseAddressState);

            //Creating RestaurantList to add to list of RestaurantList
            RestaurantList restaurantList = new RestaurantList();
            restaurantList.id(UUID.fromString(restaurantEntity.getUuid()));
            restaurantList.restaurantName(restaurantEntity.getRestaurantName());
            restaurantList.averagePrice(restaurantEntity.getAveragePriceForTwo());
            restaurantList.categories(categories);
            restaurantList.customerRating(BigDecimal.valueOf(restaurantEntity.getCustomerRating()));
            restaurantList.numberCustomersRated(restaurantEntity.getNumberOfCustomersRated());
            restaurantList.photoURL(restaurantEntity.getPhotoUrl());
            restaurantList.address(restaurantDetailsResponseAddress);

            //Adding it to the list
            restaurantLists.add(restaurantList);

        }

        return restaurantLists;
    }

    //Creating the RestaurantDetailsResponseAddressState for the RestaurantDetailsResponseAddress
    private RestaurantDetailsResponseAddressState getRestaurantDetailsResponseAddressState( RestaurantEntity restaurantEntity){
        RestaurantDetailsResponseAddressState restaurantDetailsResponseAddressState = new RestaurantDetailsResponseAddressState();
        restaurantDetailsResponseAddressState.id(UUID.fromString(restaurantEntity.getAddress().getStateId().getUuid()));
        restaurantDetailsResponseAddressState.stateName(restaurantEntity.getAddress().getStateId().getStateName());
        return restaurantDetailsResponseAddressState;
    }

    //Creating the RestaurantDetailsResponseAddress for the RestaurantList
    private RestaurantDetailsResponseAddress getRestaurantDetailsResponseAddress(RestaurantEntity restaurantEntity, RestaurantDetailsResponseAddressState restaurantDetailsResponseAddressState){
        RestaurantDetailsResponseAddress restaurantDetailsResponseAddress = new RestaurantDetailsResponseAddress();
        restaurantDetailsResponseAddress.id(UUID.fromString(restaurantEntity.getAddress().getUuid()));
        restaurantDetailsResponseAddress.city(restaurantEntity.getAddress().getCity());
        restaurantDetailsResponseAddress.flatBuildingName(restaurantEntity.getAddress().getFlatBuildingNumber());
        restaurantDetailsResponseAddress.locality(restaurantEntity.getAddress().getLocality());
        restaurantDetailsResponseAddress.pincode(restaurantEntity.getAddress().getPincode());
        restaurantDetailsResponseAddress.state(restaurantDetailsResponseAddressState);
        return restaurantDetailsResponseAddress;
    }

}
