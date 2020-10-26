package gov.va.api.lighthouse.talos;

import lombok.Builder;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FugaziRestController {

  @GetMapping("/fugazi/Patient/{id}")
  FugaziResponse hello(@PathVariable(name = "id", required = false) String id) {
    return FugaziResponse.builder().message("Hello " + id).build();
  }

  @Value
  @Builder
  public static class FugaziResponse {
    String message;

    String error;
  }
}
