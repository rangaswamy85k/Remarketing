package com.remarketing.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "user_searches")
@Data
public class UserSearch {
    @Id
    private String id = UUID.randomUUID().toString();
    
    @Column(unique = true)
    private String userId;
    
    private String category; // Latest category searched
}
