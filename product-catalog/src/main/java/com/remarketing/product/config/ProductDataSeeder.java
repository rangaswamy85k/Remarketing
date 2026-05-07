package com.remarketing.product.config;

import com.remarketing.product.entity.ProductEntity;
import com.remarketing.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProductDataSeeder {

    @Bean
    CommandLineRunner initDatabase(ProductRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                ProductEntity p1 = new ProductEntity();
                p1.setName("MacBook Pro 14");
                p1.setPrice(1999.99);
                p1.setCategory("Laptops");

                ProductEntity p2 = new ProductEntity();
                p2.setName("iPhone 15 Pro");
                p2.setPrice(999.99);
                p2.setCategory("Phones");

                ProductEntity p3 = new ProductEntity();
                p3.setName("Sony WH-1000XM5");
                p3.setPrice(349.99);
                p3.setCategory("Headphones");

                ProductEntity p4 = new ProductEntity();
                p4.setName("Dell XPS 13");
                p4.setPrice(1299.99);
                p4.setCategory("Laptops");

                ProductEntity p5 = new ProductEntity();
                p5.setName("Samsung Galaxy S24");
                p5.setPrice(899.99);
                p5.setCategory("Phones");

                repository.saveAll(List.of(p1, p2, p3, p4, p5));
                System.out.println("Dummy product data seeded successfully!");
            }
        };
    }
}
