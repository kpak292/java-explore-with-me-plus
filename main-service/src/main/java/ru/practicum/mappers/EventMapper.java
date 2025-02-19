package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    Event getEvent(NewEventDto newEventDto);

    EventDto getEventDto(Event event);
}
