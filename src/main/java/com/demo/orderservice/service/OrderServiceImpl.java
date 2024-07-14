package com.demo.orderservice.service;

import com.demo.orderservice.entity.Order;
import com.demo.orderservice.external.client.PaymentService;
import com.demo.orderservice.external.client.ProductService;
import com.demo.orderservice.external.request.PaymentRequest;
import com.demo.orderservice.model.OrderRequest;
import com.demo.orderservice.respository.OrderRespository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRespository orderRespository;
    @Autowired
    private ProductService productService;
    @Autowired
    private PaymentService paymentService;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Creating order.....");

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());
        log.info("CREATED Order with status created");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderTime(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();
        orderRespository.save(order);
        log.info("Calling Payment service to complete the payment");
        PaymentRequest  paymentRequest=PaymentRequest.builder()
                .orderId(order.getOrderId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .referenceNumber(String.valueOf(UUID.randomUUID()))
                .build();
        String orderStatus=null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done successfully. Changing the order status ");
            orderStatus="PLACED";
        }catch (Exception e){
            log.error("Error occured in payment. Changing status to PAYMENT_FAILED");
            orderStatus="PAYMENT_FAILED";
        }
        order.setOrderStatus(orderStatus);
        orderRespository.save(order);
        log.info("Order placed with order id : {}", order.getOrderId());
        return order.getOrderId();
    }
}
