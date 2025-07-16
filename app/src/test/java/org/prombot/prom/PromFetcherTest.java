package org.prombot.prom;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PromFetcherTest {

  private PromFetcher promFetcher;

  @BeforeEach
  void setUp() {
    promFetcher = new PromFetcher();
  }

  @Test
  void testParsePromResponse_successfulResponse() {
    String jsonResponse =
        """
        {
          "status": "success",
          "data": {
            "resultType": "vector",
            "result": [
              {
                "metric": {},
                "value": [1690000000.123, "42.6789"]
              }
            ]
          }
        }
        """;

    double result = promFetcher.parsePromResponse(jsonResponse);
    assertEquals(42.67, result);
  }

  @Test
  void testParsePromResponse_failureStatus() {
    String jsonResponse =
        """
        {
          "status": "error",
          "error": "some error message"
        }
        """;

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              promFetcher.parsePromResponse(jsonResponse);
            });

    assertTrue(exception.getMessage().contains("Query failed: some error message"));
  }

  @Test
  void testParsePromResponse_emptyResult() {
    String jsonResponse =
        """
        {
          "status": "success",
          "data": {
            "resultType": "vector",
            "result": []
          }
        }
        """;

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              promFetcher.parsePromResponse(jsonResponse);
            });

    assertTrue(exception.getMessage().contains("No data returned for query"));
  }

  @Test
  void testParsePromResponse_invalidJson() {
    String invalidJson = "not a json";

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              promFetcher.parsePromResponse(invalidJson);
            });

    assertTrue(exception.getMessage().contains("Failed to parse response"));
  }
}
