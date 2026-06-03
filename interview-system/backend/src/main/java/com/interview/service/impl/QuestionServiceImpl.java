package com.interview.service.impl;

import com.interview.common.exception.BusinessException;
import com.interview.common.exception.NotFoundException;
import com.interview.dto.QuestionDTO;
import com.interview.dto.ReviewResultDTO;
import com.interview.entity.Question;
import com.interview.repository.QuestionRepository;
import com.interview.service.QuestionService;
import com.interview.vo.CategoryMasteryItem;
import com.interview.vo.RecentReviewItem;
import com.interview.vo.ReviewOverviewVO;
import com.interview.vo.StatisticsVO;
import com.interview.vo.TrendItem;
import com.interview.vo.WeakCategoryItem;
import com.interview.vo.WrongRankItem;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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

    private static final int DEFAULT_WEIGHT = 10;
    private static final int MAX_PERSISTED_WEIGHT = 100;
    private static final double MIN_EFFECTIVE_WEIGHT = 1.0;
    private static final double MAX_EFFECTIVE_WEIGHT = 180.0;
    private static final double RECENT_MASTERED_SUPPRESSION_HOURS = 12.0;
    private static final int LONG_UNREVIEWED_DAYS = 14;

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
                .orElseThrow(() -> new NotFoundException("题目", id));
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
                .orElseThrow(() -> new NotFoundException("题目", id));
    }

    @Override
    public Page<Question> list(Pageable pageable, String keyword, String category,
                                String tag, Boolean mastered, Boolean favorite,
                                Boolean emptyTag, Boolean noAnswer, Boolean hotTag,
                                Boolean longUnreviewed, Integer staleDays) {
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
            if (Boolean.TRUE.equals(emptyTag)) {
                predicates.add(cb.or(
                        cb.isNull(root.get("tags")),
                        cb.equal(root.get("tags"), "")
                ));
            }
            if (Boolean.TRUE.equals(noAnswer)) {
                predicates.add(cb.or(
                        cb.isNull(root.get("answer")),
                        cb.equal(root.get("answer"), "")
                ));
            }
            if (Boolean.TRUE.equals(hotTag)) {
                predicates.add(cb.like(root.get("tags"), "%高频%"));
            }
            if (Boolean.TRUE.equals(longUnreviewed)) {
                int days = staleDays != null && staleDays > 0 ? staleDays : 14;
                LocalDateTime staleBefore = LocalDateTime.now().minusDays(days);
                predicates.add(cb.or(
                        cb.isNull(root.get("lastReviewTime")),
                        cb.lessThan(root.get("lastReviewTime"), staleBefore)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return questionRepository.findAll(spec, pageable);
    }

    // ==================== 间隔重复抽题 ====================

    @Override
    public Question randomQuestion(String mode, String category) {
        List<Question> pool;
        LocalDateTime now = LocalDateTime.now();
        boolean hasCategory = category != null && !category.isBlank() && !"全部".equals(category);

        switch (mode != null ? mode : "all") {
            case "favorites":
                pool = hasCategory
                        ? questionRepository.findByCategoryAndFavoriteTrue(category)
                        : questionRepository.findAll().stream()
                                .filter(Question::getFavorite)
                                .collect(Collectors.toList());
                break;
            case "unmastered":
                pool = hasCategory
                        ? questionRepository.findByCategoryAndMasteredFalseAndWrongCountGreaterThan(category, 0)
                        : questionRepository.findByMasteredFalseAndWrongCountGreaterThanOrderByWeightDesc(0);
                break;
            case "dueToday":
            case "today":
                // dueToday 涉及复杂业务逻辑，需加载候选集后内存判断
                List<Question> sourcePool = hasCategory
                        ? questionRepository.findByCategory(category)
                        : questionRepository.findAll();
                pool = sourcePool.stream()
                        .filter(q -> isDueToday(q, now))
                        .collect(Collectors.toList());
                break;
            case "all":
            default:
                pool = hasCategory
                        ? questionRepository.findByCategory(category)
                        : questionRepository.findAll();
                break;
        }

        if (pool.isEmpty()) {
            throw new BusinessException("没有可抽取的题目");
        }

        return weightedRandom(pool);
    }

    @Override
    public Page<Question> dueToday(Pageable pageable, String category) {
        LocalDateTime now = LocalDateTime.now();
        List<Question> dueQuestions = questionRepository.findAll().stream()
                .filter(q -> matchesCategory(q, category))
                .filter(q -> isDueToday(q, now))
                .sorted(Comparator.comparingDouble((Question q) -> spacedRepetitionWeight(q, now)).reversed())
                .collect(Collectors.toList());

        int start = Math.min((int) pageable.getOffset(), dueQuestions.size());
        int end = Math.min(start + pageable.getPageSize(), dueQuestions.size());
        return new PageImpl<>(dueQuestions.subList(start, end), pageable, dueQuestions.size());
    }

    @Override
    public ReviewOverviewVO reviewOverview() {
        LocalDateTime now = LocalDateTime.now();
        List<Question> questions = questionRepository.findAll();
        long dueTodayCount = questions.stream().filter(q -> isDueToday(q, now)).count();
        long highRiskCount = questions.stream().filter(this::isHighRisk).count();
        long longUnreviewedCount = questions.stream().filter(q -> isLongUnreviewed(q, now)).count();

        return ReviewOverviewVO.builder()
                .dueTodayCount(dueTodayCount)
                .highRiskCount(highRiskCount)
                .longUnreviewedCount(longUnreviewedCount)
                .build();
    }

    /**
     * 间隔重复加权随机算法。
     * 规则：
     * 1. weight 是持久化难度信号：答错上升，答对下降。
     * 2. 抽题时再叠加 wrongCount/correctCount/reviewCount/lastReviewTime/mastered 的动态优先级。
     * 3. 最近刚掌握的题会被短期压低；未掌握、错得多、长时间未复习的题会更容易被抽中。
     */
    private Question weightedRandom(List<Question> pool) {
        double totalWeight = 0;
        List<Double> cumulativeWeights = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Question q : pool) {
            double effectiveWeight = spacedRepetitionWeight(q, now);
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

    private double spacedRepetitionWeight(Question q, LocalDateTime now) {
        int weight = clampInt(safeInt(q.getWeight(), DEFAULT_WEIGHT), 1, MAX_PERSISTED_WEIGHT);
        int wrongCount = safeInt(q.getWrongCount(), 0);
        int correctCount = safeInt(q.getCorrectCount(), 0);
        int reviewCount = safeInt(q.getReviewCount(), 0);

        double wrongRate = reviewCount == 0 ? 0 : (double) wrongCount / reviewCount;
        double difficultyBoost = 1.0
                + Math.min(3.0, wrongCount * 0.32)
                + Math.min(1.8, wrongRate * 1.6);
        double reviewBoost = reviewCount == 0
                ? 1.8
                : 1.0 + Math.min(0.8, Math.log1p(reviewCount) * 0.12);

        double score = weight * difficultyBoost * reviewBoost;

        if (isTrue(q.getMastered())) {
            score *= masteredIntervalFactor(q.getLastReviewTime(), now, correctCount, wrongCount, reviewCount);
        } else {
            score *= unmasteredIntervalFactor(q.getLastReviewTime(), now, wrongCount);
        }

        if (isTrue(q.getFavorite())) {
            score *= 1.15;
        }

        return clamp(score, MIN_EFFECTIVE_WEIGHT, MAX_EFFECTIVE_WEIGHT);
    }

    private double masteredIntervalFactor(LocalDateTime lastReviewTime, LocalDateTime now,
                                          int correctCount, int wrongCount, int reviewCount) {
        if (lastReviewTime == null) {
            return 1.4;
        }

        double hoursSinceReview = hoursSince(lastReviewTime, now);
        if (hoursSinceReview < 0) {
            return 0.08;
        }
        if (hoursSinceReview < RECENT_MASTERED_SUPPRESSION_HOURS) {
            return 0.08 + (hoursSinceReview / RECENT_MASTERED_SUPPRESSION_HOURS) * 0.12;
        }

        double daysSinceReview = hoursSinceReview / 24.0;
        double targetIntervalDays = masteredTargetIntervalDays(correctCount, wrongCount, reviewCount);
        double overdueRatio = daysSinceReview / targetIntervalDays;
        return clamp(0.25 + overdueRatio * 1.4, 0.25, 2.6);
    }

    private double unmasteredIntervalFactor(LocalDateTime lastReviewTime, LocalDateTime now, int wrongCount) {
        double wrongBoost = 1.35 + Math.min(2.4, wrongCount * 0.35);

        if (lastReviewTime == null) {
            return wrongCount > 0 ? wrongBoost * 1.5 : 1.5;
        }

        double hoursSinceReview = hoursSince(lastReviewTime, now);
        if (hoursSinceReview < 0) {
            return wrongBoost;
        }
        if (hoursSinceReview < 0.5) {
            return wrongBoost * 0.6;
        }
        if (hoursSinceReview < 6) {
            return wrongBoost * (0.9 + hoursSinceReview / 12.0);
        }

        double daysSinceReview = hoursSinceReview / 24.0;
        return wrongBoost * clamp(1.4 + daysSinceReview * 0.45, 1.4, 3.5);
    }

    // ==================== 复习反馈 ====================

    @Override
    @Transactional
    public Question submitReview(Long id, ReviewResultDTO dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("题目", id));

        question.setReviewCount(safeInt(question.getReviewCount(), 0) + 1);
        question.setLastReviewTime(LocalDateTime.now());

        if (dto.getMastered() != null && dto.getMastered()) {
            // 掌握会降低持久权重；连续答对越多，之后动态评分中的复习间隔也会越长。
            question.setMastered(true);
            question.setCorrectCount(safeInt(question.getCorrectCount(), 0) + 1);
            int reduction = 2 + Math.min(3, safeInt(question.getCorrectCount(), 0) / 2);
            question.setWeight(Math.max(1, safeInt(question.getWeight(), DEFAULT_WEIGHT) - reduction));
        } else {
            // 未掌握会提高持久权重；错得越多，下一轮动态评分越偏向该题。
            question.setMastered(false);
            question.setWrongCount(safeInt(question.getWrongCount(), 0) + 1);
            int increase = 6 + Math.min(10, safeInt(question.getWrongCount(), 0) * 2);
            question.setWeight(Math.min(MAX_PERSISTED_WEIGHT,
                    safeInt(question.getWeight(), DEFAULT_WEIGHT) + increase));
            question.setFavorite(true);
        }

        return questionRepository.save(question);
    }

    // ==================== 答案 ====================

    @Override
    @Transactional
    public Question updateAnswer(Long id, String answer) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("题目", id));
        question.setAnswer(answer != null ? answer.trim() : null);
        return questionRepository.save(question);
    }

    // ==================== 收藏 ====================

    @Override
    @Transactional
    public Question toggleFavorite(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("题目", id));
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
        List<Question> questions = questionRepository.findAll();
        long total = questions.size();
        long mastered = questions.stream().filter(q -> isTrue(q.getMastered())).count();
        long unmastered = total - mastered;
        long favorites = questions.stream().filter(q -> isTrue(q.getFavorite())).count();

        // 今日复习数
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayReviews = questionRepository.countTodayReviews(startOfDay);

        // 各分类题目数
        Map<String, Long> categoryDist = new HashMap<>();
        List<Object[]> categoryCounts = questionRepository.countByCategory();
        for (Object[] row : categoryCounts) {
            categoryDist.put((String) row[0], (Long) row[1]);
        }

        Map<String, List<Question>> questionsByCategory = questions.stream()
                .collect(Collectors.groupingBy(q -> normalizeCategory(q.getCategory()), TreeMap::new, Collectors.toList()));
        List<CategoryMasteryItem> categoryMastery = buildCategoryMastery(questionsByCategory);
        List<TrendItem> reviewTrend = buildSevenDayReviewTrend(questions);
        List<TrendItem> wrongGrowthTrend = buildSevenDayWrongTrend(questions);
        List<WeakCategoryItem> weakCategoryRank = buildWeakCategoryRank(questionsByCategory);

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
                .masteryRate(rate(mastered, total))
                .categoryDistribution(categoryDist)
                .categoryMastery(categoryMastery)
                .reviewTrend(reviewTrend)
                .wrongGrowthTrend(wrongGrowthTrend)
                .weakCategoryRank(weakCategoryRank)
                .wrongRank(wrongRank)
                .recentReviews(recentReviews)
                .build();
    }

    private List<CategoryMasteryItem> buildCategoryMastery(Map<String, List<Question>> questionsByCategory) {
        return questionsByCategory.entrySet().stream()
                .map(entry -> {
                    List<Question> items = entry.getValue();
                    long total = items.size();
                    long mastered = items.stream().filter(q -> isTrue(q.getMastered())).count();
                    int wrongCount = items.stream().mapToInt(q -> safeInt(q.getWrongCount(), 0)).sum();
                    return CategoryMasteryItem.builder()
                            .category(entry.getKey())
                            .total(total)
                            .mastered(mastered)
                            .unmastered(total - mastered)
                            .wrongCount(wrongCount)
                            .masteryRate(rate(mastered, total))
                            .build();
                })
                .sorted(Comparator.comparing(CategoryMasteryItem::getCategory))
                .collect(Collectors.toList());
    }

    private List<TrendItem> buildSevenDayReviewTrend(List<Question> questions) {
        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> countByDay = questions.stream()
                .map(Question::getLastReviewTime)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .filter(day -> !day.isBefore(today.minusDays(6)) && !day.isAfter(today))
                .collect(Collectors.groupingBy(day -> day, Collectors.counting()));

        return buildSevenDayTrend(countByDay);
    }

    private List<TrendItem> buildSevenDayWrongTrend(List<Question> questions) {
        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> wrongCountByDay = questions.stream()
                .filter(q -> q.getLastReviewTime() != null)
                .filter(q -> {
                    LocalDate day = q.getLastReviewTime().toLocalDate();
                    return !day.isBefore(today.minusDays(6)) && !day.isAfter(today);
                })
                .collect(Collectors.groupingBy(
                        q -> q.getLastReviewTime().toLocalDate(),
                        Collectors.summingLong(q -> safeInt(q.getWrongCount(), 0))
                ));

        return buildSevenDayTrend(wrongCountByDay);
    }

    private List<TrendItem> buildSevenDayTrend(Map<LocalDate, Long> valueByDay) {
        LocalDate today = LocalDate.now();
        List<TrendItem> trend = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            trend.add(TrendItem.builder()
                    .date(day.toString())
                    .label(day.getMonthValue() + "/" + day.getDayOfMonth())
                    .value(valueByDay.getOrDefault(day, 0L))
                    .build());
        }

        return trend;
    }

    private List<WeakCategoryItem> buildWeakCategoryRank(Map<String, List<Question>> questionsByCategory) {
        return questionsByCategory.entrySet().stream()
                .map(entry -> {
                    List<Question> items = entry.getValue();
                    long total = items.size();
                    long mastered = items.stream().filter(q -> isTrue(q.getMastered())).count();
                    long reviewCount = items.stream().mapToLong(q -> safeInt(q.getReviewCount(), 0)).sum();
                    int wrongCount = items.stream().mapToInt(q -> safeInt(q.getWrongCount(), 0)).sum();
                    double masteryRate = rate(mastered, total);
                    double riskScore = (100 - masteryRate) * 0.45 + wrongCount * 2.5 + (total - mastered) * 0.35;

                    return WeakCategoryItem.builder()
                            .category(entry.getKey())
                            .total(total)
                            .mastered(mastered)
                            .unmastered(total - mastered)
                            .reviewCount(reviewCount)
                            .wrongCount(wrongCount)
                            .masteryRate(masteryRate)
                            .riskScore(roundOneDecimal(riskScore))
                            .build();
                })
                .filter(item -> item.getTotal() > 0)
                .sorted(Comparator
                        .comparingDouble(WeakCategoryItem::getRiskScore).reversed()
                        .thenComparing(WeakCategoryItem::getWrongCount, Comparator.reverseOrder()))
                .limit(8)
                .collect(Collectors.toList());
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }

    private String normalizeCategory(String category) {
        return category == null || category.isBlank() ? "未分类" : category;
    }

    private double rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return roundOneDecimal(numerator * 100.0 / denominator);
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private int safeInt(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }

    private boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private int clampInt(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }

    private double hoursSince(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMinutes() / 60.0;
    }

    private boolean isDueToday(Question question, LocalDateTime now) {
        LocalDateTime lastReviewTime = question.getLastReviewTime();
        int wrongCount = safeInt(question.getWrongCount(), 0);
        int correctCount = safeInt(question.getCorrectCount(), 0);
        int reviewCount = safeInt(question.getReviewCount(), 0);

        if (lastReviewTime == null) {
            return true;
        }

        double hoursSinceReview = hoursSince(lastReviewTime, now);
        if (hoursSinceReview < 0) {
            return false;
        }

        // 未掌握题按短间隔复习；刚提交过的题留出半小时缓冲，避免立即反复抽中。
        if (!isTrue(question.getMastered())) {
            return wrongCount > 0 ? hoursSinceReview >= 0.5 : hoursSinceReview >= 24;
        }

        double daysSinceReview = hoursSinceReview / 24.0;
        return daysSinceReview >= masteredTargetIntervalDays(correctCount, wrongCount, reviewCount);
    }

    private boolean isHighRisk(Question question) {
        int wrongCount = safeInt(question.getWrongCount(), 0);
        int reviewCount = safeInt(question.getReviewCount(), 0);
        int weight = safeInt(question.getWeight(), DEFAULT_WEIGHT);
        double wrongRate = reviewCount == 0 ? 0 : (double) wrongCount / reviewCount;

        return wrongCount >= 3
                || weight >= 30
                || (!isTrue(question.getMastered()) && wrongCount > 0 && wrongRate >= 0.5);
    }

    private boolean isLongUnreviewed(Question question, LocalDateTime now) {
        LocalDateTime lastReviewTime = question.getLastReviewTime();
        return lastReviewTime == null || lastReviewTime.isBefore(now.minusDays(LONG_UNREVIEWED_DAYS));
    }

    private boolean matchesCategory(Question question, String category) {
        return category == null
                || category.isBlank()
                || "全部".equals(category)
                || category.equals(question.getCategory());
    }

    private double masteredTargetIntervalDays(int correctCount, int wrongCount, int reviewCount) {
        return clamp(
                1.0 + correctCount * 1.8 + reviewCount * 0.25 - wrongCount * 0.8,
                1.0,
                30.0
        );
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
        return questionRepository.findDistinctCategories();
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
