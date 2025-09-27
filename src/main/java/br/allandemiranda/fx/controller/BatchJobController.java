package br.allandemiranda.fx.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/batch")
public interface BatchJobController {

    @GetMapping
    ResponseEntity<Void> runAllJobsWithDefaultInputs();
}
