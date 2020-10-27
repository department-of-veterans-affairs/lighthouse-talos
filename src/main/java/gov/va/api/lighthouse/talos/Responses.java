package gov.va.api.lighthouse.talos;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/** Provides utilities for common responses. */
@UtilityClass
public class Responses {
  /**
   * A super basic 401 Unauthorized response message.
   *
   * <p>Status Code: 401 Content-Type: application/json {"message": "UNAUTHORIZED"}
   */
  @SneakyThrows
  public static void standardUnauthorizedMessage(HttpServletResponse response) {
    respondWith(response, 401, "application/json", "{\"message\":\"UNAUTHORIZED\"}");
  }

  /** Update the response with status code, content and body. */
  @SneakyThrows
  public void respondWith(
      HttpServletResponse response, int statusCode, String contentType, String body) {
    response.setStatus(statusCode);
    response.addHeader("Content-Type", contentType);
    response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
  }

  /** This consumer will set the status to 401 with an application/json body response. */
  @SneakyThrows
  public Consumer<HttpServletResponse> unauthorizedAsJson(String jsonBody) {
    return (response) -> {
      respondWith(response, 401, "application/json", jsonBody);
    };
  }
}
