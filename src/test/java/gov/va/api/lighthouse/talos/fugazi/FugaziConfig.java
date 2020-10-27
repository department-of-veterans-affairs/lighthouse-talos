package gov.va.api.lighthouse.talos.fugazi;

import static gov.va.api.lighthouse.talos.Responses.unauthorizedAsJson;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.lighthouse.talos.ClientKeyProtectedEndpointFilter;
import java.util.List;
import lombok.SneakyThrows;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    return protectedEndpoint;
  }

  @SneakyThrows
  private String unauthorizedPayload() {
    return JacksonConfig.createMapper()
        .writeValueAsString(
            FugaziRestController.FugaziResponse.builder().error("Unauthorized").build());
  }
}
