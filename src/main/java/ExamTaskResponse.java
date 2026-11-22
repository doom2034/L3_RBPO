package com.file;

public record ExamTaskResponse(Long id, Long examId, String description, Integer maxScore) {
    public static ExamTaskResponse from(ExamTask task) {
        return new ExamTaskResponse(task.getId(), task.getExam().getId(), task.getDescription(), task.getMaxScore());
    }
}
