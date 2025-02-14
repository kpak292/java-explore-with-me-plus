package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@RequestBody StatsHitDto hitDto) {
        statsService.saveHit(hitDto);
    }

    @GetMapping("/stats")
    public Collection<StatsViewDto> getStat(@RequestParam String start,
                                            @RequestParam String end,
                                            @RequestParam(required = false) ArrayList<String> uris,
                                            @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        return statsService.getStats(start, end, uris, unique);
    }
}
