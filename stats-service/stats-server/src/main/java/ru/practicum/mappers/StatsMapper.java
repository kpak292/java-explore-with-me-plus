package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;
import ru.practicum.model.StatItem;

@Mapper
public interface StatsMapper {
    StatsMapper INSTANCE = Mappers.getMapper(StatsMapper.class);

    @Mapping(target = "created", source = "timestamp")
    StatItem getStatItem(StatsHitDto statsHitDto);

    @Mapping(target = "timestamp", source = "created")
    StatsHitDto getStatsHitDto(StatItem statItem);

    StatsViewDto getStatsViewDto(StatItem statItem, int hits);
}
