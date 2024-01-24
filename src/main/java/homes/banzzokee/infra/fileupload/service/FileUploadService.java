package homes.banzzokee.infra.fileupload.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import homes.banzzokee.infra.fileupload.dto.ImageDto;
import homes.banzzokee.infra.fileupload.exception.FileFailToUploadException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileUploadService {

  private final AmazonS3Client amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  /**
   * 1개의 이미지 파일 업로드
   */
  public ImageDto uploadOneFile(MultipartFile multipartFile) {
    try {
      return uploadFile(multipartFile);
    } catch (IOException e) {
      throw new FileFailToUploadException();
    }
  }

  /**
   * 여러 개의 이미지 파일 업로드
   */
  @Transactional
  public List<ImageDto> uploadManyFile(List<MultipartFile> multipartFiles) {
    return multipartFiles.stream()
        .map(multipartFile -> {
          try {
            return this.uploadFile(multipartFile);
          } catch (IOException e) {
            throw new FileFailToUploadException();
          }
        }).toList();
  }

  public void deleteFile(String filename) {
    amazonS3Client.deleteObject(bucketName, filename);
  }

  private ImageDto uploadFile(MultipartFile multipartFile) throws IOException {
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
  }
}