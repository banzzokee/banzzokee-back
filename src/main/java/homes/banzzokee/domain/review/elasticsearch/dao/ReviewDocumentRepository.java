package homes.banzzokee.domain.review.elasticsearch.dao;

import homes.banzzokee.domain.review.elasticsearch.document.ReviewDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewDocumentRepository extends
    ElasticsearchRepository<ReviewDocument, Long> {

}
