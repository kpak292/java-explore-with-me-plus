package ru.practicum.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dal.CategoryRepository;
import ru.practicum.dal.EventRepository;
import ru.practicum.dal.LocationRepository;
import ru.practicum.dal.UserRepository;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventState;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mappers.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;

@Service
public class EventServiceImpl implements EventService {
    @Autowired
    LocationRepository locationRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Override
    public EventDto save(long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));
        Event event = EventMapper.INSTANCE.getEvent(newEventDto);

        event.setInitiator(initiator);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        return EventMapper.INSTANCE.getEventDto(eventRepository.save(event));
    }
}
