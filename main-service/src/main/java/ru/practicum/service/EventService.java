package ru.practicum.service;

import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.NewEventDto;

public interface EventService {
    EventDto save(long userId, NewEventDto newEventDto);

}
