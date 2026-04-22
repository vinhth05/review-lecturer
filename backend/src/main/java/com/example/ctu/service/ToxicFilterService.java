package com.example.ctu.service;

import org.springframework.stereotype.Service;

@Service
public class ToxicFilterService {

    private final ToxicKeywordService toxicKeywordService;

    public ToxicFilterService(ToxicKeywordService toxicKeywordService) {
        this.toxicKeywordService = toxicKeywordService;
    }

    public boolean containsToxicWord(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = text.toLowerCase();
        return toxicKeywordService.keywordSet().stream()
                .anyMatch(normalized::contains);
    }
}
