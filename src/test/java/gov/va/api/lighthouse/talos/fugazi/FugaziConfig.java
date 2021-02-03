package gov.va.api.lighthouse.talos.fugazi;

import static gov.va.api.lighthouse.talos.Responses.unauthorizedAsJson;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.lighthouse.talos.ClientKeyProtectedEndpointFilter;
import gov.va.api.lighthouse.talos.PathRewriteFilter;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FugaziConfig {

  @Bean
  FilterRegistrationBean<ClientKeyProtectedEndpointFilter> clientKeyProtectedEndpointFilter() {
    var protectedEndpoint = new FilterRegistrationBean<ClientKeyProtectedEndpointFilter>();

    protectedEndpoint.setFilter(
        ClientKeyProtectedEndpointFilter.builder()
            .clientKeyHeader("shanktokey")
            .clientKeys(List.of("shanktopus"))
            .unauthorizedResponse(unauthorizedAsJson(unauthorizedPayload()))
            .build());
    protectedEndpoint.addUrlPatterns("/fugazi/Patient/*");
    log.info("Client-key filter initialized");
    return protectedEndpoint;
  }

  @Bean
  FilterRegistrationBean<PathRewriteFilter> pathRewriteFilter() {
    var registration = new FilterRegistrationBean<PathRewriteFilter>();
    PathRewriteFilter filter = PathRewriteFilter.builder().removeLeadingPath("/talos/").build();
    registration.setFilter(filter);
    registration.addUrlPatterns(filter.removeLeadingPathsAsUrlPatterns());
    log.info("PathRewrite filter initialized");
    return registration;
  }

  @SneakyThrows
  private String unauthorizedPayload() {
    return JacksonConfig.createMapper()
        .writeValueAsString(
            FugaziRestController.FugaziResponse.builder().error("Unauthorized").build());
  }
}
