package com.example.demo;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

@Log4j2
@EnableScheduling
@SpringBootApplication
public class DemoApplication {

    private final Random random = new Random();

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @Scheduled(fixedDelay = 1500)
    public void jobLog() {
        
        int r = random.nextInt(100, 1000);
        for (int i = 0; i < r; i++) {
            log.info("Hello from demo service {} out of {}", i, r);
        }
    }

    @Autowired
    MeterRegistry registry;

    private final AtomicInteger randomValue = new AtomicInteger(0);

    @PostConstruct 
    public void initMetric() {

        Gauge.builder("demo_random_value", randomValue, AtomicInteger::get)
             .description("Randomly generated value, updated every 5s")
             .register(registry);

    }

    @Scheduled(fixedDelay = 1000)
    public void jobMetric() {
        
        int r = random.nextInt(1, 100);
        randomValue.set(r);
    }
} 
