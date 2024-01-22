package homes.banzzokee.domain.common.image.service;

import homes.banzzokee.domain.common.image.dto.ImageDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ImageServiceTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ImageService imageService;

  @Test
  @DisplayName("1개 파일 업로드 성공 테스트 및 파일 삭제 성공 테스트")
  void successUploadFile() throws IOException {
    //given
    MultipartFile multipartFile = createMockMultipartFile();

    //when
    ImageDto imageDto = imageService.uploadOneFile(multipartFile);

    // then
    ResponseEntity<String> afterSaveResponse = restTemplate.getForEntity(imageDto.getUrl(),
        String.class);
    Assertions.assertEquals(afterSaveResponse.getBody(), "Upload Test");

    imageService.deleteFile(imageDto.getFilename());
    ResponseEntity<String> afterDeleteResponse = restTemplate.getForEntity(imageDto.getUrl(),
        String.class);
    Assertions.assertEquals(afterDeleteResponse.getStatusCode(), HttpStatus.FORBIDDEN);
  }

  @Test
  @DisplayName("다수의 파일 업로드 성공 테스트")
  void successUploadManyFile() throws IOException {
    // given
    List<MultipartFile> multipartFiles = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      multipartFiles.add(createMockMultipartFile());
    }
    // when
    List<ImageDto> imageDtos = imageService.uploadManyFile(multipartFiles);
    // then
    for (ImageDto imageDto: imageDtos) {
      ResponseEntity<String> response = restTemplate.getForEntity(imageDto.getUrl(),
          String.class);

      Assertions.assertEquals(response.getBody(), "Upload Test");
      imageService.deleteFile(imageDto.getFilename());
    }
  }

  private MultipartFile createMockMultipartFile() throws IOException {
    // 임시 파일 생성
    Path tempFile = Files.createTempFile("testFile", ".txt");
    Files.write(tempFile, "Upload Test".getBytes());

    // MultipartFile 로 변환
    String originalFilename = "testFile.txt";
    String name = "file";
    String contentType = "text/plain";
    byte[] content = Files.readAllBytes(tempFile);

    return new MockMultipartFile(name, originalFilename, contentType, content);
  }

}