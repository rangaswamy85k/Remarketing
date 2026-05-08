package com.remarketing.product.repository;

import com.remarketing.product.entity.UserSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSearchRepository extends JpaRepository<UserSearch, String> {
    Optional<UserSearch> findByUserId(String userId);
    List<UserSearch> findByCategoryIgnoreCase(String category);
}
