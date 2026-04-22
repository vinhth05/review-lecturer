package com.example.ctu.config;

import com.example.ctu.service.ToxicKeywordService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BootstrapConfig {

    @Bean
    CommandLineRunner bootstrapToxicKeywords(ToxicKeywordService toxicKeywordService) {
        return args -> toxicKeywordService.bootstrapDefaultsIfMissing();
    }
}
