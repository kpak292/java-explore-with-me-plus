package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dal.StatRepository;
import ru.practicum.mappers.StatMapper;
import ru.practicum.model.StatItem;

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
    public void saveHit(StatsHitDto hitDto) {
        statRepository.save(StatMapper.mapToItem(hitDto));
    }

    @Override
    public Collection<StatsViewDto> getStats(String start, String end, ArrayList<String> uris, Boolean unique) {
        Collection<StatItem> stats;
        if (uris != null) {
            stats = statRepository.getStatsbyDatesAndUris(uris, LocalDateTime.parse(start, Constants.DATE_TIME_FORMATTER), LocalDateTime.parse(end, Constants.DATE_TIME_FORMATTER));
        } else {
            stats = statRepository.getStatsbyDates(LocalDateTime.parse(start, Constants.DATE_TIME_FORMATTER), LocalDateTime.parse(end, Constants.DATE_TIME_FORMATTER));
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
                result.add(StatMapper.mapToViewDto(uniqueUri.stream().findFirst().get(),
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
                result.add(StatMapper.mapToViewDto(uniqueUri.stream().findFirst().get(), uniqueUri.size()));
            }
        }
        result = result.stream().sorted(Comparator.comparingInt(StatsViewDto::getHits).reversed())
                .toList();
        return result;
    }


}
