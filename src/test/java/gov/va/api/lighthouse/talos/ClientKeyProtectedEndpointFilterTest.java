package gov.va.api.lighthouse.talos;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ClientKeyProtectedEndpointFilterTest {
  HttpServletRequest request = mock(HttpServletRequest.class);

  HttpServletResponse response = mock(HttpServletResponse.class);

  FilterChain filterChain = mock(FilterChain.class);

  private ClientKeyProtectedEndpointFilter filter() {
    return ClientKeyProtectedEndpointFilter.builder()
        .clientKeys(List.of("key1", "key3"))
        .unauthorizedResponse(ClientKeyProtectedEndpointFilter::standardUnauthorizedMessage)
        .build();
  }

  @ParameterizedTest
  @ValueSource(strings = {"key1", "key3"})
  @SneakyThrows
  void filterAppliedWhenUrlAndKeyAreCorrect(String clientKey) {
    when(request.getHeader("client-key")).thenReturn(clientKey);
    filter().doFilterInternal(request, response, filterChain);
    verifyNoInteractions(response);
    verify(filterChain).doFilter(request, response);
    verifyNoMoreInteractions(filterChain);
  }

  @ParameterizedTest
  @ValueSource(strings = {"KEY1", "key2", "kEy3", " key1 ", "", "   "})
  @SneakyThrows
  void filterResponse401WhenClientKeyDoesntMatch(String keyValue) {
    when(request.getRequestURL()).thenReturn(new StringBuffer("Saaad!"));
    when(request.getHeader("client-key")).thenReturn(keyValue);
    when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
    filter().doFilterInternal(request, response, filterChain);
    verifyNoInteractions(filterChain);
    verify(response).setStatus(401);
  }

  @Test
  @SneakyThrows
  void filterResponse401WhenClientKeyIsNull() {
    when(request.getRequestURL()).thenReturn(new StringBuffer("Saaad!"));
    when(request.getHeader("client-key")).thenReturn(null);
    when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
    filter().doFilterInternal(request, response, filterChain);
    verifyNoInteractions(filterChain);
    verify(response).setStatus(401);
  }
}
