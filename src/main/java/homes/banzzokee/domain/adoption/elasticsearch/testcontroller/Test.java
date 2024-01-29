package homes.banzzokee.domain.adoption.elasticsearch.testcontroller;

import homes.banzzokee.domain.adoption.elasticsearch.dao.AdoptionSearchRepository;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/es")
@RequiredArgsConstructor
public class Test {

  private final AdoptionSearchRepository adoptionSearchRepository;

  @GetMapping
  public String test() {
    AdoptionDocument adoptionDocument = AdoptionDocument.builder()
        .id(1L)
        .breed("견종")
        .build();
    adoptionSearchRepository.save(adoptionDocument);
    String string = adoptionSearchRepository.findByBreed("견종").get(0).getBreed();
    return string;
  }

}