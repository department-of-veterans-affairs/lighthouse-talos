package gov.va.api.lighthouse.talos;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.nio.charset.StandardCharsets;
import java.util.List;
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

  @Builder.Default String name = "Client key protected endpoint";

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
  /* getRequestUrl() returns a StringBuffer, errorprone wants a StringBuilder */
  @SuppressWarnings("JdkObsolete")
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    String key = request.getHeader(clientKeyHeader());
    if (isBlank(key) || !clientKeys().contains(key)) {
      log.error("Rejecting request {} ({})", name(), clientKeyHeader());
      unauthorizedResponse.accept(response);
      return;
    }
    filterChain.doFilter(request, response);
  }
}
