package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.PaymentListResponse;
import com.upgrad.FoodOrderingApp.api.model.PaymentResponse;
import com.upgrad.FoodOrderingApp.service.businness.PaymentService;
import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    //Implementation of Get Payment Methods - “/payment” API
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/payment", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<PaymentListResponse> getPaymentMethods() {

        //Calls getAllPaymentMethods of paymentService to get all the paymentEntity in DB
        List<PaymentEntity> listOfPaymentEntities = paymentService.getAllPaymentMethods();

        //Creating PaymentListResponse by adding list of PaymentResponse to it
        PaymentListResponse paymentListResponse = new PaymentListResponse();

        for (PaymentEntity paymentEntity : listOfPaymentEntities) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setId(UUID.fromString(paymentEntity.getUuid()));
            paymentResponse.setPaymentName(paymentEntity.getPaymentName());
            paymentListResponse.addPaymentMethodsItem(paymentResponse);
        }
        return new ResponseEntity<PaymentListResponse>(paymentListResponse, HttpStatus.OK);
    }
}
