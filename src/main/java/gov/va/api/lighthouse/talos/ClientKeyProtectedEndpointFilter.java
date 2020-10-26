package gov.va.api.lighthouse.talos;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.function.Consumer;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Builder
@Getter
public class ClientKeyProtectedEndpointFilter extends OncePerRequestFilter {
  @Builder.Default private final String clientKeyHeader = "client-key";

  @Singular private final List<String> clientKeys;

  @NonNull private final Consumer<HttpServletResponse> unauthorizedResponse;

  @Builder.Default private final String name = "Client key protected endpoint";

  /** Immutable set of client keys. */
  List<String> clientKeys() {
    if (clientKeys == null) {
      return List.of();
    }
    return clientKeys;
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
      unauthorizedResponse().accept(response);
      return;
    }
    filterChain.doFilter(request, response);
  }
}
