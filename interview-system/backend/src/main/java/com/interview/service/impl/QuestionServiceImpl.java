package com.interview.service.impl;

import com.interview.dto.QuestionDTO;
import com.interview.dto.ReviewResultDTO;
import com.interview.entity.Question;
import com.interview.repository.QuestionRepository;
import com.interview.service.QuestionService;
import com.interview.vo.RecentReviewItem;
import com.interview.vo.StatisticsVO;
import com.interview.vo.WrongRankItem;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    // ==================== CRUD ====================

    @Override
    @Transactional
    public Question create(QuestionDTO dto) {
        Question question = Question.builder()
                .question(dto.getQuestion().trim())
                .answer(dto.getAnswer() != null ? dto.getAnswer().trim() : null)
                .category(dto.getCategory() != null ? dto.getCategory().trim() : "未分类")
                .tags(dto.getTags() != null ? dto.getTags().trim() : "")
                .mastered(dto.getMastered() != null ? dto.getMastered() : false)
                .favorite(dto.getFavorite() != null ? dto.getFavorite() : false)
                .weight(dto.getWeight() != null ? dto.getWeight() : 10)
                .wrongCount(0)
                .correctCount(0)
                .reviewCount(0)
                .build();
        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public Question update(Long id, QuestionDTO dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + id));
        question.setQuestion(dto.getQuestion().trim());
        question.setAnswer(dto.getAnswer() != null ? dto.getAnswer().trim() : null);
        question.setCategory(dto.getCategory() != null ? dto.getCategory().trim() : "未分类");
        question.setTags(dto.getTags() != null ? dto.getTags().trim() : "");
        question.setMastered(dto.getMastered() != null ? dto.getMastered() : question.getMastered());
        question.setFavorite(dto.getFavorite() != null ? dto.getFavorite() : question.getFavorite());
        question.setWeight(dto.getWeight() != null ? dto.getWeight() : question.getWeight());
        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        questionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        questionRepository.deleteAllById(ids);
    }

    @Override
    public Question getById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + id));
    }

    @Override
    public Page<Question> list(Pageable pageable, String keyword, String category,
                                String tag, Boolean mastered, Boolean favorite) {
        Specification<Question> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("question"), kw),
                        cb.like(root.get("answer"), kw)
                ));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category.trim()));
            }
            if (tag != null && !tag.isBlank()) {
                predicates.add(cb.like(root.get("tags"), "%" + tag.trim() + "%"));
            }
            if (mastered != null) {
                predicates.add(cb.equal(root.get("mastered"), mastered));
            }
            if (favorite != null) {
                predicates.add(cb.equal(root.get("favorite"), favorite));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return questionRepository.findAll(spec, pageable);
    }

    // ==================== 加权随机抽题 ====================

    @Override
    public Question randomQuestion(String mode, String category) {
        List<Question> pool;

        switch (mode != null ? mode : "all") {
            case "favorites":
                pool = questionRepository.findAll().stream()
                        .filter(Question::getFavorite)
                        .collect(Collectors.toList());
                break;
            case "unmastered":
                pool = questionRepository.findByMasteredFalseAndWrongCountGreaterThan(0);
                break;
            case "all":
            default:
                pool = questionRepository.findAll();
                break;
        }

        // 如果指定了分类，再过滤
        if (category != null && !category.isBlank() && !"全部".equals(category)) {
            pool = pool.stream()
                    .filter(q -> category.equals(q.getCategory()))
                    .collect(Collectors.toList());
        }

        if (pool.isEmpty()) {
            throw new IllegalArgumentException("没有可抽取的题目");
        }

        return weightedRandom(pool);
    }

    /**
     * 加权随机算法
     * weight 越高，被抽中的概率越大
     * 已掌握的题目权重会被打折（乘以0.3），降低被抽中概率
     */
    private Question weightedRandom(List<Question> pool) {
        double totalWeight = 0;
        List<Double> cumulativeWeights = new ArrayList<>();

        for (Question q : pool) {
            double effectiveWeight = q.getWeight();

            // 已掌握的题目降低权重
            if (q.getMastered()) {
                effectiveWeight *= 0.3;
            }

            // 收藏题目增加权重
            if (q.getFavorite()) {
                effectiveWeight *= 1.3;
            }

            // 权重不低于1
            effectiveWeight = Math.max(effectiveWeight, 1.0);

            totalWeight += effectiveWeight;
            cumulativeWeights.add(totalWeight);
        }

        // 生成随机数 [0, totalWeight)
        double random = Math.random() * totalWeight;

        // 二分查找
        int left = 0, right = cumulativeWeights.size() - 1;
        while (left < right) {
            int mid = (left + right) / 2;
            if (cumulativeWeights.get(mid) <= random) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        return pool.get(left);
    }

    // ==================== 复习反馈 ====================

    @Override
    @Transactional
    public Question submitReview(Long id, ReviewResultDTO dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + id));

        question.setReviewCount(question.getReviewCount() + 1);
        question.setLastReviewTime(LocalDateTime.now());

        if (dto.getMastered() != null && dto.getMastered()) {
            // 掌握
            question.setMastered(true);
            question.setCorrectCount(question.getCorrectCount() + 1);
            // 降低权重（最低为1）
            question.setWeight(Math.max(1, question.getWeight() - 2));
        } else {
            // 没掌握
            question.setMastered(false);
            question.setWrongCount(question.getWrongCount() + 1);
            // 增加权重
            question.setWeight(question.getWeight() + 5);
            // 自动收藏
            question.setFavorite(true);
        }

        return questionRepository.save(question);
    }

    // ==================== 答案 ====================

    @Override
    @Transactional
    public Question updateAnswer(Long id, String answer) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + id));
        question.setAnswer(answer != null ? answer.trim() : null);
        return questionRepository.save(question);
    }

    // ==================== 收藏 ====================

    @Override
    @Transactional
    public Question toggleFavorite(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在: " + id));
        question.setFavorite(!question.getFavorite());
        return questionRepository.save(question);
    }

    @Override
    public Page<Question> favorites(Pageable pageable) {
        return questionRepository.findByFavoriteTrue(pageable);
    }

    // ==================== 未掌握 ====================

    @Override
    public Page<Question> unmastered(Pageable pageable) {
        return questionRepository.findByMasteredFalseAndWrongCountGreaterThan(0, pageable);
    }

    // ==================== 统计 ====================

    @Override
    public StatisticsVO statistics() {
        long total = questionRepository.count();
        long mastered = questionRepository.findAll().stream().filter(Question::getMastered).count();
        long unmastered = total - mastered;
        long favorites = questionRepository.findAll().stream().filter(Question::getFavorite).count();

        // 今日复习数
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayReviews = questionRepository.countTodayReviews(startOfDay);

        // 各分类题目数
        Map<String, Long> categoryDist = new HashMap<>();
        List<Object[]> categoryCounts = questionRepository.countByCategory();
        for (Object[] row : categoryCounts) {
            categoryDist.put((String) row[0], (Long) row[1]);
        }

        // 错题排行榜
        List<WrongRankItem> wrongRank = questionRepository
                .findTop10ByWrongCountGreaterThanOrderByWrongCountDesc(0)
                .stream()
                .map(q -> new WrongRankItem(q.getId(), truncate(q.getQuestion(), 30), q.getWrongCount()))
                .collect(Collectors.toList());

        // 最近复习
        List<RecentReviewItem> recentReviews = questionRepository
                .findTop10ByLastReviewTimeIsNotNullOrderByLastReviewTimeDesc()
                .stream()
                .map(q -> new RecentReviewItem(q.getId(), truncate(q.getQuestion(), 30),
                        q.getCategory(), q.getLastReviewTime()))
                .collect(Collectors.toList());

        return StatisticsVO.builder()
                .totalQuestions(total)
                .masteredQuestions(mastered)
                .unmasteredQuestions(unmastered)
                .favoriteQuestions(favorites)
                .todayReviewCount(todayReviews)
                .categoryDistribution(categoryDist)
                .wrongRank(wrongRank)
                .recentReviews(recentReviews)
                .build();
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }

    // ==================== 导入导出 ====================

    @Override
    public List<Question> exportAll() {
        return questionRepository.findAllByOrderByIdAsc();
    }

    @Override
    @Transactional
    public Map<String, Object> importFromJson(List<QuestionDTO> dtos) {
        int added = 0;
        int skipped = 0;

        for (QuestionDTO dto : dtos) {
            if (dto.getQuestion() == null || dto.getQuestion().isBlank()) {
                skipped++;
                continue;
            }
            // 判断重复
            Optional<Question> existing = questionRepository.findByQuestion(dto.getQuestion().trim());
            if (existing.isPresent()) {
                skipped++;
                continue;
            }
            create(dto);
            added++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("added", added);
        result.put("skipped", skipped);
        return result;
    }

    // ==================== 分类与标签 ====================

    /** 返回所有不重复的分类列表 */
    @Override
    public List<String> allCategories() {
        return questionRepository.findAll().stream()
                .map(Question::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** 返回所有不重复的标签列表 */
    @Override
    public List<String> allTags() {
        return questionRepository.findAll().stream()
                .map(Question::getTags)
                .filter(t -> t != null && !t.isBlank())
                .flatMap(t -> Arrays.stream(t.split(",")))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
