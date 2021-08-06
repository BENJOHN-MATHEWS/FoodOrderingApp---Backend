package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.PaymentDao;
import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import com.upgrad.FoodOrderingApp.service.exception.PaymentMethodNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {
    @Autowired
    private PaymentDao paymentDao;
    @Autowired
    private UtilityService utilityService;

    public List<PaymentEntity> getAllPaymentMethods() {
        return paymentDao.getPaymentMethods();
    }

    public PaymentEntity getPaymentByUUID(final String paymentUuid) throws PaymentMethodNotFoundException {
        PaymentEntity paymentEntity = paymentDao.getPaymentMethodByUUID(paymentUuid);
        if (paymentEntity == null || paymentUuid == null) {
            throw new PaymentMethodNotFoundException("PNF-002", "No payment method found by this id");
        }
        return paymentEntity;
    }

}
