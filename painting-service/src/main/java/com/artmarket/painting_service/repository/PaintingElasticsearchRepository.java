package com.artmarket.painting_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.artmarket.painting_service.model.PaintingDoc;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PaintingElasticsearchRepository extends ElasticsearchRepository<PaintingDoc, Long> {
    @Query("""
            {
              "bool": {
                "should": [
                  {
                    "multi_match": {
                      "query": "?0",
                      "fields": ["title^4","author^3", "overview^2"],
                      "type": "best_fields",
                      "operator": "or"
                    }
                  },
                  {
                    "match_phrase": {
                      "movie": {
                        "query": "?0",
                        "boost": 3
                      }
                    }
                  },
                  {
                    "match_phrase": {
                      "overview": {
                        "query": "?0",
                        "boost": 2
                      }
                    }
                  }
                ],
                "minimum_should_match": 1
              }
            }
            """)
    Page<PaintingDoc> searchByQuery(String query, Pageable pageable);
}
