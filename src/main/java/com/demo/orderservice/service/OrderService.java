package com.demo.orderservice.service;

import com.demo.orderservice.model.OrderRequest;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);
}
