package com.remarketing.cart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "checkout_orders")
@Data
public class CheckoutOrder {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CheckoutItem> items = new ArrayList<>();
    
    public void addItem(CheckoutItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
