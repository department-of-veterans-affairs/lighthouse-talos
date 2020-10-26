package gov.va.api.lighthouse.talos;

import java.util.List;
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
            .clientKeyPair(
                ClientKeyProtectedEndpointFilter.ClientKeyPair.builder()
                    .urlPattern(".*/Patient/.*")
                    .clientKeys(List.of("shanktopus"))
                    .build())
            .unauthorizedResponseString("{\"message\":\"NOPE\"}")
            .build());
    protectedEndpoint.addUrlPatterns("/fugazi/Patient/*");
    return protectedEndpoint;
  }
}
