package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;

import java.time.LocalDateTime;
import java.util.List;

// это временный тест, он ничего не проверяет, используется только для вывода отладочного лога из StatsClientImpl
@SpringBootTest(classes = StatsClientImpl.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class StatsClientImplTest {
    private final StatsClient statsClient;

    private StatsViewDto statsViewDto;

    private StatsHitDto statsHitDto = StatsHitDto.builder()
            .id(1L)
            .app("test app")
            .uri("test uri")
            .ip("127.0.0.1")
            .timestamp(LocalDateTime.now())
            .build();

    @Test
    void hit() {
        statsClient.hit(statsHitDto);
    }

    @Test
    void getStats() {
        List<StatsViewDto> statsViewDtos = statsClient.getStats("2021-01-01 10:11:12",
                "2021-01-02 11:12:13", List.of("uri1", "uri2"), true);
    }
}
