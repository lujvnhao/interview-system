package com.interview.repository;

import com.interview.entity.TagConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagConfigRepository extends JpaRepository<TagConfig, Long> {
    Optional<TagConfig> findByName(String name);
    void deleteByName(String name);
    boolean existsByName(String name);
}
