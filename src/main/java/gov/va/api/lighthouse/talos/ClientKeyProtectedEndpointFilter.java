package gov.va.api.lighthouse.talos;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Value
@EqualsAndHashCode(callSuper = false)
@Builder
public class ClientKeyProtectedEndpointFilter extends OncePerRequestFilter {
  @Builder.Default String clientKeyHeader = "client-key";

  List<String> clientKeys;

  @NonNull Consumer<HttpServletResponse> unauthorizedResponse;

  /**
   * A super basic 401 Unauthorized response message.
   *
   * <p>Status Code: 401 Content-Type: application/json {"message": "UNAUTHORIZED"}
   */
  @SneakyThrows
  public static void standardUnauthorizedMessage(HttpServletResponse response) {
    response.setStatus(401);
    response.setContentType("application/json");
    response
        .getOutputStream()
        .write("{\"message\":\"UNAUTHORIZED\"}".getBytes(StandardCharsets.UTF_8));
  }

  @Override
  @SneakyThrows
  // getRequestUrl() returns a StringBuffer, errorprone wants a StringBuilder
  @SuppressWarnings("JdkObsolete")
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    String clientKeyHeader = request.getHeader(clientKeyHeader());
    boolean clientKeyIsValid =
        clientKeyHeader != null
            && clientKeys().stream().filter(Objects::nonNull).anyMatch(clientKeyHeader::equals);

    if (clientKeyIsValid) {
      filterChain.doFilter(request, response);
      return;
    }

    log.error(
        "Client Key ({}) is invalid for request {}",
        sanitize(clientKeyHeader),
        sanitize(request.getRequestURL().toString()));
    unauthorizedResponse.accept(response);
  }
}
