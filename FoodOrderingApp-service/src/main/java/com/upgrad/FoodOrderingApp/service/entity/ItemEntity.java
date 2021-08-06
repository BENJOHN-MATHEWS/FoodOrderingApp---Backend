package com.upgrad.FoodOrderingApp.service.entity;

import com.upgrad.FoodOrderingApp.service.common.ItemType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="item")
@NamedQueries({
        @NamedQuery(name = "getItemsByUuid", query = "select i from ItemEntity i where i.uuid =:uuid"),
        @NamedQuery(name = "getAllItems", query = "select i from ItemEntity i "),
        @NamedQuery(name = "itemByItemId", query = "select i from ItemEntity i where i.id =:id"),
})
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="uuid")
    @Size(max=200)
    @NotNull
    private String uuid;

    @Column(name="item_name")
    @Size(max=30)
    @NotNull
    private String itemName;

    @NotNull
    @Column(name="price")
    private Integer price;

    @Column(name="type")
    @Size(max=10)
    @NotNull
    private ItemType type;

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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }
}
