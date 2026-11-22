package com.file;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task-solutions")
public class TaskSolutionController {
    private final TaskSolutionService solutionService;

    public TaskSolutionController(TaskSolutionService solutionService) { this.solutionService = solutionService; }

    @GetMapping
    public ResponseEntity<List<TaskSolutionResponse>> getAll() { return ResponseEntity.ok(solutionService.getAllSolutions().stream().map(TaskSolutionResponse::from).toList()); }

    @GetMapping("/{id}")
    public ResponseEntity<TaskSolutionResponse> getById(@PathVariable Long id) {
        return solutionService.getSolutionById(id).map(TaskSolutionResponse::from).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // БИЗНЕС-ОПЕРАЦИЯ №2: Отправка ответа на задание с проверкой по эталону
    @PostMapping("/submit")
    public ResponseEntity<?> submitSolution(@RequestParam Long attemptId, @RequestParam Long taskId, @RequestBody Map<String, String> body) {
        try {
            String answer = body.get("answer");
            if (answer == null) return ResponseEntity.badRequest().body("Поле 'answer' обязательно");
            return ResponseEntity.status(HttpStatus.CREATED).body(TaskSolutionResponse.from(solutionService.submitTaskSolution(attemptId, taskId, answer)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return solutionService.deleteSolution(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
