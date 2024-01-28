package homes.banzzokee.infra.fileupload.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.s3.AmazonS3Client;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.infra.fileupload.dto.FileDto;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

  @Mock
  private AmazonS3Client amazonS3Client;

  @InjectMocks
  private FileUploadService fileUploadService;

  @BeforeEach
  void injectBucketNameField() throws NoSuchFieldException, IllegalAccessException {
    Field field = FileUploadService.class.getDeclaredField("bucketName");
    field.setAccessible(true);
    field.set(fileUploadService, "bucketName");
  }

  @Test
  @DisplayName("1개 파일 업로드 성공 테스트")
  void successUploadFile() throws IOException {
    //given
    MultipartFile multipartFile = createMockMultipartFile();

    given(amazonS3Client.getUrl(anyString(), anyString())).willReturn(
        new URL("https://imageUrl.com"));
    //when
    FileDto fileDto = fileUploadService.uploadOneFile(multipartFile,
        FilePath.ADOPTION);

    // then
    assertEquals(fileDto.getUrl(), "https://imageUrl.com");

  }

  @Test
  @DisplayName("여러 개 파일 업로드 성공 테스트")
  void successUploadManyFile() throws IOException {
    //given
    List<MultipartFile> multipartFiles = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      multipartFiles.add(createMockMultipartFile());
    }

    given(amazonS3Client.getUrl(anyString(), anyString())).willReturn(
        new URL("https://imageUrl.com"));
    //when
    List<FileDto> fileDtoList = fileUploadService.uploadManyFile(multipartFiles,
        FilePath.ADOPTION);
    //then
    assertEquals(3, fileDtoList.size());
    assertEquals("https://imageUrl.com", fileDtoList.get(0).getUrl());
    verify(amazonS3Client, times(3)).getUrl(any(), anyString());
  }

  @Test
  @DisplayName("업로드 파일 삭제 성공 테스트")
  void successDeleteFile() {
    //given
    String filename = "anything";
    //when
    fileUploadService.deleteFile(filename);
    //then
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(amazonS3Client, times(1)).deleteObject(any(), captor.capture());
    assertEquals(captor.getValue(), filename);
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