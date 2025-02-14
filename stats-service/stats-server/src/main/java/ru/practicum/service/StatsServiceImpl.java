package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.Constants;
import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;
import ru.practicum.dal.StatRepository;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.StatsMapper;
import ru.practicum.model.StatItem;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatRepository statRepository;

    @Override
    public StatsHitDto saveHit(StatsHitDto hitDto) {
        StatItem statItem = statRepository.save(StatsMapper.INSTANCE.getStatItem(hitDto));

        return StatsMapper.INSTANCE.getStatsHitDto(statItem);
    }

    @Override
    public Collection<StatsViewDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        if (start == null || end == null) {
            throw new ValidationException("start and end parameters is mandatory");
        }

        LocalDateTime startDate;
        LocalDateTime endDate;

        try {
            startDate = LocalDateTime.parse(start,
                    Constants.DATE_TIME_FORMATTER);

            endDate = LocalDateTime.parse(end,
                    Constants.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            throw new ValidationException("incorrect date format. expected format = " + Constants.DATE_PATTERN);
        }


        Collection<StatItem> stats;
        if (uris != null) {
            stats = statRepository.getStatsbyDatesAndUris(uris, startDate, endDate);
        } else {
            stats = statRepository.getStatsbyDates(startDate, endDate);
        }
        if (!stats.isEmpty()) {
            return parseResult(stats, unique);
        } else {
            return new ArrayList<>();
        }
    }

    private Collection<StatsViewDto> parseResult(Collection<StatItem> items, Boolean unique) {
        List<StatsViewDto> result = new ArrayList<>();
        List<String> uniqueUris = items.stream()
                .map(StatItem::getUri)
                .distinct()
                .toList();
        if (unique) {
            for (int i = 0; i < uniqueUris.size(); i++) {
                int finalI = i;
                Collection<StatItem> uniqueUri = items.stream()
                        .filter(statItem -> statItem.getUri().equals(uniqueUris.get(finalI)))
                        .toList();
                result.add(StatsMapper.INSTANCE.getStatsViewDto(uniqueUri.stream().findFirst().get(),
                        (int) items.stream()
                                .filter(statItem -> statItem.getUri().equals(uniqueUris.get(finalI)))
                                .map(StatItem::getIp)
                                .distinct()
                                .count()));
            }
        } else {
            for (int i = 0; i < uniqueUris.size(); i++) {
                int finalI = i;
                Collection<StatItem> uniqueUri = items.stream()
                        .filter(statItem -> statItem.getUri().equals(uniqueUris.get(finalI)))
                        .toList();
                result.add(StatsMapper.INSTANCE.getStatsViewDto(uniqueUri.stream().findFirst().get(), uniqueUri.size()));
            }
        }
        result = result.stream().sorted(Comparator.comparingInt(StatsViewDto::getHits).reversed())
                .toList();
        return result;
    }


}
