package com.file;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class TaskSolutionService {
    private final TaskSolutionRepository solutionRepository;
    private final ExamAttemptRepository attemptRepository;
    private final ExamTaskRepository taskRepository;

    public TaskSolutionService(TaskSolutionRepository solutionRepository, ExamAttemptRepository attemptRepository, ExamTaskRepository taskRepository) {
        this.solutionRepository = solutionRepository;
        this.attemptRepository = attemptRepository;
        this.taskRepository = taskRepository;
    }

    public List<TaskSolution> getAllSolutions() { return solutionRepository.findAll(); }
    public Optional<TaskSolution> getSolutionById(Long id) { return solutionRepository.findById(id); }

    // БИЗНЕС-ОПЕРАЦИЯ №2: Прием решения задания с автоматической проверкой ответа
    @Transactional
    public TaskSolution submitTaskSolution(Long attemptId, Long taskId, String studentAnswer) {
        ExamAttempt attempt = attemptRepository.findByIdForUpdate(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Попытка экзамена не найдена"));
        ExamTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Экзаменационное задание не найдено"));

        if (!"IN_PROGRESS".equals(attempt.getStatus())) {
            throw new IllegalStateException("Нельзя добавлять решения в завершенную или отмененную попытку!");
        }

        if (ExamAttemptService.isExpired(attempt, java.time.LocalDateTime.now())) {
            throw new IllegalStateException("Время экзамена истекло");
        }
        if (!task.getExam().getId().equals(attempt.getExam().getId())) {
            throw new IllegalArgumentException("Задание не относится к экзамену этой попытки");
        }
        if (solutionRepository.existsByAttemptIdAndTaskId(attemptId, taskId)) {
            throw new IllegalStateException("Ответ на это задание уже отправлен");
        }
        if (studentAnswer == null || studentAnswer.isBlank()) {
            throw new IllegalArgumentException("Ответ не должен быть пустым");
        }
        TaskSolution solution = new TaskSolution();
        solution.setAttempt(attempt);
        solution.setTask(task);
        solution.setStudentAnswer(studentAnswer);

        // Бизнес-логика проверки: если ответ совпал с эталоном — даем максимум, иначе 0
        if (task.getReferenceAnswer().trim().equalsIgnoreCase(studentAnswer.trim())) {
            solution.setScore(task.getMaxScore());
        } else {
            solution.setScore(0);
        }

        return solutionRepository.save(solution);
    }

    @Transactional
    public boolean deleteSolution(Long id) {
        Optional<TaskSolution> existing = solutionRepository.findById(id);
        if (existing.isPresent()) {
            ExamAttempt attempt = attemptRepository.findByIdForUpdate(existing.get().getAttempt().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Попытка не найдена"));
            if (!"IN_PROGRESS".equals(attempt.getStatus())) {
                throw new IllegalStateException("Нельзя удалять решение завершенной попытки");
            }
            solutionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
