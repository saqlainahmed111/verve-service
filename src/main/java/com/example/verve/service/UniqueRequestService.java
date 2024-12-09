package com.example.verve.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UniqueRequestService {
    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    private AtomicInteger uniqueRequestCount = new AtomicInteger();

    public Mono<String> handleRequest(Integer id, String endpoint) {
        return redisTemplate.opsForSet().add("unique-requests", id.toString())
                .flatMap(added -> {
                    if (added == 1) { // Unique request
                        uniqueRequestCount.incrementAndGet();
                    }

                    if (endpoint != null) {
                        // Send POST request with payload
                        return sendPostRequest(endpoint, uniqueRequestCount.get())
                                .map(response -> "ok");
                    }
                    return Mono.just("ok");
                });
    }

    private Mono<String> sendPostRequest(String endpoint, int count) {
        return org.springframework.web.reactive.function.client.WebClient.create()
                .post()
                .uri(endpoint)
                .bodyValue("{\"uniqueCount\": " + count + "}")
                .retrieve()
                .bodyToMono(String.class);
    }

    public void writeUniqueRequestCountToFile() {
        try (FileWriter writer = new FileWriter("unique_requests.log", true)) {
            writer.write("Unique count this minute: " + uniqueRequestCount.get() + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        uniqueRequestCount.set(0);
    }
}
