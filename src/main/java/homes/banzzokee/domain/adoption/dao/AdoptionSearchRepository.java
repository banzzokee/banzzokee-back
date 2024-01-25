package homes.banzzokee.domain.adoption.dao;

import homes.banzzokee.domain.adoption.document.AdoptionDocument;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AdoptionSearchRepository extends
    ElasticsearchRepository<AdoptionDocument, Long> {

  List<AdoptionDocument> findByBreed(String breed);

}
