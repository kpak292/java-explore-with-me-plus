package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.StatItem;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<StatItem, Long> {

    @Query(value = "SELECT * FROM stats WHERE uri IN (?1) and (created_at between ?2 and ?3)", nativeQuery = true)
    Collection<StatItem> getStatsbyDatesAndUris(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM stats WHERE created_at between ?1 and ?2", nativeQuery = true)
    Collection<StatItem> getStatsbyDates(LocalDateTime start, LocalDateTime end);
}
