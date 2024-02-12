package homes.banzzokee.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import homes.banzzokee.domain.type.Role;
import homes.banzzokee.domain.user.entity.User;
import org.apache.tika.Tika;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.util.StringUtils;

import static org.mockito.BDDMockito.given;

public class MockDataUtil {

  private final static Tika tika = new Tika();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static MockMultipartFile createMockMultipartFile(String name, String file)
      throws IOException {
    Path path = Path.of(file);
    byte[] bytes = Files.readAllBytes(path);
    String originFilename = StringUtils.getFilename(file);
    String contentType = tika.detect(path);
    return new MockMultipartFile(name, originFilename, contentType, bytes);
  }

  public static MockPart createMockPart(String name, Object value)
      throws JsonProcessingException {
    MockPart mockPart = new MockPart(name, objectMapper.writeValueAsBytes(value));
    mockPart.getHeaders().set("Content-Type", "application/json");
    return mockPart;
  }

  public static User createMockUser(long id, String email, Role[] roles) {
    User user = Mockito.mock(User.class);
    given(user.getId()).willReturn(id);
    given(user.getEmail()).willReturn(email);
    given(user.getRole()).willReturn(Arrays.stream(roles).collect(Collectors.toSet()));
    return user;
  }
}
