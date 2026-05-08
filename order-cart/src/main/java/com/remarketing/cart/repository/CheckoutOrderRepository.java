package com.remarketing.cart.repository;

import com.remarketing.cart.entity.CheckoutOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckoutOrderRepository extends JpaRepository<CheckoutOrder, String> {
    List<CheckoutOrder> findByUserId(String userId);
}
