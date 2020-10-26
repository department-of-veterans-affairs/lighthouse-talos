package gov.va.api.lighthouse.talos;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
    List<String> matchingKeyPairs =
        clientKeyPairs.stream()
            .filter(kp -> Pattern.compile(kp.urlPattern).matcher(request.getRequestURI()).matches())
            .flatMap(kp -> kp.clientKeys().stream())
            .collect(Collectors.toList());

    if (matchingKeyPairs.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    String clientKeyHeader = request.getHeader(CLIENT_KEY_HEADER);
    boolean clientKeyIsValid =
        clientKeyHeader != null
            && matchingKeyPairs.stream().anyMatch(key -> key.equals(clientKeyHeader));

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

  /** One to many mapping of Client-Keys and Url Patterns. */
  @Value
  @Builder
  public static class ClientKeyPair {
    String urlPattern;

    List<String> clientKeys;
  }
}
