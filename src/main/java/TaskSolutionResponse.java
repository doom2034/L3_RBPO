package com.file;

public record TaskSolutionResponse(Long id, Long attemptId, Long taskId, String studentAnswer, Integer score) {
    public static TaskSolutionResponse from(TaskSolution solution) {
        return new TaskSolutionResponse(solution.getId(), solution.getAttempt().getId(),
                solution.getTask().getId(), solution.getStudentAnswer(), solution.getScore());
    }
}
