package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CouponNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.PaymentMethodNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private RestaurantService restaurantService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UtilityService utilityService;
    @Autowired
    private PaymentService paymentService;

    //Implementation of Get Coupon by Coupon Name - “/order/coupon/{coupon_name}” API
    @RequestMapping(path = "/order/coupon/{coupon_name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CouponDetailsResponse> getCouponByCouponName(@RequestHeader("authorization") final String authorization,  @PathVariable("coupon_name") final String couponName) throws AuthorizationFailedException, CouponNotFoundException {

        String accessToken = utilityService.splitAuthorization(authorization);
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        CouponEntity couponEntity = orderService.getCouponByCouponName(couponName);

        CouponDetailsResponse couponDetailsResponse = new CouponDetailsResponse()
                .couponName(couponEntity.getCouponName())
                .id(UUID.fromString(couponEntity.getUuid()))
                .percent(couponEntity.getPercent());

        return new ResponseEntity<CouponDetailsResponse>(couponDetailsResponse, HttpStatus.OK);
    }

    //Implementation of Get Past Orders of User - “/order” API
    @RequestMapping(method = RequestMethod.GET, path = "/order", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CustomerOrderResponse> getPastOrdersOfUser(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException, AuthorizationFailedException {//Access the accessToken from the request Header

        String accessToken = utilityService.splitAuthorization(authorization);
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        //Get all the past orders of the customer.
        List<OrderEntity> ordersEntities = orderService.getOrdersByCustomers(customerEntity.getUuid());

        //Creating List of OrderList
        List<OrderList> orderLists = new LinkedList<>();

        if (ordersEntities != null) {     //Checking if order entities is null if yes them empty list is returned
            for (OrderEntity ordersEntity : ordersEntities) {
                //Calls getOrderItemsByOrder by order of orderService get all the items ordered in past by orders.
                List<OrderItemEntity> orderItemEntities = orderService.getOrderItemsByOrder(ordersEntity);

                //Creating ItemQuantitiesResponse List
                List<ItemQuantityResponse> itemQuantityResponseList = new LinkedList<>();

                //Loop through each item to get details
                for ( OrderItemEntity orderItemEntity:orderItemEntities ) {
                    //Creating new ItemQuantityResponseItem
                    ItemQuantityResponseItem itemQuantityResponseItem = new ItemQuantityResponseItem()
                            .itemName(orderItemEntity.getItemId().getItemName())
                            .itemPrice(orderItemEntity.getItemId().getPrice())
                            .id(UUID.fromString(orderItemEntity.getItemId().getUuid()))
                            .type(ItemQuantityResponseItem.TypeEnum.valueOf(orderItemEntity.getItemId().getType().getValue()));
                    //Creating ItemQuantityResponse which will be added to the list
                    ItemQuantityResponse itemQuantityResponse = new ItemQuantityResponse()
                            .item(itemQuantityResponseItem)
                            .quantity(orderItemEntity.getQuantity())
                            .price(orderItemEntity.getPrice());
                    itemQuantityResponseList.add(itemQuantityResponse);
                }

                //Creating OrderListAddressState to add in the address
                OrderListAddressState orderListAddressState = new OrderListAddressState()
                        .id(UUID.fromString(ordersEntity.getAddress().getStateId().getUuid()))
                        .stateName(ordersEntity.getAddress().getStateId().getStateName());

                //Creating OrderListAddress to add address to the orderList
                OrderListAddress orderListAddress = new OrderListAddress()
                        .id(UUID.fromString(ordersEntity.getAddress().getUuid()))
                        .flatBuildingName(ordersEntity.getAddress().getFlatBuildingNumber())
                        .locality(ordersEntity.getAddress().getLocality())
                        .city(ordersEntity.getAddress().getCity())
                        .pincode(ordersEntity.getAddress().getPincode())
                        .state(orderListAddressState);

                //Creating OrderListCoupon to add Coupon to the orderList
                OrderListCoupon orderListCoupon = new OrderListCoupon()
                        .couponName(ordersEntity.getCoupon().getCouponName())
                        .id(UUID.fromString(ordersEntity.getCoupon().getUuid()))
                        .percent(ordersEntity.getCoupon().getPercent());

                //Creating OrderListCustomer to add Customer to the orderList
                OrderListCustomer orderListCustomer = new OrderListCustomer()
                        .id(UUID.fromString(ordersEntity.getCustomer().getUuid()))
                        .firstName(ordersEntity.getCustomer().getFirstName())
                        .lastName(ordersEntity.getCustomer().getLastName())
                        .emailAddress(ordersEntity.getCustomer().getEmail())
                        .contactNumber(ordersEntity.getCustomer().getContactNumber());

                //Creating OrderListPayment to add Payment to the orderList
                OrderListPayment orderListPayment = new OrderListPayment()
                        .id(UUID.fromString(ordersEntity.getPayment().getUuid()))
                        .paymentName(ordersEntity.getPayment().getPaymentName());

                //Craeting orderList to add all the above info and then add it orderLists to finally add it to CustomerOrderResponse
                OrderList orderList = new OrderList()
                        .id(UUID.fromString(ordersEntity.getUuid()))
                        .itemQuantities(itemQuantityResponseList)
                        .address(orderListAddress)
                        .bill(BigDecimal.valueOf(ordersEntity.getBill()))
                        .date(String.valueOf(ordersEntity.getDate()))
                        .discount(BigDecimal.valueOf(ordersEntity.getDiscount()))
                        .coupon(orderListCoupon)
                        .customer(orderListCustomer)
                        .payment(orderListPayment);
                orderLists.add(orderList);
            }

            //Creating CustomerOrderResponse by adding OrderLists to it
            CustomerOrderResponse customerOrderResponse = new CustomerOrderResponse()
                    .orders(orderLists);
            return new ResponseEntity<CustomerOrderResponse>(customerOrderResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<CustomerOrderResponse>(new CustomerOrderResponse(), HttpStatus.OK);//If no order created by customer empty array is returned.
        }
    }

    //Implementation of Save Order - “/order” API
    @RequestMapping(method = RequestMethod.POST, path = "/order", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveOrderResponse> saveOrder(@RequestHeader("authorization") final String authorization,
                                                       @RequestBody(required = false) final SaveOrderRequest saveOrderRequest) throws Exception {
        String[] authParts = authorization.split("Bearer ");
        if(saveOrderRequest.getPaymentId() == null ){
            throw new PaymentMethodNotFoundException("PNF-002", "No payment method found by this id");
        }
        final String accessToken = authParts[1];

        OrderEntity orderEntity = new OrderEntity();

        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        CouponEntity couponEntity = orderService.getCouponByCouponId(saveOrderRequest.getCouponId().toString());
        PaymentEntity paymentEntity = paymentService.getPaymentByUUID(saveOrderRequest.getPaymentId().toString());
        AddressEntity addressEntity = addressService.getAddressByUUID(saveOrderRequest.getAddressId(), customerEntity);
        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(saveOrderRequest.getRestaurantId().toString());

        orderEntity.setBill(saveOrderRequest.getBill().doubleValue());
        orderEntity.setUuid(UUID.randomUUID().toString());
        orderEntity.setDiscount(saveOrderRequest.getDiscount().doubleValue());
        orderEntity.setCoupon(couponEntity);
        orderEntity.setCustomer(customerEntity);
        orderEntity.setDate(new Timestamp(System.currentTimeMillis()));
        orderEntity.setRestaurant(restaurantEntity);
        orderEntity.setPayment(paymentEntity);
        orderEntity.setAddress(addressEntity);

        OrderEntity updatedOrderEntity = orderService.saveOrder(orderEntity);

        List<ItemQuantity> itemQuantities = saveOrderRequest.getItemQuantities();
        for (ItemQuantity i : itemQuantities) {

            OrderItemEntity orderItemEntity = new OrderItemEntity();

            ItemEntity itemEntity = itemService.getItemByUUID(i.getItemId().toString());

            orderItemEntity.setItemId(itemEntity);
            orderItemEntity.setOrderId(orderEntity);
            orderItemEntity.setPrice(i.getPrice());
            orderItemEntity.setQuantity(i.getQuantity());

            OrderItemEntity savedOrderItem = orderService.saveOrderItem(orderItemEntity);
        }
        SaveOrderResponse response = new SaveOrderResponse().id(updatedOrderEntity.getUuid()).status("ORDER SUCCESSFULLY PLACED");
        return new ResponseEntity<SaveOrderResponse>(response, HttpStatus.CREATED);
    }


}
