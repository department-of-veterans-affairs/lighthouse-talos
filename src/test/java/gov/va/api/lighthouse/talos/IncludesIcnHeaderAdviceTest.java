package gov.va.api.lighthouse.talos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

public class IncludesIcnHeaderAdviceTest {
  @Test
  public void addHeaderForNoPatients() {
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    IncludesIcnHeaderAdvice.addHeaderForNoPatients(mockResponse);
    verify(mockResponse, Mockito.atLeastOnce()).getHeader("X-VA-INCLUDES-ICN");
    verify(mockResponse).addHeader("X-VA-INCLUDES-ICN", "NONE");
    verifyNoMoreInteractions(mockResponse);
  }

  @Test
  public void addHeaderForPatients() {
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    IncludesIcnHeaderAdvice.addHeader(mockResponse, "a,b,c");
    verify(mockResponse, Mockito.atLeastOnce()).getHeader("X-VA-INCLUDES-ICN");
    verify(mockResponse).addHeader("X-VA-INCLUDES-ICN", "a,b,c");
    verifyNoMoreInteractions(mockResponse);
  }

  @Test
  public void beforeBodyWriteThrowsExceptionForUnsupportedType() {
    Assertions.assertThrows(
        IllegalStateException.class,
        () -> new FakeMajg().beforeBodyWrite(null, null, null, null, null, null));
  }

  @Test
  public void icnHeaderIsPresentForResource() {
    ServerHttpResponse mockResponse = mock(ServerHttpResponse.class);
    HttpHeaders mockHeaders = mock(HttpHeaders.class);
    when(mockResponse.getHeaders()).thenReturn(mockHeaders);
    new FakeMajg()
        .beforeBodyWrite(
            FakeResource.builder().id("666V666").build(), null, null, null, null, mockResponse);
    verify(mockHeaders, Mockito.atLeastOnce()).get("X-VA-INCLUDES-ICN");
    verify(mockHeaders).add("X-VA-INCLUDES-ICN", "666V666");
    verifyNoMoreInteractions(mockHeaders);
  }

  @Test
  public void icnHeadersAreDistictValuesForResourceBundle() {
    ServerHttpResponse mockResponse = mock(ServerHttpResponse.class);
    HttpHeaders mockHeaders = mock(HttpHeaders.class);
    when(mockResponse.getHeaders()).thenReturn(mockHeaders);
    var payload =
        FakeBundle.builder()
            .entry(
                List.of(
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("666V666").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("777V777").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("888V888").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("666V666").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("777V777").build())
                        .build()))
            .build();
    new FakeMajg().beforeBodyWrite(payload, null, null, null, null, mockResponse);
    verify(mockHeaders, Mockito.atLeastOnce()).get("X-VA-INCLUDES-ICN");
    verify(mockHeaders).add("X-VA-INCLUDES-ICN", "666V666,777V777,888V888");
    verifyNoMoreInteractions(mockHeaders);
  }

  @Test
  public void icnHeadersArePresentForResourceBundle() {
    ServerHttpResponse mockResponse = mock(ServerHttpResponse.class);
    HttpHeaders mockHeaders = mock(HttpHeaders.class);
    when(mockResponse.getHeaders()).thenReturn(mockHeaders);
    var payload =
        FakeBundle.builder()
            .entry(
                List.of(
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("666V666").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("777V777").build())
                        .build(),
                    FakeEntry.builder()
                        .resource(FakeResource.builder().id("888V888").build())
                        .build()))
            .build();
    new FakeMajg().beforeBodyWrite(payload, null, null, null, null, mockResponse);
    verify(mockHeaders, Mockito.atLeastOnce()).get("X-VA-INCLUDES-ICN");
    verify(mockHeaders).add("X-VA-INCLUDES-ICN", "666V666,777V777,888V888");
    verifyNoMoreInteractions(mockHeaders);
  }

  @Test
  public void supportedAcceptsResourceOrResourceBundle() {
    MethodParameter supportedResource = mock(MethodParameter.class);
    doReturn(FakeResource.class).when(supportedResource).getParameterType();
    assertThat(new FakeMajg().supports(supportedResource, null)).isTrue();
    MethodParameter supportedResourceBundle = mock(MethodParameter.class);
    doReturn(FakeBundle.class).when(supportedResourceBundle).getParameterType();
    assertThat(new FakeMajg().supports(supportedResourceBundle, null)).isTrue();
    MethodParameter unsupportedResource = mock(MethodParameter.class);
    doReturn(String.class).when(unsupportedResource).getParameterType();
    assertThat(new FakeMajg().supports(unsupportedResource, null)).isFalse();
  }

  /**
   * Silly Test implementation of the AbstractIncludesIcnMajig.java Because we are using Templates,
   * we also need a a fake Resource, Entry, and Bundle class
   */
  public static final class FakeMajg implements ResponseBodyAdvice<Object> {
    @Delegate
    private final ResponseBodyAdvice<Object> delegate =
        IncludesIcnHeaderAdvice.<FakeResource, FakeBundle>builder()
            .type(FakeResource.class)
            .bundleType(FakeBundle.class)
            .extractResources(bundle -> bundle.entry().stream().map(FakeEntry::resource))
            .extractIcns(body -> Stream.of(body.id))
            .build();
  }

  @Data
  @Builder
  static final class FakeResource {
    String id;

    String implicitRules;

    String language;

    String meta;
  }

  @Data
  @Builder
  static final class FakeEntry {
    String url;

    FakeResource resource;
  }

  @Data
  @Builder
  static final class FakeBundle {
    Integer total;

    List<FakeEntry> entry;
  }
}
