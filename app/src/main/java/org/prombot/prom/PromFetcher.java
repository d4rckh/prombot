package org.prombot.prom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.prombot.config.ConfigService;

public class PromFetcher {
    @Inject
    private ConfigService yamlConfigService;

    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PromFetcher() {
        this.client = createUnsafeClient();
    }

    double parsePromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (!"success".equals(root.path("status").asText())) {
                throw new RuntimeException("Query failed: " + root.path("error").asText());
            }

            JsonNode result = root.path("data").path("result");
            if (!result.isArray() || result.isEmpty()) {
                throw new RuntimeException("No data returned for query");
            }

            JsonNode valueNode = result.get(0).path("value");

            return Math.floor(valueNode.get(1).asDouble() * 100) / 100;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response from prometheus: " + e.getMessage(), e);
        }
    }

    public Double fetchLastValue(String query) {
        String prometheusBaseUrl = this.yamlConfigService.getBotConfig().getPrometheusUrl();

        HttpUrl url = HttpUrl.parse(prometheusBaseUrl)
                .newBuilder()
                .addPathSegments("api/v1/query")
                .addQueryParameter("query", query)
                .addQueryParameter("time", Instant.now().toString())
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP code " + response.code());
            }
            return this.parsePromResponse(response.body().string());
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch from Prometheus: " + e.getMessage(), e);
        }
    }

    public List<Double> fetchValuesOverDuration(String query, Duration duration) {
        return this.fetchValuesOverDuration(query, duration, calculateStep(duration));
    }

    public List<Double> fetchValuesOverDuration(String query, Duration duration, String step) {
        String prometheusBaseUrl = this.yamlConfigService.getBotConfig().getPrometheusUrl();

        Instant end = Instant.now();
        Instant start = end.minus(duration);

        HttpUrl url = HttpUrl.parse(prometheusBaseUrl)
                .newBuilder()
                .addPathSegments("api/v1/query_range")
                .addQueryParameter("query", query)
                .addQueryParameter("start", start.toString())
                .addQueryParameter("end", end.toString())
                .addQueryParameter("step", step)
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP code " + response.code());
            }

            return parsePromRangeResponse(response.body().string());
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch range data from Prometheus: " + e.getMessage(), e);
        }
    }

    private List<Double> parsePromRangeResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (!"success".equals(root.path("status").asText())) {
                throw new RuntimeException(
                        "Query range failed: " + root.path("error").asText());
            }

            JsonNode result = root.path("data").path("result");
            if (!result.isArray() || result.isEmpty()) {
                throw new RuntimeException("No data returned for range query");
            }

            JsonNode values = result.get(0).path("values"); // array of [timestamp, value]
            List<Double> extracted = new ArrayList<>();
            for (JsonNode pair : values) {
                extracted.add(Math.floor(pair.get(1).asDouble() * 100) / 100);
            }
            return extracted;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse range response: " + e.getMessage(), e);
        }
    }

    public String calculateStep(Duration duration) {
        long seconds = duration.getSeconds();
        long stepSeconds;

        if (seconds <= 60) stepSeconds = 1;
        else if (seconds <= 300) stepSeconds = 5;
        else if (seconds <= 3600) stepSeconds = 30;
        else stepSeconds = 60;

        return String.valueOf(stepSeconds);
    }

    private OkHttpClient createUnsafeClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            HostnameVerifier allHostsValid = (hostname, session) -> true;

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(allHostsValid)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create unsafe HTTP client", e);
        }
    }
}
