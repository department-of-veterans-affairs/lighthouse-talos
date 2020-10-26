package gov.va.api.lighthouse.talos;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.net.URI;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({FugaziApplication.class, JacksonConfig.class})
@TestPropertySource(properties = {"ssl.enable-client=false"})
public class ClientKeyProtectedEndpointFilterSpringTest {
  @Autowired TestRestTemplate restTemplate;

  @Test
  void filterApplied() {
    ResponseEntity<FugaziRestController.FugaziResponse> testResponse =
        makeRequest("/fugazi/Patient/m8", "shanktopus");
    assertThat(testResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(testResponse.getBody().message()).isEqualTo("Hello m8");
  }

  @SneakyThrows
  ResponseEntity<FugaziRestController.FugaziResponse> makeRequest(String url, String clientKey) {
    RequestEntity<Void> request =
        RequestEntity.get(new URI(url)).header("shanktokey", clientKey).build();
    ResponseEntity<FugaziRestController.FugaziResponse> testResponse =
        restTemplate.exchange(request, FugaziRestController.FugaziResponse.class);
    return testResponse;
  }

  @Test
  void unauthorized() {
    ResponseEntity<FugaziRestController.FugaziResponse> testResponse =
        makeRequest("/fugazi/Patient/401", "BIG-oof");
    assertThat(testResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(testResponse.getBody().error()).isEqualTo("Unauthorized");
  }
}
