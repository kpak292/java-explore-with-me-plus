package ru.practicum.mappers;

import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;
import ru.practicum.model.StatItem;

public class StatMapper {

    public static StatItem mapToItem(StatsHitDto hitDto) {
        StatItem item = new StatItem();
        item.setIp(hitDto.getIp());
        item.setUri(hitDto.getUri());
        item.setApp(hitDto.getApp());
        item.setCreated(hitDto.getTimestamp());
        return item;
    }

    public static StatsViewDto mapToViewDto(StatItem item, int hits) {
        StatsViewDto viewDto = new StatsViewDto();
        viewDto.setApp(item.getApp());
        viewDto.setUri(item.getUri());
        viewDto.setHits(hits);
        return viewDto;
    }
}
