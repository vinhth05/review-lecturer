package com.example.ctu.repository;

import com.example.ctu.entity.ToxicKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ToxicKeywordRepository extends JpaRepository<ToxicKeyword, UUID> {
    Optional<ToxicKeyword> findByKeyword(String keyword);
    boolean existsByKeyword(String keyword);
    List<ToxicKeyword> findAllByOrderByKeywordAsc();
}
