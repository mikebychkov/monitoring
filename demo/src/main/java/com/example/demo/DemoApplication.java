package com.example.demo;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

@Log4j2
@EnableScheduling
@SpringBootApplication
public class DemoApplication {

    @Autowired
    MeterRegistry registry;

    private final Random random = new Random();
    private final AtomicInteger randomValue = new AtomicInteger(0);

    private Counter jobCounter;
    private Timer jobTimer;
    private DistributionSummary batchSummary;
    private LongTaskTimer longTaskTimer;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @PostConstruct 
    public void initMetric() {

        Gauge.builder("demo_gauge_random_value", randomValue, AtomicInteger::get)
             .description("Randomly generated value, updated every 5s")
             .register(registry);

        jobCounter = Counter.builder("demo_counter_jobs_total")
                .description("Total number of jobs processed")
                .tag("type", "random")
                .register(registry);

        jobTimer = Timer.builder("demo_timer_job_duration")
                .description("Duration of simulated jobs")
                .publishPercentileHistogram() // enable histogram for Prometheus
                .register(registry);

        batchSummary = DistributionSummary.builder("demo_distr_batch_size")
                .description("Size of processed batches")
                .baseUnit("items")
                .publishPercentileHistogram()
                .register(registry);

        longTaskTimer = registry.more().longTaskTimer("demo_long_task");
    }

    @Scheduled(fixedDelay = 1000)
    public void jobMetric() {
        
        int r = random.nextInt(1, 100);
        randomValue.set(r);

        jobCounter.increment(); 
    }

    @Scheduled(fixedRate = 7000) // every 7s
    public void simulateJob() {
        jobTimer.record(() -> {
            try {
                Thread.sleep(200 + random.nextInt(300)); // 200–500 ms
            } catch (InterruptedException ignored) {}
        });
    }

    @Scheduled(fixedRate = 4000) // every 4s
    public void recordBatch() {
        int batchSize = random.nextInt(50) + 1; // 1–50
        batchSummary.record(batchSize);
        System.out.println("Recorded batch size: " + batchSize);
    }

    @Scheduled(fixedRate = 15000) // every 15s
    public void runLongTask() {
        LongTaskTimer.Sample sample = longTaskTimer.start();
        try {
            Thread.sleep(5000); // simulate 5s work
        } catch (InterruptedException ignored) {}
        finally {
            sample.stop();
            System.out.println("Recorded long task duration");
        }
    }
} 
