package homes.banzzokee.domain.adoption.elasticsearch.dao;

import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdoptionSearchRepository extends
    ElasticsearchRepository<AdoptionDocument, Long> {

  List<AdoptionDocument> findAllByReviewIsNotNull(Pageable pageable);

}
