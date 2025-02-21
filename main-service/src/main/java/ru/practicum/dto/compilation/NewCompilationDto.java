package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NewCompilationDto {
    List<Long> events;

    //default value
    Boolean pinned = false;

    @Size(max = 50)
    @NotBlank
    @NotNull
    String title;
}
