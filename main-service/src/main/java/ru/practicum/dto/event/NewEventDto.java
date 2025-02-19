package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.Constants;
import ru.practicum.dto.category.CategoryDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NewEventDto {
    @Size(max = 100)
    @NotBlank
    String annotation;

    CategoryDto category;

    @Size(max = 255)
    @NotBlank
    String description;

    @JsonFormat(pattern = Constants.DATE_PATTERN)
    LocalDateTime eventDate;

    @NotNull
    LocationDto location;

    Boolean paid;

    @PositiveOrZero
    Integer participantLimit;

    Boolean requestModeration;

    @Size(min = 3, max = 120)
    @NotBlank
    String title;
}
