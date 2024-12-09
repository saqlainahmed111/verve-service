package com.example.verve.controller;

import com.example.verve.service.UniqueRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/verve")
public class UniqueRequestController {

    @Autowired
    private UniqueRequestService service;

    @GetMapping("/accept")
    public Mono<ResponseEntity<String>> acceptRequest(
            @RequestParam(name = "id") Integer id,
            @RequestParam(name = "endpoint", required = false) String endpoint
    ) {
        return service.handleRequest(id, endpoint)
                .map(ResponseEntity::ok);
    }
}
