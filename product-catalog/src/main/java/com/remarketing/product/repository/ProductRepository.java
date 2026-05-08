package com.remarketing.product.repository;

import com.remarketing.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String> {
    List<ProductEntity> findByNameContainingIgnoreCase(String name);
    List<ProductEntity> findByCategoryIgnoreCase(String category);
}
