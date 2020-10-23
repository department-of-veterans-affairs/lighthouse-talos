package gov.va.api.lighthouse.talos;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Builder
public class ClientKeyProtectedEndpointFilter extends OncePerRequestFilter {

  public static final String CLIENT_KEY_HEADER = "client-key";

  @Singular List<ClientKeyPair> clientKeyPairs;

  String unauthorizedResponseString;

  @Override
  @SneakyThrows
  @SuppressWarnings("JdkObsolete")
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    boolean clientKeyIsValid =
        clientKeyPairs.stream()
            .filter(kp -> request.getRequestURI().matches(kp.urlPattern()))
            .anyMatch(
                kp -> hasValidClientKey(kp.clientKeys(), request.getHeader(CLIENT_KEY_HEADER)));

    if (clientKeyIsValid) {
      filterChain.doFilter(request, response);
      return;
    }

    log.error(
        "Client Key is Invalid. Request was {}", sanitize(request.getRequestURL().toString()));
    response.setStatus(401);
    response.setContentType("application/json");
    response.getOutputStream().write(unauthorizedResponseString.getBytes(StandardCharsets.UTF_8));
  }

  private boolean hasValidClientKey(List<String> allowedKeys, String requestKey) {
    if (requestKey == null) {
      return false;
    }
    return allowedKeys.stream().filter(Objects::nonNull).anyMatch(requestKey::equals);
  }

  /** One to many mapping of Client-Keys and Url Patterns. */
  @Value
  @Builder
  public static class ClientKeyPair {
    String urlPattern;

    List<String> clientKeys;
  }
}
