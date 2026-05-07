package com.remarketing.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
public class ProductEntity {
    @Id
    private String id = UUID.randomUUID().toString();
    
    private String name;
    private double price;
    private String category;
}
