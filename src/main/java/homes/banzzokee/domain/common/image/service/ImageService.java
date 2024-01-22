package homes.banzzokee.domain.common.image.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import homes.banzzokee.domain.common.image.dto.ImageDto;
import homes.banzzokee.domain.common.image.exception.ImageFailToUploadException;
import homes.banzzokee.global.error.ErrorCode;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

  private final AmazonS3Client amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  /**
   * 1개의 이미지 파일 업로드(비동기)
   */
  public ImageDto uploadOneFile(MultipartFile multipartFile) {
    return uploadFile(multipartFile).join();
  }

  /**
   * 여러 개의 이미지 파일 업로드(비동기 처리)
   */
  public List<ImageDto> uploadManyFile(List<MultipartFile> multipartFiles) {
    List<CompletableFuture<ImageDto>> futures = multipartFiles.stream()
        .map(this::uploadFile).toList();

    return CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]))
        .thenApply(Void -> futures.stream()
            .map(CompletableFuture::join)
            .toList()).join();
  }

  private CompletableFuture<ImageDto> uploadFile(MultipartFile multipartFile) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        String extension = StringUtils.getFilenameExtension(
            multipartFile.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filename,
            multipartFile.getInputStream(), objectMetadata);

        amazonS3Client.putObject(putObjectRequest);

        String objectUrl = amazonS3Client.getUrl(bucketName, filename).toString();
        return new ImageDto(objectUrl, filename);
      } catch (IOException e) {
        throw new ImageFailToUploadException(ErrorCode.FAIL_TO_UPLOAD_FILE);
      }
    });
  }
}
