package ru.practicum.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.Constants;
import ru.practicum.dal.CategoryRepository;
import ru.practicum.dal.EventRepository;
import ru.practicum.dal.LocationRepository;
import ru.practicum.dal.UserRepository;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.enums.EventActionStateAdmin;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.dto.event.enums.SortingOptions;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.EventMapper;
import ru.practicum.mappers.EventUpdater;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

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
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (newEventDto.getEventDate() != null && newEventDto.getEventDate().isBefore(validDate)) {
            throw new ConflictException("Event date should be after two hours after now");
        }

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));

        Event event = EventMapper.INSTANCE.getEvent(newEventDto);

        Location location = locationRepository.save(event.getLocation());

        event.setInitiator(initiator);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(location);

        return EventMapper.INSTANCE.getEventDto(eventRepository.save(event));
    }

    @Override
    public EventDto findEvent(long eventId, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));

        return EventMapper.INSTANCE.getEventDto(
                eventRepository.findByIdAndUserId(eventId, userId)
                        .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId))
        );
    }

    @Override
    public List<EventShortDto> findEvents(long userId, int from, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));

        Pageable pageable = PageRequest.of(from, size);

        return eventRepository.findByUserId(userId, pageable).stream()
                .map(EventMapper.INSTANCE::getEventShortDto)
                .toList();
    }

    @Transactional
    @Override
    public EventDto updateEvent(long eventId, long userId, UpdateEventUserRequest updateEventUserRequest) {
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (updateEventUserRequest.getEventDate() != null && updateEventUserRequest.getEventDate().isBefore(validDate)) {
            throw new ConflictException("Event date should be after two hours after now");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));

        Event baseEvent = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));

        if (baseEvent.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot updated published event");
        }

        EventUpdater.INSTANCE.update(baseEvent, updateEventUserRequest);

        return EventMapper.INSTANCE.getEventDto(baseEvent);
    }

    @Transactional
    @Override
    public EventDto updateEventAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (updateEventAdminRequest.getEventDate() != null && updateEventAdminRequest.getEventDate().isBefore(validDate)) {
            throw new ConflictException("Event date should be after two hours after now");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));

        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction() == EventActionStateAdmin.REJECT_EVENT &&
                    event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Cannot cancel published event");
            }

            if (updateEventAdminRequest.getStateAction() == EventActionStateAdmin.PUBLISH_EVENT &&
                    event.getState() != EventState.PENDING) {
                throw new ConflictException("Cannot publish event not in status Pending");
            }

            if (updateEventAdminRequest.getStateAction() == EventActionStateAdmin.PUBLISH_EVENT &&
                    event.getEventDate().minusHours(1L).isBefore(LocalDateTime.now())) {
                throw new ConflictException("Cannot publish event less than 1 hour before start");
            }
        }

        EventUpdater.INSTANCE.update(event, updateEventAdminRequest);

        if (event.getState() == EventState.PUBLISHED) {
            event.setPublishedOn(LocalDateTime.now());
            event.setConfirmedRequests(0L);
            event.setViews(0L);
        }
        return EventMapper.INSTANCE.getEventDto(event);
    }

    @Override
    public List<EventDto> findEventsByFilter(List<Long> users,
                                             List<String> states,
                                             List<Long> categories,
                                             String rangeStart,
                                             String rangeEnd,
                                             int from,
                                             int size) {
        Pageable pageable = PageRequest.of(from, size);

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (rangeStart != null) {
            start = LocalDateTime.parse(rangeStart, Constants.DATE_TIME_FORMATTER);
        }

        if (rangeEnd != null) {
            end = LocalDateTime.parse(rangeEnd, Constants.DATE_TIME_FORMATTER);
        }

        return eventRepository.findAllByFilter(users, states, categories, start, end, pageable).stream()
                .map(EventMapper.INSTANCE::getEventDto)
                .toList();
    }

    @Override
    public List<EventShortDto> findEventsByFilterPublic(String text, List<Long> categories, Boolean paid,
                                                        String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                                        SortingOptions sortingOptions, int from, int size) {
        Pageable pageable;
        if (sortingOptions != null) {
            String sort = sortingOptions == SortingOptions.EVENT_DATE ? "eventDate" : "views";
            pageable = PageRequest.of(from, size, Sort.by(sort).descending());
        } else {
            pageable = PageRequest.of(from, size);
        }

        LocalDateTime start;
        LocalDateTime end = null;

        if (rangeStart != null) {
            start = LocalDateTime.parse(rangeStart, Constants.DATE_TIME_FORMATTER);
        } else {
            start = LocalDateTime.now();
        }

        if (rangeEnd != null) {
            end = LocalDateTime.parse(rangeEnd, Constants.DATE_TIME_FORMATTER);
            if (end.isBefore(start)){
                throw new ValidationException("End is before start");
            }
        }

        //todo добавить отправку запроса в сервис статистики
        //todo добавить количество одобренных заявок

        return eventRepository.findAllByFilterPublic(text, categories, paid, start, end, onlyAvailable,
                        EventState.PUBLISHED, pageable).stream()
                .map(EventMapper.INSTANCE::getEventShortDto)
                .toList();
    }

    @Override
    public EventDto findEventPublic(long eventId) {
        Event baseEvent = eventRepository.findByIdAndStatus(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("published event is not found with id = " + eventId));
        //todo добавить отправку запроса в сервис статистики
        //todo добавить количество одобренных заявок
        return EventMapper.INSTANCE.getEventDto(baseEvent);
    }
}
