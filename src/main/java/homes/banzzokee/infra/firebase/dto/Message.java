package homes.banzzokee.infra.firebase.dto;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class Message<T> {

  private final String title;
  private final String body;
  private final String image;
  private final Map<String, String> data;

  protected static ObjectMapper objectMapper;
  protected T fcmMessage;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.setVisibility(
        objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(ANY));
    objectMapper.setSerializationInclusion(NON_NULL);
  }

  abstract public T toFcmMessage();

  public String toJson() {
    try {
      SimpleFilterProvider filterProvider = new SimpleFilterProvider();
      filterProvider.setDefaultFilter(
          SimpleBeanPropertyFilter.serializeAllExcept("tokens", "getTokens"));
      return objectMapper.writer(filterProvider).writeValueAsString(toFcmMessage());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}