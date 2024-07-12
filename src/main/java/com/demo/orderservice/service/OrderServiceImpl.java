package com.demo.orderservice.service;

import com.demo.orderservice.entity.Order;
import com.demo.orderservice.model.OrderRequest;
import com.demo.orderservice.respository.OrderRespository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderRespository orderRespository;
    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Creating order.....");
        Order order=Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderTime(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();
        orderRespository.save(order);
        log.info("Order placed with order id : {}",order.getOrderId());
        return order.getOrderId();
    }
}
