package com.file;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ExamAttemptService {
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final TaskSolutionRepository taskSolutionRepository;

    public ExamAttemptService(ExamAttemptRepository examAttemptRepository, ExamRepository examRepository,
                              UserRepository userRepository, TaskSolutionRepository taskSolutionRepository) {
        this.examAttemptRepository = examAttemptRepository;
        this.examRepository = examRepository;
        this.userRepository = userRepository;
        this.taskSolutionRepository = taskSolutionRepository;
    }

    public List<ExamAttempt> getAllAttempts() { return examAttemptRepository.findAll(); }
    public Optional<ExamAttempt> getAttemptById(Long id) { return examAttemptRepository.findById(id); }

    // БИЗНЕС-ОПЕРАЦИЯ №1: Старт попытки (с проверками состояния объектов)
    @Transactional
    public ExamAttempt startExamAttempt(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Экзамен не найден"));
        User user = userRepository.findWithLockById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (!"ACTIVE".equals(exam.getStatus())) {
            throw new IllegalStateException("Нельзя начать экзамен, который не находится в статусе ACTIVE!");
        }

        List<ExamAttempt> activeAttempts = examAttemptRepository.findByStudentIdAndStatus(userId, "IN_PROGRESS");
        // Previous expired attempts cannot remain active forever.
        for (ExamAttempt active : activeAttempts) {
            if (isExpired(active, LocalDateTime.now())) {
                finishExamAttempt(active.getId());
            } else {
                throw new IllegalStateException("У вас уже есть незавершенный экзамен в процессе сдачи!");
            }
        }

        ExamAttempt attempt = new ExamAttempt();
        attempt.setExam(exam);
        attempt.setStudent(user);
        attempt.setStartTime(LocalDateTime.now()); // Серверное время
        attempt.setStatus("IN_PROGRESS");
        attempt.setFinalScore(0);
        return examAttemptRepository.save(attempt);
    }

    // БИЗНЕС-ОПЕРАЦИЯ №3: Завершение экзамена с подсчетом итогов
    @Transactional
    public ExamAttempt finishExamAttempt(Long attemptId) {
        ExamAttempt attempt = examAttemptRepository.findByIdForUpdate(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Попытка не найдена"));

        if (!"IN_PROGRESS".equals(attempt.getStatus())) {
            throw new IllegalStateException("Эта попытка уже была завершена ранее!");
        }

        // Считаем сумму баллов за все присланные решения этой попытки
        List<TaskSolution> solutions = taskSolutionRepository.findByAttemptId(attemptId);
        int totalScore = solutions.stream().mapToInt(TaskSolution::getScore).sum();

        attempt.setFinalScore(totalScore);
        attempt.setEndTime(LocalDateTime.now()); // Серверное время завершения

        // Сверяем с проходным баллом экзамена
        if (!isExpired(attempt, LocalDateTime.now()) && totalScore >= attempt.getExam().getPassingScore()) {
            attempt.setStatus("PASSED");
        } else {
            attempt.setStatus("FAILED");
        }

        return examAttemptRepository.save(attempt);
    }

    // БИЗНЕС-ОПЕРАЦИЯ №5: Комплексный отчет/аналитика по студенту
    public Map<String, Object> getStudentReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        List<ExamAttempt> allUserAttempts = examAttemptRepository.findByStudentId(userId);

        long totalAttempts = allUserAttempts.size();
        long passedCount = allUserAttempts.stream().filter(a -> "PASSED".equals(a.getStatus())).count();
        int maxScore = allUserAttempts.stream().mapToInt(a -> a.getFinalScore() != null ? a.getFinalScore() : 0).max().orElse(0);

        Map<String, Object> report = new HashMap<>();
        report.put("studentName", user.getName());
        report.put("totalAttempts", totalAttempts);
        report.put("successfulExams", passedCount);
        report.put("maxScoreAchieved", maxScore);
        report.put("successRatePercentage", totalAttempts > 0 ? ((double) passedCount / totalAttempts) * 100 : 0.0);

        return report;
    }

    public static boolean isExpired(ExamAttempt attempt, LocalDateTime now) {
        Integer minutes = attempt.getExam().getTimeLimit();
        return minutes != null && minutes > 0
                && !now.isBefore(attempt.getStartTime().plusMinutes(minutes));
    }

    public boolean deleteAttempt(Long id) {
        if (examAttemptRepository.existsById(id)) {
            examAttemptRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
