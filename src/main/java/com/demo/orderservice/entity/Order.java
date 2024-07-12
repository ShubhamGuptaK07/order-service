package com.demo.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "Order_Details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long orderId;
    @Column(name = "PRODUCT_ID")
    private long productId;
    @Column(name = "QUANTITY")
    private long quantity;
    @Column(name = "ORDER_TIME")
    private Instant orderTime;
    @Column(name = "ORDER_STATUS")
    private String orderStatus;
    @Column(name = "AMOUNT")
    private long amount;

}
