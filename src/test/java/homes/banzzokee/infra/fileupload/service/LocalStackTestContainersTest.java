package homes.banzzokee.infra.fileupload.service;


import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.testcontainers.utility.DockerImageName;

@Disabled
@Testcontainers
public class LocalStackTestContainersTest {

  private static final DockerImageName LOCALSTACK_IMAGE = DockerImageName.parse(
      "localstack/localstack");

  @Container
  LocalStackContainer localStackContainer = new LocalStackContainer(LOCALSTACK_IMAGE)
      .withServices(S3);

  @Test
  void test() {
    AmazonS3 amazonS3 = AmazonS3ClientBuilder
        .standard()
        .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(S3))
        .withCredentials(localStackContainer.getDefaultCredentialsProvider())
        .build();

    String bucketName = "banzzokee";
    amazonS3.createBucket(bucketName);
    System.out.println(bucketName + " 버킷 생성");

    String key = "filename";
    String content = "fileContent";
    amazonS3.putObject(bucketName, key, content);
    System.out.println("파일을 업로드하였습니다. key=" + key + ", content=" + content);

    List<String> results = IOUtils.readLines(
        amazonS3.getObject(bucketName, key).getObjectContent(), "utf-8");
    System.out.println("파일을 가져왔습니다. = " + results);

    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals(content, results.get(0));
  }
}