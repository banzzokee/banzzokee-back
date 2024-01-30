package homes.banzzokee.domain.adoption.elasticsearch.testcontroller;

import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.type.S3Object;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/es")
@RequiredArgsConstructor
public class Test {

  private final AdoptionSearchRepository adoptionSearchRepository;

  @DeleteMapping
  public void test2() {
    adoptionSearchRepository.deleteAll();
  }

  @GetMapping
  public String test() {
    AdoptionDocument adoptionDocument = AdoptionDocument.builder()
        .id(1L)
        .breed("견종")
        .images(List.of(new S3Object("1"), new S3Object("2")))
        .build();
    adoptionSearchRepository.save(adoptionDocument);
    String string = adoptionSearchRepository.findByBreed("견종").get(0).getBreed();
    return string;
  }

}
