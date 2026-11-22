package com.file;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ExamTaskService {
    private final ExamTaskRepository examTaskRepository;
    private final ExamRepository examRepository;

    public ExamTaskService(ExamTaskRepository examTaskRepository, ExamRepository examRepository) {
        this.examTaskRepository = examTaskRepository;
        this.examRepository = examRepository;
    }

    public List<ExamTask> getAllTasks() { return examTaskRepository.findAll(); }
    public Optional<ExamTask> getTaskById(Long id) { return examTaskRepository.findById(id); }

    public ExamTask createTask(ExamTask task, Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Экзамен не найден"));
        requireDraft(exam);
        validateTask(task);
        task.setExam(exam);
        return examTaskRepository.save(task);
    }

    public Optional<ExamTask> updateTask(Long id, ExamTask updated, Long examId) {
        return examTaskRepository.findById(id).map(existing -> {
            requireDraft(existing.getExam());
            if (examId != null) {
                Exam newExam = examRepository.findById(examId)
                        .orElseThrow(() -> new IllegalArgumentException("Экзамен не найден"));
                requireDraft(newExam);
                existing.setExam(newExam);
            }
            if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
            if (updated.getReferenceAnswer() != null) existing.setReferenceAnswer(updated.getReferenceAnswer());
            if (updated.getMaxScore() != null) existing.setMaxScore(updated.getMaxScore());
            validateTask(existing);
            return examTaskRepository.save(existing);
        });
    }

    private void requireDraft(Exam exam) {
        if (!"DRAFT".equals(exam.getStatus())) throw new IllegalStateException("Изменение заданий возможно только в DRAFT");
    }

    private void validateTask(ExamTask task) {
        if (task.getDescription() == null || task.getDescription().isBlank()
                || task.getReferenceAnswer() == null || task.getReferenceAnswer().isBlank()
                || task.getMaxScore() == null || task.getMaxScore() < 0)
            throw new IllegalArgumentException("Нужны условие, эталонный ответ и неотрицательный балл");
    }

    public boolean deleteTask(Long id) {
        Optional<ExamTask> task = examTaskRepository.findById(id);
        if (task.isPresent()) {
            requireDraft(task.get().getExam());
            examTaskRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
