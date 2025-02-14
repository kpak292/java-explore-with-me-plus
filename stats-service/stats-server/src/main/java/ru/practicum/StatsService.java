package ru.practicum;

import java.util.ArrayList;
import java.util.Collection;

public interface StatsService {

    void saveHit(StatsHitDto hitDto);

    Collection<StatsViewDto> getStats(String start,
                                      String end,
                                      ArrayList<String> uris,
                                      Boolean unique);
}
