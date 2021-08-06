package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CouponDao {

    @PersistenceContext
    private EntityManager entityManager;

    public CouponEntity getCouponByName(final String couponName) {
        try {
            return entityManager.createNamedQuery("getCouponByCouponName", CouponEntity.class).setParameter("couponName", couponName).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public CouponEntity getCouponByUUID(final String couponUuid) {
        try {
            return entityManager.createNamedQuery("getCouponByCouponUuid", CouponEntity.class).setParameter("couponUuid", couponUuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}