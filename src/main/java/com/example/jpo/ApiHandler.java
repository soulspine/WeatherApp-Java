package com.example.jpo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.databind.*;

public class ApiHandler {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static String timezone = "Europe/Berlin";

    private static JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "192.168.1.13", 6379, 2000, "fortnite");

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void SetTimezone(String newTimezone) {
        if (newTimezone.isEmpty()) throw new IllegalArgumentException("Timezone parameter cannot be empty");
        timezone = newTimezone;
    }

    public static String GetData(long fetchParams, float latitude, float longitude, LocalDate startDate, LocalDate endDate) {
        List<ApiFetchTypes> selectedTypes = new ArrayList<>();
        for (ApiFetchTypes type : ApiFetchTypes.values()) {
            if ((fetchParams & type.bit) != 0) {
                selectedTypes.add(type);
            }
        }

        Map<String, Map<String, String>> cachedData = new HashMap<>();
        boolean cacheMiss = false;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth("fortnite");

            for (ApiFetchTypes type : selectedTypes) {
                Map<String, String> timeToValue = new LinkedHashMap<>();
                LocalDate currentDate = startDate;

                while (!currentDate.isAfter(endDate)) {
                    for (int hour = 0; hour < 24; hour++) {
                        String timeKey = String.format("%sT%02d:00", currentDate, hour);
                        String redisKey = type.description + ":" + timeKey;

                        String cachedValue = jedis.get(redisKey);
                        if (cachedValue == null) {
                            cacheMiss = true;
                        } else {
                            timeToValue.put(timeKey, cachedValue);
                        }
                    }
                    currentDate = currentDate.plusDays(1);
                }
                cachedData.put(type.description, timeToValue);
            }

            if (!cacheMiss) {
                System.out.println("Data fetched from Redis cache");
                return objectMapper.writeValueAsString(cachedData);
            }
        } catch (Exception e) {
            System.err.println("Redis error: " + e.getMessage());
        }

        String endpoint = BASE_URL
                + "?timezone=" + timezone
                + "&latitude=" + latitude
                + "&longitude=" + longitude
                + "&start_date=" + startDate
                + "&end_date=" + endDate
                + "&hourly=";

        for (ApiFetchTypes type : selectedTypes) {
            endpoint += type.description + ",";
        }
        if (endpoint.endsWith(",")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        HttpResponse<String> response;
        try {
            response = CLIENT.send(
                    HttpRequest.newBuilder().uri(URI.create(endpoint)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("API request failed: " + response.statusCode());
        }

        String body = response.body();

        Map<String, Map<String, String>> resultMap = new HashMap<>();

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth("fortnite");

            JsonNode root = objectMapper.readTree(body);
            JsonNode hourly = root.path("hourly");
            JsonNode timeArray = hourly.path("time");

            for (ApiFetchTypes type : selectedTypes) {
                JsonNode valuesArray = hourly.path(type.description);
                Map<String, String> timeToValue = new LinkedHashMap<>();

                if (!valuesArray.isMissingNode() && valuesArray.isArray()) {
                    for (int i = 0; i < timeArray.size(); i++) {
                        String timeKey = timeArray.get(i).asText();
                        String value = valuesArray.get(i).isNull() ? "null" : valuesArray.get(i).asText();
                        jedis.set(type.description + ":" + timeKey, value);
                        jedis.expire(type.description + ":" + timeKey, 86400);
                        timeToValue.put(timeKey, value);
                    }
                }
                resultMap.put(type.description, timeToValue);
            }
        } catch (Exception e) {
            System.err.println("Redis error while saving: " + e.getMessage());
        }

        try {
            System.out.println("Data fetched from external API");
            return objectMapper.writeValueAsString(resultMap);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing result map to JSON", e);
        }
    }
}
