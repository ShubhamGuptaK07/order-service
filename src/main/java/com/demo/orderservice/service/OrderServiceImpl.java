package com.demo.orderservice.service;

import brave.messaging.ProducerResponse;
import com.demo.orderservice.entity.Order;
import com.demo.orderservice.exception.CustomException;
import com.demo.orderservice.external.client.PaymentService;
import com.demo.orderservice.external.client.ProductService;
import com.demo.orderservice.external.request.PaymentRequest;
import com.demo.orderservice.model.OrderRequest;
import com.demo.orderservice.model.OrderResponse;
import com.demo.orderservice.respository.OrderRespository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    @Autowired
    private RestTemplate restTemplate;

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
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getOrderId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .referenceNumber(String.valueOf(UUID.randomUUID()))
                .build();
        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done successfully. Changing the order status ");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error occurred in payment. Changing status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }
        order.setOrderStatus(orderStatus);
        orderRespository.save(order);
        log.info("Order placed with order id : {}", order.getOrderId());
        return order.getOrderId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get Order placed details for order id : {}", orderId);
        Order order = orderRespository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not Found for the id: " + orderId, "ORDER_NOT_FOUND", 404));

        log.info("Invoking the Product Service to fetch the product details for order id : {}", order.getOrderId());
        OrderResponse.ProductDetails productDetails = restTemplate.getForObject(
                "http://PRODUCT-SERVICE/product/" + order.getProductId(),
                OrderResponse.ProductDetails.class
        );
        if (productDetails != null) {
            productDetails.setQuantity(order.getQuantity());
        }
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderTime())
                .amount(order.getAmount())
                .productDetails(productDetails)
                .build();
    }
}
