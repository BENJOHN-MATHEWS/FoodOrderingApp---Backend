package com.upgrad.FoodOrderingApp.service.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name="restaurant")
@NamedQueries({

        @NamedQuery(name = "getRestaurantByUuid", query = "SELECT r FROM RestaurantEntity r WHERE r.uuid = :uuid"),
        @NamedQuery(name = "restaurantsByRating", query = "SELECT r FROM RestaurantEntity r ORDER BY r.customerRating DESC"),
        @NamedQuery(name = "restaurantsByName", query = "SELECT r FROM  RestaurantEntity r WHERE LOWER(r.restaurantName) LIKE :restaurant_lowercase"),
})
public class RestaurantEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="uuid")
    @Size(max=200)
    @NotNull
    private String uuid;

    @Column(name="restaurant_name")
    @Size(max=50)
    @NotNull
    private String restaurantName;

    @Column(name="photo_url")
    @Size(max=255)
    @NotNull
    private String photoUrl;

    @Column(name="customer_rating")
    @NotNull
    private double customerRating;

    @Column(name="average_price_for_two")
    @NotNull
    private Integer averagePriceForTwo;

    @Column(name="number_of_customers_rated")
    @NotNull
    private Integer numberOfCustomersRated;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="address_id")
    private AddressEntity address;

    public RestaurantEntity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public double getCustomerRating() {
        return customerRating;
    }

    public void setCustomerRating(double customerRating) {
        this.customerRating = customerRating;
    }

    public Integer getAveragePriceForTwo() {
        return averagePriceForTwo;
    }

    public void setAveragePriceForTwo(Integer averagePriceForTwo) {
        this.averagePriceForTwo = averagePriceForTwo;
    }

    public Integer getNumberOfCustomersRated() {
        return numberOfCustomersRated;
    }

    public void setNumberOfCustomersRated(Integer numberOfCustomersRated) {
        this.numberOfCustomersRated = numberOfCustomersRated;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }
}
