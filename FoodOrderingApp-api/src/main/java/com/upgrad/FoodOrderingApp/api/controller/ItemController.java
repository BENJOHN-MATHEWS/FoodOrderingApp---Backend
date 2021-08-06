package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.api.model.ItemListResponse;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.UUID;


@Controller
@RequestMapping("/")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @Autowired
    private RestaurantService restaurantService;

    //Implementation of Get Top 5 Items by Popularity - “/item/restaurant/{restaurant_id} API
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/item/restaurant/{restaurant_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ItemListResponse> getItemsByPopularity(
            @PathVariable("restaurant_id") final String restaurantId) throws RestaurantNotFoundException
    {

        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(restaurantId);
        List<ItemEntity> itemList = itemService.getItemsByPopularity(restaurantEntity);
        ItemListResponse itemListResponse = new ItemListResponse();

        int count = 0;
        for(ItemEntity itemEntity:itemList){
            if(count<5) {
                ItemList items = new ItemList();
                items.id(UUID.fromString(itemEntity.getUuid()));
                items.itemName(itemEntity.getItemName());
                items.price(itemEntity.getPrice());
                items.itemType(ItemList.ItemTypeEnum.fromValue(itemEntity.getType().getValue()));
                itemListResponse.add(items);
                count = count + 1;
            }
            else{
                break;
            }
        }

        return new ResponseEntity<ItemListResponse>(itemListResponse, HttpStatus.OK);
    }
}
