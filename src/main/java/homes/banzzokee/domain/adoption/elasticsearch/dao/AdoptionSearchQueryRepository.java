package homes.banzzokee.domain.adoption.elasticsearch.dao;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.json.JsonData;
import homes.banzzokee.domain.adoption.dto.AdoptionSearchRequest;
import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdoptionSearchQueryRepository {

  private final ElasticsearchOperations operations;

  public List<AdoptionDocument> findByAdoptionSearchRequest(
      AdoptionSearchRequest request, Pageable pageable) {
    NativeQuery query = createAdoptionSearchQuery(request, pageable);
    SearchHits<AdoptionDocument> search = operations.search(query,
        AdoptionDocument.class);

    return search.stream()
        .map(SearchHit::getContent)
        .collect(Collectors.toList());
  }

  private NativeQuery createAdoptionSearchQuery(AdoptionSearchRequest request,
      Pageable pageable) {
    NativeQueryBuilder query = new NativeQueryBuilder();

    if (request == null) {
      BoolQuery.Builder boolQueryBuilder = new Builder();
      boolQueryBuilder.mustNot(ExistsQuery.of(e -> e.field("deletedAt"))._toQuery());
      return query
          .withQuery(boolQueryBuilder.build()._toQuery())
          .withPageable(pageable)
          .build();
    }

    BoolQuery.Builder boolQueryBuilder = new Builder();

    if (request.getBreed() != null) {
      List<FieldValue> fieldValues = request.getBreed().stream()
          .map(breedType -> FieldValue.of(breedType.getValue()))
          .toList();

      Query breedQuery = TermsQuery.of(terms -> terms.field("breed")
          .terms(TermsQueryField.of(t -> t.value(fieldValues))))._toQuery();
      boolQueryBuilder.must(breedQuery);
    }

    if (request.getSize() != null) {
      Query sizeQuery = TermQuery.of(t -> t.field("size").value(request.getSize().getValue()))
          ._toQuery();
      boolQueryBuilder.must(sizeQuery);
    }

    if (request.getNeutering() != null) {
      Query neuteringQuery = TermQuery.of(
          t -> t.field("neutering").value(request.getNeutering()))._toQuery();
      boolQueryBuilder.must(neuteringQuery);
    }

    if (request.getHealthChecked() != null) {
      Query healthCheckedQuery = TermQuery.of(
          t -> t.field("healthChecked").value(request.getHealthChecked()))._toQuery();
      boolQueryBuilder.must(healthCheckedQuery);
    }

    if (request.getGender() != null) {
      Query genderQuery = TermQuery.of(t -> t.field("gender").value(request.getGender().getValue()))
          ._toQuery();
      boolQueryBuilder.must(genderQuery);
    }

    if (request.getAgeRange() != null) {
      JsonData minAge = JsonData.of(request.getAgeRange().getMinAge());
      JsonData maxAge = JsonData.of(request.getAgeRange().getMaxAge());
      Query ageRangeQuery = RangeQuery.of(
              r -> r.field("age").lte(maxAge).gte(minAge))
          ._toQuery();
      boolQueryBuilder.must(ageRangeQuery);
    }

    boolQueryBuilder.mustNot(ExistsQuery.of(e -> e.field("deletedAt"))._toQuery());

    return query
        .withQuery(boolQueryBuilder.build()._toQuery())
        .withPageable(pageable)
        .build();
  }


}
