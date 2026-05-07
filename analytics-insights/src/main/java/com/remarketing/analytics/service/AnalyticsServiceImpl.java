package com.remarketing.analytics.service;

import org.springframework.stereotype.Service;

@Service
public class AnalyticsServiceImpl {
    
    public double calculateROI(double revenue, double cost) {
        if (cost == 0) return 0.0;
        return ((revenue - cost) / cost) * 100;
    }
    
    public double calculateConversionRate(int conversions, int totalUsers) {
        if (totalUsers == 0) return 0.0;
        return ((double) conversions / totalUsers) * 100;
    }
}
