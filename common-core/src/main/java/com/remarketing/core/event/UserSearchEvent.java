package com.remarketing.core.event;

import org.springframework.context.ApplicationEvent;

public class UserSearchEvent extends ApplicationEvent {
    private final String userId;
    private final String searchQuery;

    public UserSearchEvent(Object source, String userId, String searchQuery) {
        super(source);
        this.userId = userId;
        this.searchQuery = searchQuery;
    }

    public String getUserId() { return userId; }
    public String getSearchQuery() { return searchQuery; }
}
