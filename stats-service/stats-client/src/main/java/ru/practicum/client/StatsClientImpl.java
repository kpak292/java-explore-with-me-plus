package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;

import java.util.List;

@Slf4j
@Service
public class StatsClientImpl implements StatsClient {
    private final String baseUri;
    private final RestClient restClient;

    @Autowired
    public StatsClientImpl(@Value("${stats-server.url}") String baseUri) {
        this.baseUri = baseUri;
        restClient = RestClient.builder()
                .baseUrl(baseUri)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public void hit(StatsHitDto statsHitDto) {
        log.info("save statistics for {}", statsHitDto);
        try {
            restClient.post()
                    .uri("/hit")
                    .body(statsHitDto)
                    .retrieve();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public List<StatsViewDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("retrieve statistics with params: start = {}, end = {}, uris = {}, unique ={}",
                start, end, uris, unique);
        try {
            UriComponents uriComponents = UriComponentsBuilder
                    /*
                     к сожалению restClient при совместном использовании с UriComponentsBuilder
                     игнорирует baseUri в RestClient.builder() и выдает ошибку "URI with undefined scheme"
                     поэтому в fromUriString в данном случае baseUri используется явно, либо есть другой способ
                    */
                    .fromUriString(baseUri + "/stats")
                    .queryParam("start", start)
                    .queryParam("end", end)
                    .queryParam("uris", uris)
                    .queryParam("unique", unique)
                    .encode()
                    .build();
            log.info("uriComponents encoded {}", uriComponents.toUri());
            return restClient.get()
                    .uri(uriComponents.toUri())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StatsViewDto>>() {
                    });
        } catch (Exception e) {
            log.error(e.getMessage());
            return List.of();
        }
    }
}
