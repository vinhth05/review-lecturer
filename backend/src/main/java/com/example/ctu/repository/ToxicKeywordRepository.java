package com.example.ctu.repository;

import com.example.ctu.entity.ToxicKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ToxicKeywordRepository extends JpaRepository<ToxicKeyword, Long> {
    Optional<ToxicKeyword> findByKeyword(String keyword);
    boolean existsByKeyword(String keyword);
    List<ToxicKeyword> findAllByOrderByKeywordAsc();
}
