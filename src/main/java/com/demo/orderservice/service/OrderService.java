package com.demo.orderservice.service;

import com.demo.orderservice.model.OrderRequest;
import com.demo.orderservice.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
