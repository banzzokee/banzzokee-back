package homes.banzzokee.infra.fileupload.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.infra.fileupload.dto.FileDto;
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
  public FileDto uploadOneFile(MultipartFile multipartFile, FilePath path) {
    try {
      return uploadFile(multipartFile, path);
    } catch (IOException e) {
      throw new FileFailToUploadException();
    }
  }

  /**
   * 여러 개의 이미지 파일 업로드
   */
  @Transactional
  public List<FileDto> uploadManyFile(List<MultipartFile> multipartFiles,
      FilePath path) {
    return multipartFiles.stream()
        .map(multipartFile -> {
          try {
            return this.uploadFile(multipartFile, path);
          } catch (IOException e) {
            throw new FileFailToUploadException();
          }
        }).toList();
  }

  public void deleteFile(String filename) {
    amazonS3Client.deleteObject(bucketName, filename);
  }

  private FileDto uploadFile(MultipartFile multipartFile, FilePath path)
      throws IOException {
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(multipartFile.getContentType());
    objectMetadata.setContentLength(multipartFile.getSize());

    String extension = StringUtils.getFilenameExtension(
        multipartFile.getOriginalFilename());
    String filename = path + "/" + UUID.randomUUID() + "." + extension;

    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filename,
        multipartFile.getInputStream(), objectMetadata);

    amazonS3Client.putObject(putObjectRequest);

    String objectUrl = amazonS3Client.getUrl(bucketName, filename).toString();
    return new FileDto(objectUrl, filename);
  }
}