package com.apps.pochak.elastic.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories
@RequiredArgsConstructor
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Value("${elasticsearch.member-index}")
    private String memberIndex;

    @Value("${elasticsearch.like-index}")
    private String likeIndex;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchHost)
                .usingSsl()
                .withBasicAuth(username, password)
                .build();
    }

    @Bean
    public String memberIndex() {
        return memberIndex;
    }

    @Bean
    public String likeIndex() {
        return likeIndex;
    }
}
