package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.TestDto;
import ru.practicum.service.TestServiceImpl;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/test")
public class TestController {
    @Autowired
    TestServiceImpl testService;

    @GetMapping
    public ResponseEntity<Collection<TestDto>> getAll() {
        return new ResponseEntity<>(
                testService.getAll(),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ResponseEntity<TestDto> create(@RequestBody TestDto testDto) {
        return new ResponseEntity<>(
                testService.create(testDto),
                HttpStatus.CREATED
        );
    }
}
