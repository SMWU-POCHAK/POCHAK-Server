package com.apps.pochak.elastic.like;

import com.apps.pochak.elastic.repository.ElasticsearchRepository;

public interface LikeElasticRepository extends ElasticsearchRepository<LikeDocument, String> {
}
