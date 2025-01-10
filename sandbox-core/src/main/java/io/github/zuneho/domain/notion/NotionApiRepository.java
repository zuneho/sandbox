package io.github.zuneho.domain.notion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.zuneho.domain.common.util.JsonUtil;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NotionApiRepository {

    private static final String NOTION_DATABASE_ENDPOINT = "https://api.notion.com/v1/databases/";
    private static final String NOTION_API_TOKEN = "secret_AgRqZEOeyakFwtggSqUufF1WICoqLvPcN7xF8podMCt";
    private static final String NOTION_VERSION_HEADER_NAME = "Notion-Version";
    private static final String NOTION_VERSION_HEADER_VALUE = "2022-06-28";

    private static final int API_CONNECT_TIMEOUT = 5000;

    private static final String TEST_DB_ID = "10b650dbdc4f80619695dbd1357982ee";
    private static final String TEST_DB_2_ID = "10b650dbdc4f8032ad86f6fcfd432a35";

    private static final WebClient webClient = initNotionClient();

    public static HttpHeaders getDefaultHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + NOTION_API_TOKEN);
        headers.set(NOTION_VERSION_HEADER_NAME, NOTION_VERSION_HEADER_VALUE);
        return headers;
    }


    public static <T> T getNotionUrl(String database, String property, Long notionDataIdx, ParameterizedTypeReference<T> responseModel) {
        String queryUrl = TEST_DB_ID + "/query";
        NotionQueryPayload notionQueryPayload = NotionQueryPayload.of(property, notionDataIdx);
        return exchangeFromNotionApi(HttpMethod.POST, queryUrl, notionQueryPayload, true, getDefaultHeader(), responseModel);
    }


    public static void main(String[] args) {
//        String data = getNotionData();
//        System.out.println(data);
        ParameterizedTypeReference<NotionData> responseType = new ParameterizedTypeReference<>() {
        };
        NotionData result = getNotionUrl(TEST_DB_ID, "ID", 1L, responseType);
        System.out.println("====result===");
        System.out.println(result);
    }

    public static String getNotionData() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + NOTION_API_TOKEN);
        headers.set(NOTION_VERSION_HEADER_NAME, NOTION_VERSION_HEADER_VALUE);

        return exchangeFromNotionApi(HttpMethod.POST, TEST_DB_ID + "/query", null, false, headers, new ParameterizedTypeReference<String>() {
        });
    }

    public static <T> T exchangeFromNotionApi(
            HttpMethod httpMethod,
            String uri,
            Object requestBody,
            boolean jsonBody,
            HttpHeaders headers,
            ParameterizedTypeReference<T> responseModel
    ) {

        try {
            WebClient.RequestBodySpec baseSpec = webClient.method(httpMethod).uri(uri);
            if (requestBody != null) {
                if (jsonBody) {
                    log.info(JsonUtil.toJsonStringWithThrows(requestBody));
                }
                baseSpec.bodyValue(jsonBody ? JsonUtil.toJsonStringWithThrows(requestBody) : requestBody);
            }
            if (headers != null) {
                baseSpec.headers(httpHeaders -> httpHeaders.addAll(headers));
            }

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            ResponseEntity<T> response = baseSpec
                    .retrieve()
                    .toEntity(responseModel)
                    .block();
            stopWatch.stop();
            log.info("total time ={}", stopWatch.getTotalTimeMillis());
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }

            throw new RuntimeException("exchangeFromInternalApi response status Error. "
                    + "status=" + (response != null ? response.getStatusCode().toString() : null)
                    + "body=" + (response != null ? response.getBody() : null));
        } catch (Exception e) {
            log.error("exchangeFromInternalApi error. message={}", e.getMessage(), e);
            throw new RuntimeException("일시적인 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @Getter
    @Builder
    public static class NotionQueryPayload {
        private final Filter filter;

        @Getter
        @Builder
        public static class Filter {
            private final String property;
            private final UniqueId unique_id;

            public static Filter of(String property, Long notionIdx) {
                return Filter.builder()
                        .property(property)
                        .unique_id(UniqueId.of(notionIdx))
                        .build();
            }
        }

        @Getter
        @Builder
        public static class UniqueId {
            private final Long equals;

            public static UniqueId of(Long notionIdx) {
                return UniqueId.builder()
                        .equals(notionIdx)
                        .build();
            }
        }

        public static NotionQueryPayload of(String property, Long notionIdx) {
            return NotionQueryPayload.builder()
                    .filter(Filter.of(property, notionIdx))
                    .build();
        }
    }

    @ToString
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NotionData {
        private String object;
        private List<Page> results;

        // Getters and Setters

        public static class Page {
            private String object;
            private String id;

            @JsonProperty("created_time")
            private String createdTime;

            private Properties properties;

            // Getters and Setters
        }

        public static class Properties {
            @JsonProperty("NAME")
            private Name name;

            // Getters and Setters
        }

        public static class Name {
            private String type;
            private List<Title> title;

            // Getters and Setters
        }

        public static class Title {
            private Text text;

            // Getters and Setters
        }

        public static class Text {
            private String content;

            // Getters and Setters
        }
    }

    private static WebClient initNotionClient() {
        int maxInMemorySize = 10 * 1024 * 1024;
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .build();
        ConnectionProvider provider = ConnectionProvider.builder("notion-pool")
                .maxConnections(500)
                .pendingAcquireTimeout(Duration.ofMillis(0))
                .pendingAcquireMaxCount(-1)
                .maxIdleTime(Duration.ofMillis(8000L))
                .maxLifeTime(Duration.ofMillis(8000L))
                .build();

        SslContext sslContext;
        try {
            sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

        } catch (Exception e) {
            log.error("initNotionClient error. message={}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }

        HttpClient httpClient = HttpClient.create(provider)
                .tcpConfiguration(client -> client
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, API_CONNECT_TIMEOUT)
                        .doOnConnected(connection -> connection
                                .addHandlerLast(new ReadTimeoutHandler(API_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(API_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS))
                        )
                )
                .responseTimeout(Duration.ofMillis(API_CONNECT_TIMEOUT))
                .secure(t -> t.sslContext(sslContext));

        return WebClient.builder()
                .baseUrl(NOTION_DATABASE_ENDPOINT)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
