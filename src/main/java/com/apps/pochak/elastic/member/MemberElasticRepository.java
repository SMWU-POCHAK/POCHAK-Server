package com.apps.pochak.elastic.member;

import com.apps.pochak.elastic.repository.ElasticsearchRepository;

public interface MemberElasticRepository extends ElasticsearchRepository<MemberDocument, String> {
}
