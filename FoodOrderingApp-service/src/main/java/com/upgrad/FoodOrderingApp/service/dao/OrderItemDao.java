package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class OrderItemDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<OrderItemEntity> getItemsByOrder(OrderEntity orderEntity) {
        try {
            return entityManager.createNamedQuery("getItemsByOrders", OrderItemEntity.class).setParameter("orderEntity", orderEntity).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<OrderItemEntity> getOrderItemsByOrder(OrderEntity orderEntity) {
        try {
            List<OrderItemEntity> orderItemEntities = entityManager.createNamedQuery("getOrderItemByOrder", OrderItemEntity.class).setParameter("order", orderEntity).getResultList();
            return orderItemEntities;
        } catch (NoResultException nre) {
            return null;
        }
    }


}
