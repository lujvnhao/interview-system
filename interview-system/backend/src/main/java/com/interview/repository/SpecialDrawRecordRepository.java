package com.interview.repository;

import com.interview.entity.SpecialDrawRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialDrawRecordRepository extends JpaRepository<SpecialDrawRecord, Long> {

    @Query("SELECT r.questionId FROM SpecialDrawRecord r WHERE r.category = :category AND r.drawDate = :drawDate")
    List<Long> findQuestionIdsByCategoryAndDrawDate(@Param("category") String category,
                                                    @Param("drawDate") LocalDate drawDate);

    List<SpecialDrawRecord> findByCategoryAndDrawDate(String category, LocalDate drawDate);

    Optional<SpecialDrawRecord> findByCategoryAndQuestionIdAndDrawDate(String category,
                                                                       Long questionId,
                                                                       LocalDate drawDate);

    @Query("SELECT COALESCE(MAX(r.batchNo), 0) FROM SpecialDrawRecord r WHERE r.category = :category AND r.drawDate = :drawDate")
    Integer findMaxBatchNoByCategoryAndDrawDate(@Param("category") String category,
                                                @Param("drawDate") LocalDate drawDate);

    long countByCategoryAndDrawDate(String category, LocalDate drawDate);
}
