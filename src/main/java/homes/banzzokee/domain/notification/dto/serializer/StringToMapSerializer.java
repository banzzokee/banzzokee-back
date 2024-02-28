package homes.banzzokee.domain.notification.dto.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringToMapSerializer extends JsonSerializer<String> {

  private final ObjectMapper objectMapper;

  @Override
  public void serialize(String s, JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider) throws IOException {
    Map map = objectMapper.readValue(s, Map.class);
    jsonGenerator.writeObject(map);
  }
}
