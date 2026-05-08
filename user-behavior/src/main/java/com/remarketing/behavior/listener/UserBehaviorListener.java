package com.remarketing.behavior.listener;

import com.remarketing.core.event.UserSearchEvent;
import com.remarketing.behavior.entity.UserActivity;
import com.remarketing.behavior.repository.UserActivityRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class UserBehaviorListener {

    private final UserActivityRepository userActivityRepository;

    public UserBehaviorListener(UserActivityRepository userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }

    @Async
    @EventListener
    public void handleUserSearchEvent(UserSearchEvent event) {
        System.out.println("Tracked search for User: " + event.getUserId() + ", Query: " + event.getSearchQuery());
        
        UserActivity activity = new UserActivity();
        activity.setUserId(event.getUserId());
        activity.setActivityType("SEARCH");
        activity.setDetails(event.getSearchQuery());
        
        userActivityRepository.save(activity);
    }
}
