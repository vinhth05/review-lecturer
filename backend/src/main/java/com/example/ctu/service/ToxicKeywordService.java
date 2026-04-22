package com.example.ctu.service;

import com.example.ctu.config.AppProperties;
import com.example.ctu.dto.admin.AdminDtos;
import com.example.ctu.entity.ToxicKeyword;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.repository.ToxicKeywordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ToxicKeywordService {

    private final ToxicKeywordRepository toxicKeywordRepository;
    private final AppProperties properties;

    public ToxicKeywordService(ToxicKeywordRepository toxicKeywordRepository, AppProperties properties) {
        this.toxicKeywordRepository = toxicKeywordRepository;
        this.properties = properties;
    }

    @Transactional
    public void bootstrapDefaultsIfMissing() {
        if (!toxicKeywordRepository.findAll().isEmpty()) {
            return;
        }
        List<String> defaults = properties.review().toxicKeywords();
        if (defaults == null || defaults.isEmpty()) {
            return;
        }
        defaults.stream()
                .map(this::normalize)
                .filter(value -> !value.isBlank())
                .distinct()
                .forEach(value -> {
                    ToxicKeyword entity = new ToxicKeyword();
                    entity.setKeyword(value);
                    toxicKeywordRepository.save(entity);
                });
    }

    public List<AdminDtos.ToxicKeywordItem> getAll() {
        return toxicKeywordRepository.findAllByOrderByKeywordAsc().stream()
                .map(item -> new AdminDtos.ToxicKeywordItem(item.getId(), item.getKeyword(), item.getCreatedAt()))
                .toList();
    }

    @Transactional
    public AdminDtos.ToxicKeywordItem add(AdminDtos.CreateToxicKeywordRequest request) {
        String normalized = normalize(request.keyword());
        if (normalized.isBlank()) {
            throw new BadRequestException("Keyword không hợp lệ");
        }
        if (toxicKeywordRepository.existsByKeyword(normalized)) {
            throw new BadRequestException("Keyword đã tồn tại");
        }
        ToxicKeyword entity = new ToxicKeyword();
        entity.setKeyword(normalized);
        ToxicKeyword saved = toxicKeywordRepository.save(entity);
        return new AdminDtos.ToxicKeywordItem(saved.getId(), saved.getKeyword(), saved.getCreatedAt());
    }

    @Transactional
    public void delete(UUID id) {
        ToxicKeyword entity = toxicKeywordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Keyword không tồn tại"));
        toxicKeywordRepository.delete(entity);
    }

    public Set<String> keywordSet() {
        return toxicKeywordRepository.findAllByOrderByKeywordAsc().stream()
                .map(ToxicKeyword::getKeyword)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
