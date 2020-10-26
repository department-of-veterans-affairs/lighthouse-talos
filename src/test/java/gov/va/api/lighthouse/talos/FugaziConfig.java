package gov.va.api.lighthouse.talos;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
            .clientKeys(List.of("shanktopus"))
            .unauthorizedResponse(this::unauthorizedResponse)
            .build());
    protectedEndpoint.addUrlPatterns("/fugazi/Patient/*");
    return protectedEndpoint;
  }

  @SneakyThrows
  private void unauthorizedResponse(HttpServletResponse response) {
    var message =
        JacksonConfig.createMapper()
            .writeValueAsString(
                FugaziRestController.FugaziResponse.builder().error("Unauthorized").build());
    response.setStatus(401);
    response.addHeader("Content-Type", "application/json");
    response.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
  }
}
