package ru.practicum.service;

import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;

import java.util.List;

public interface EventService {
    EventDto save(long userId, NewEventDto newEventDto);

    EventDto findEvent(long eventId, long userId);

    List<EventShortDto> findEvents(long userId);

    EventDto updateEvent(long eventId, long userId, UpdateEventUserRequest updateEventUserRequest);
}
