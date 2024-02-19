package homes.banzzokee.domain.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class AdoptionStatusTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void jsonSerialize()
      throws JsonProcessingException {
    TestDto testDto = TestDto.builder()
        .adoptionId(1L)
        .status(AdoptionStatus.ADOPTING)
        .build();
    String responseJson = objectMapper.writeValueAsString(testDto);

    assertEquals("ADOPTING", JsonPath.read(responseJson, "$.status.key"));
    assertEquals("분양중", JsonPath.read(responseJson, "$.status.value"));
  }

  @Test
  void jsonDeserialize() throws IOException {
    objectMapper.registerModule(new ParameterNamesModule());
    String json = "{\"adoptionId\":1, \"status\":\"ADOPTING\"}";
    TestDto testDto = objectMapper.readValue(json, TestDto.class);
    assertEquals(1L, testDto.getAdoptionId());
    assertEquals(AdoptionStatus.ADOPTING, testDto.getStatus());
  }
}