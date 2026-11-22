package com.file;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-attempts")
public class ExamAttemptController {
    private final ExamAttemptService examAttemptService;

    public ExamAttemptController(ExamAttemptService examAttemptService) { this.examAttemptService = examAttemptService; }

    @GetMapping
    public ResponseEntity<List<ExamAttempt>> getAll() { return ResponseEntity.ok(examAttemptService.getAllAttempts()); }

    @GetMapping("/{id}")
    public ResponseEntity<ExamAttempt> getById(@PathVariable Long id) {
        return examAttemptService.getAttemptById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // БИЗНЕС-ОПЕРАЦИЯ №1: Старт новой попытки
    @PostMapping("/start")
    public ResponseEntity<?> startAttempt(@RequestParam Long examId, @RequestParam Long userId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(examAttemptService.startExamAttempt(examId, userId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // БИЗНЕС-ОПЕРАЦИЯ №3: Финиш попытки с автоподсчетом
    @PostMapping("/{id}/finish")
    public ResponseEntity<?> finishAttempt(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(examAttemptService.finishExamAttempt(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // БИЗНЕС-ОПЕРАЦИЯ №5: Аналитический отчет по студенту
    @GetMapping("/report/{userId}")
    public ResponseEntity<?> getReport(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(examAttemptService.getStudentReport(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return examAttemptService.deleteAttempt(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
