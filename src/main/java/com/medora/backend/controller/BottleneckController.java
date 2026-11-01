package com.medora.backend.controller;

import com.medora.backend.entity.Bottleneck;
import com.medora.backend.repository.BottleneckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bottlenecks")
public class BottleneckController {

    @Autowired
    private BottleneckRepository bottleneckRepository;

    @GetMapping
    public ResponseEntity<List<Bottleneck>> getAllBottlenecks() {
        return ResponseEntity.ok(bottleneckRepository.findAll());
    }
}
