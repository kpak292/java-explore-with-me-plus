package ru.practicum.controller.privateapi;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.service.EventService;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventController {
    @Autowired
    EventService eventService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto addCategory(@PathVariable long userId,
                                @RequestBody @Valid NewEventDto newEventDto) {
        log.info("adding new event with body: {}", newEventDto);
        return eventService.save(userId, newEventDto);
    }
}
