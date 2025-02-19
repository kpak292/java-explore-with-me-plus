package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.Constants;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EventDto {
    Long id;
    String annotation;
    CategoryDto category;
    Long confirmedRequests;

    @JsonFormat(pattern = Constants.DATE_PATTERN)
    LocalDateTime createdOn;
    String description;

    @JsonFormat(pattern = Constants.DATE_PATTERN)
    LocalDateTime eventDate;
    UserShortDto initiator;
    LocationDto location;
    Boolean paid;
    Integer participantLimit;

    @JsonFormat(pattern = Constants.DATE_PATTERN)
    LocalDateTime publishedOn;
    Boolean requestModeration;
    EventState state;
    String title;
    Long views;
}
