package com.remarketing.abtesting.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class ABTestServiceImpl {
    public String assignVariant(String userId, String testId) {
        // Simple A/B test variant assignment logic
        return new Random().nextBoolean() ? "Variant A" : "Variant B";
    }
}
