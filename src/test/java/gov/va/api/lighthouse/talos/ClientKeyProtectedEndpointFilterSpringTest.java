package gov.va.api.lighthouse.talos;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
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

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({FugaziApplication.class, JacksonConfig.class})
@TestPropertySource(
        properties = {
                "ssl.enable-client=false"
        })
public class ClientKeyProtectedEndpointFilterSpringTest {
    @Autowired TestRestTemplate restTemplate;

    @Test
    void filterApplied() {
        ResponseEntity<String> testResponse = makeRequest("/fugazi/Patient/m3", "shanktopus");
        assertThat(testResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(testResponse.getBody()).isEqualTo("hello m3");
    }

    @SneakyThrows
    ResponseEntity<String> makeRequest(String url, String clientKey) {
        RequestEntity<Void> request = RequestEntity.get(new URI(url)).header("client-key", clientKey).build();
        ResponseEntity<String> testResponse = restTemplate.exchange(request, String.class);
        return testResponse;
    }

    @Test
    void unauthorized() {
        ResponseEntity<String> testResponse = makeRequest("/fugazi/Patient/m3", "BIG-oof");
        assertThat(testResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(testResponse.getBody()).isEqualTo("{\"message\":\"NOPE\"}");
    }

}
