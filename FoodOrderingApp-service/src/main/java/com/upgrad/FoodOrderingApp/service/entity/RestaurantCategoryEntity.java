package com.upgrad.FoodOrderingApp.service.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="restaurant_category")
@NamedQueries({

        @NamedQuery(name = "getCategoriesByRestaurant", query = "SELECT r FROM RestaurantCategoryEntity r WHERE r.restaurantId= :restaurant ORDER BY r.categoryId.categoryName ASC "),
        @NamedQuery(name = "getRestaurantByCategory", query = "SELECT r FROM RestaurantCategoryEntity r WHERE r.categoryId = :category ORDER BY r.restaurantId.customerRating DESC "),
})
public class RestaurantCategoryEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="restaurant_id")
    private RestaurantEntity restaurantId;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="category_id")
    private CategoryEntity categoryId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RestaurantEntity getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(RestaurantEntity restaurantId) {
        this.restaurantId = restaurantId;
    }

    public CategoryEntity getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(CategoryEntity categoryId) {
        this.categoryId = categoryId;
    }

    public RestaurantCategoryEntity() {
    }
}
