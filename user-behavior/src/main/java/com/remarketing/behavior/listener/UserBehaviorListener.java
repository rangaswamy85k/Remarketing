package com.remarketing.behavior.listener;

import com.remarketing.core.event.UserSearchEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class UserBehaviorListener {

    @Async
    @EventListener
    public void handleUserSearchEvent(UserSearchEvent event) {
        System.out.println("Tracked search for User: " + event.getUserId() + ", Query: " + event.getSearchQuery());
        // Future: Save to SearchHistory table and maintain only top/last 5 searches per user.
    }
}
