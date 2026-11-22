package com.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskSolutionServiceTest {
    @Mock TaskSolutionRepository solutions;
    @Mock ExamAttemptRepository attempts;
    @Mock ExamTaskRepository tasks;
    TaskSolutionService service;
    Exam exam;
    ExamTask task;
    ExamAttempt attempt;

    @BeforeEach
    void setup() {
        service = new TaskSolutionService(solutions, attempts, tasks);
        exam = new Exam(); exam.setId(1L); exam.setTimeLimit(30); exam.setPassingScore(5);
        task = new ExamTask(); task.setId(20L); task.setExam(exam); task.setMaxScore(10);
        task.setReferenceAnswer("right");
        attempt = new ExamAttempt(); attempt.setId(10L); attempt.setExam(exam);
        attempt.setStatus("IN_PROGRESS"); attempt.setStartTime(LocalDateTime.now().minusMinutes(5));
        when(attempts.findByIdForUpdate(10L)).thenReturn(Optional.of(attempt));
    }

    @Test
    void awardsPointsOnceForCorrectAnswer() {
        when(tasks.findById(20L)).thenReturn(Optional.of(task));
        when(solutions.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals(10, service.submitTaskSolution(10L, 20L, " RIGHT ").getScore());
        verify(solutions).save(any());
    }

    @Test
    void blocksDuplicateSubmission() {
        when(tasks.findById(20L)).thenReturn(Optional.of(task));
        when(solutions.existsByAttemptIdAndTaskId(10L, 20L)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.submitTaskSolution(10L, 20L, "right"));
        verify(solutions, never()).save(any());
    }

    @Test
    void blocksTaskFromOtherExam() {
        Exam other = new Exam(); other.setId(2L); task.setExam(other);
        when(tasks.findById(20L)).thenReturn(Optional.of(task));
        assertThrows(IllegalArgumentException.class, () -> service.submitTaskSolution(10L, 20L, "right"));
        verify(solutions, never()).save(any());
    }

    @Test
    void rejectsAnswerAfterDeadline() {
        attempt.setStartTime(LocalDateTime.now().minusMinutes(31));
        when(tasks.findById(20L)).thenReturn(Optional.of(task));
        assertThrows(IllegalStateException.class, () -> service.submitTaskSolution(10L, 20L, "right"));
        verify(solutions, never()).save(any());
    }

    @Test
    void rejectsAnswerForFinishedAttempt() {
        attempt.setStatus("PASSED");
        when(tasks.findById(20L)).thenReturn(Optional.of(task));
        assertThrows(IllegalStateException.class, () -> service.submitTaskSolution(10L, 20L, "right"));
    }
}
