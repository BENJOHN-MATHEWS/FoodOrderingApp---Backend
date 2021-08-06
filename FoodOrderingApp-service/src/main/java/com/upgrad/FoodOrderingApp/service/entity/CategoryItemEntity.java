package com.upgrad.FoodOrderingApp.service.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name="category_item")
@NamedQueries({

        @NamedQuery(name = "getItemsByCategory", query = "SELECT c FROM CategoryItemEntity c WHERE c.categoryId = :category ORDER BY LOWER(c.itemId.itemName) ASC "),
})
public class CategoryItemEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="item_id")
    @NotNull
    private ItemEntity itemId;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="category_id")
    @NotNull
    private CategoryEntity categoryId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ItemEntity getItemId() {
        return itemId;
    }

    public void setItemId(ItemEntity itemId) {
        this.itemId = itemId;
    }

    public CategoryEntity getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(CategoryEntity categoryId) {
        this.categoryId = categoryId;
    }

}
