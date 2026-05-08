package com.remarketing.behavior.repository;

import com.remarketing.behavior.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, String> {
    List<UserActivity> findByUserIdOrderByTimestampDesc(String userId);
}
