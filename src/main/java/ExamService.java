package com.file;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ExamService {
    private final ExamRepository examRepository;
    private final ExamTaskRepository examTaskRepository;

    public ExamService(ExamRepository examRepository, ExamTaskRepository examTaskRepository) {
        this.examRepository = examRepository;
        this.examTaskRepository = examTaskRepository;
    }

    public List<Exam> getAllExams() { return examRepository.findAll(); }
    public Optional<Exam> getExamById(Long id) { return examRepository.findById(id); }
    public Exam createExam(Exam exam) {
        validateExam(exam);
        exam.setStatus("DRAFT");
        return examRepository.save(exam);
    }

    public Optional<Exam> updateExam(Long id, Exam updated) {
        return examRepository.findById(id).map(existing -> {
            if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
            if (updated.getPassingScore() != null) existing.setPassingScore(updated.getPassingScore());
            if (updated.getTimeLimit() != null) existing.setTimeLimit(updated.getTimeLimit());
            validateExam(existing);
            if (!"DRAFT".equals(existing.getStatus())) throw new IllegalStateException("Опубликованный экзамен нельзя изменять");
            return examRepository.save(existing);
        });
    }

    private void validateExam(Exam exam) {
        if (exam.getTitle() == null || exam.getTitle().isBlank()
                || exam.getPassingScore() == null || exam.getPassingScore() < 0
                || exam.getTimeLimit() == null || exam.getTimeLimit() <= 0)
            throw new IllegalArgumentException("Нужны непустое название, неотрицательный проходной балл и положительный лимит минут");
    }

    public boolean deleteExam(Long id) {
        if (examRepository.existsById(id)) {
            examRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // БИЗНЕС-ОПЕРАЦИЯ №4: Публикация экзамена с проверкой на наличие заданий
    @Transactional
    public Exam publishExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Экзамен не найден"));

        List<ExamTask> tasks = examTaskRepository.findByExamId(examId);
        if (tasks.isEmpty()) {
            throw new IllegalStateException("Нельзя опубликовать экзамен без экзаменационных заданий!");
        }

        validateExam(exam);
        int max = 0;
        for (ExamTask task : tasks) {
            if (task.getMaxScore() == null || task.getMaxScore() < 0 || task.getReferenceAnswer() == null || task.getReferenceAnswer().isBlank())
                throw new IllegalStateException("Задание заполнено некорректно");
            max = Math.addExact(max, task.getMaxScore());
        }
        if (exam.getPassingScore() > max) throw new IllegalStateException("Проходной балл выше максимального");
        exam.setStatus("ACTIVE");
        return examRepository.save(exam);
    }
}
