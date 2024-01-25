package homes.banzzokee.global.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.tika.Tika;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StringUtils;

public class MockDataUtil {

  private final static Tika tika = new Tika();

  public static MockMultipartFile createMockMultipartFile(String file)
      throws IOException {
    Path path = Path.of(file);
    byte[] bytes = Files.readAllBytes(path);
    String originFilename = StringUtils.getFilename(file);
    String name = StringUtils.stripFilenameExtension(originFilename);
    String contentType = tika.detect(path);
    return new MockMultipartFile(name, originFilename, contentType, bytes);
  }
}
