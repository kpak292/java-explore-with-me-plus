package ru.practicum.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.Constants;
import ru.practicum.dal.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.enums.EventActionStateAdmin;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.dto.event.enums.RequestStatus;
import ru.practicum.dto.event.enums.SortingOptions;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.EventMapper;
import ru.practicum.mappers.EventUpdater;
import ru.practicum.mappers.RequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.Request;
import ru.practicum.model.User;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
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

    @Autowired
    RequestRepository requestRepository;

    @Override
    public EventDto save(long userId, NewEventDto newEventDto) {
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (newEventDto.getEventDate() != null && newEventDto.getEventDate().isBefore(validDate)) {
            throw new ValidationException("Event date should be after two hours after now");
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
            throw new ValidationException("Event date should be after two hours after now");
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
            throw new ValidationException("Event date should be after two hours after now");
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
            if (end.isBefore(start)) {
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

    @Transactional
    @Override
    public ParticipationRequestDto newRequest(long userId, long eventId) {
        if (requestRepository.findByUserIdAndEventId(userId, eventId).isEmpty()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));
            if (event.getInitiator().getId() == userId)
                throw new ConflictException("Event initiator can't make a request");
            if (event.getState() != EventState.PUBLISHED)
                throw new ConflictException("Event with id = " + eventId + " is not published yet");
            if (event.getParticipantLimit() <= event.getConfirmedRequests())
                throw new ConflictException("Limit of requests reached on event with id = " + event);
            Request request = new Request();
            request.setUserId(user);
            request.setEventId(event);
            request.setCreatedOn(LocalDateTime.now());
            if (event.getRequestModeration())
                request.setStatus(RequestStatus.PENDING);
            else
                request.setStatus(RequestStatus.CONFIRMED);
            return RequestMapper.INSTANCE.toParticipationRequestDto(requestRepository.save(request));
        } else
            throw new ConflictException("Request from user with id = " + userId +
                    " on event with id = " + eventId + " already exists");
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id = " + requestId + " not found"));
        if (request.getUserId().getId() != userId)
            throw new ConflictException("User with id = " + userId + " is not an initializer of request with id = " + requestId);
        requestRepository.delete(request);
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.INSTANCE.toParticipationRequestDto(request);
    }

    @Override
    public Collection<ParticipationRequestDto> findAllRequestsByUserId(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));
        Collection<ParticipationRequestDto> result = new ArrayList<>();
        result = requestRepository.findAllByUserId(userId).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList();
        return result;
    }

    @Override
    public Collection<ParticipationRequestDto> findAllRequestsByEventId(long userId, long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));
        Collection<ParticipationRequestDto> result = new ArrayList<>();
        result = requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList();
        return result;
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(long userId,
                                                               long eventId,
                                                               EventRequestStatusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));
        if (!event.getInitiator().equals(user))
            throw new ValidationException("User with id = " + userId + " is not a initiator of event with id = " + eventId);

        Collection<Request> requests = requestRepository.findAllRequestsOnEventByIds(eventId,
                RequestStatus.PENDING,
                request.getRequestIds());
        int limit = event.getParticipantLimit() - event.getConfirmedRequests().intValue();
        for (Request req : requests) {
            if (request.getStatus().equals(RequestStatus.REJECTED)) {
                req.setStatus(RequestStatus.REJECTED);
            } else if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                req.setStatus(RequestStatus.CONFIRMED);
            } else if (limit == 0) {
                req.setStatus(RequestStatus.REJECTED);
            } else {
                req.setStatus(RequestStatus.CONFIRMED);
                limit--;
            }
            requestRepository.save(req);
        }
        event.setConfirmedRequests((long) event.getParticipantLimit() - limit);
        eventRepository.save(event);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(requestRepository.findAllRequestsOnEventByIds(eventId,
                        RequestStatus.CONFIRMED,
                        request.getRequestIds()).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList());
        result.setRejectedRequests(requestRepository.findAllRequestsOnEventByIds(eventId,
                        RequestStatus.REJECTED,
                        request.getRequestIds()).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList());
        return result;
    }
}
