package ru.practicum;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsClient {

    @Value("${statistics.url}")
    private String serverUrl;
    private final RestTemplate restTemplate;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void addHit(HitDto endpointHitRequestDto) {
        restTemplate.postForLocation(serverUrl.concat("/hit"), endpointHitRequestDto);
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                               List<String> uris, boolean unique) {
        Map<String, Object> parameters = new HashMap<>(Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "unique", unique));

        if (uris != null && !uris.isEmpty()) {
            parameters.put("uris", String.join(",", uris));
        }

        StatsDto[] response = restTemplate.getForObject(
                serverUrl.concat("/stats?start={start}&end={end}&uris={uris}&unique={unique}"),
                StatsDto[].class, parameters);

        return Objects.isNull(response)
                ? List.of()
                : List.of(response);
    }

/*
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final WebClient webClient;

    @Value("${spring.application.name}")
    private String appName;

    public StatClient(String url) {
        this.webClient = WebClient.create(url);
    }

    public void addHit(HttpServletRequest httpRequest, Long eventId) {
        HitDto endpointHitDto = HitDto.builder()
                .app(appName)
                .ip(httpRequest.getRemoteAddr())
                .uri(httpRequest.getRequestURI() + "/" + eventId)
                .timestamp(LocalDateTime.now())
                .build();
        webClient.post()
                .uri("/hit")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(endpointHitDto)
                .retrieve()
                .bodyToMono(HitDto.class)
                .block();
    }

    public void addHit(HttpServletRequest httpRequest) {
        HitDto endpointHitDto = HitDto.builder()
                .app(appName)
                .ip(httpRequest.getRemoteAddr())
                .uri(httpRequest.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        webClient.post()
                .uri("/hit")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(endpointHitDto)
                .retrieve()
                .bodyToMono(HitDto.class)
                .block();
    }

    public List<StatsDto> getListStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/stats")
                        .queryParam("start", start.format(DATE_TIME_FORMATTER))
                        .queryParam("end", end.format(DATE_TIME_FORMATTER))
                        .queryParam("uris", uris)
                        .queryParam("unique", unique)
                        .build())
                .retrieve()
                .bodyToFlux(StatsDto.class)
                .collectList()
                .block();
    }*/


    /*private final HttpClient httpClient;

    private final String application;

    private final String statisticsUri;
    private final ObjectMapper mapper;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatisticsClient(String statisticsUri, String application, ObjectMapper mapper) {
        this.application = application;
        this.statisticsUri = statisticsUri;
        this.mapper = mapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public void addHit(HttpServletRequest request) {

        HitDto hitDto = HitDto.builder()
                .app(application)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(dateTimeFormatter))
                .build();
        try {
            HttpRequest.BodyPublisher bodyPublisher = HttpRequest
                    .BodyPublishers
                    .ofString(mapper.writeValueAsString(hitDto));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(statisticsUri + "/hit"))
                    .POST(bodyPublisher)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .build();
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());

        } catch (Exception e) {
            log.error("Не удалось сохранить информацию о том, что на uri конкретного сервиса был отправлен " +
                    "запрос пользователем.", e);

        }
    }

    public List<StatsDto> getStatsHit(StatsParamDto statsParamDto) {
        try {
            String queryString = toQueryString(statsParamDto);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(statisticsUri + "/stats" + queryString))
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (HttpStatus.valueOf(response.statusCode()).is2xxSuccessful()) {
                return mapper.readValue(response.body(), new TypeReference<>(){});
            }

        } catch (Exception e) {
            log.error("Не удалось получить статистику по запросу: " +  statsParamDto, e);
        }

        return Collections.emptyList();
    }

    private String toQueryString(StatsParamDto statsParamDto) {
        String start = statsParamDto.getStart();
        String end = statsParamDto.getEnd();

        String queryString = String.format("?start=%s&end=%s&unique=%b",
                start, end, statsParamDto.isUnique());
        if (statsParamDto.getUris().length > 0) {
            queryString += "&uris=" + String.join(",", statsParamDto.getUris());
        }

        return queryString;
    }*/
}
