package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.TestDto;
import ru.practicum.entity.Test;

@Mapper
public interface TestMapper {
    TestMapper INSTANCE = Mappers.getMapper(TestMapper.class);

    Test getTest(TestDto testDto);

    TestDto getTestDto(Test test);
}
