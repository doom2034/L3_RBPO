package com.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExamAttemptServiceTest {
    @Mock ExamAttemptRepository attempts;
    @Mock ExamRepository exams;
    @Mock UserRepository users;
    @Mock TaskSolutionRepository solutions;

    @Test
    void scoresSolutionsAndPassesWithinDeadline() {
        Exam exam = new Exam(); exam.setPassingScore(5); exam.setTimeLimit(30);
        ExamAttempt attempt = new ExamAttempt(); attempt.setId(1L); attempt.setExam(exam);
        attempt.setStatus("IN_PROGRESS"); attempt.setStartTime(LocalDateTime.now().minusMinutes(2));
        TaskSolution answer = new TaskSolution(); answer.setScore(10);
        when(attempts.findByIdForUpdate(1L)).thenReturn(Optional.of(attempt));
        when(solutions.findByAttemptId(1L)).thenReturn(List.of(answer));
        when(attempts.save(any())).thenAnswer(call -> call.getArgument(0));
        ExamAttempt result = new ExamAttemptService(attempts, exams, users, solutions).finishExamAttempt(1L);
        assertEquals("PASSED", result.getStatus());
        assertEquals(10, result.getFinalScore());
        assertNotNull(result.getEndTime());
    }

    @Test
    void expiredAttemptFailsEvenIfEnoughPoints() {
        Exam exam = new Exam(); exam.setPassingScore(5); exam.setTimeLimit(30);
        ExamAttempt attempt = new ExamAttempt(); attempt.setId(1L); attempt.setExam(exam);
        attempt.setStatus("IN_PROGRESS"); attempt.setStartTime(LocalDateTime.now().minusMinutes(31));
        TaskSolution answer = new TaskSolution(); answer.setScore(10);
        when(attempts.findByIdForUpdate(1L)).thenReturn(Optional.of(attempt));
        when(solutions.findByAttemptId(1L)).thenReturn(List.of(answer));
        when(attempts.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals("FAILED", new ExamAttemptService(attempts, exams, users, solutions).finishExamAttempt(1L).getStatus());
    }

    @Test
    void blocksSecondActiveAttempt() {
        Exam exam = new Exam(); exam.setId(5L); exam.setStatus("ACTIVE");
        User user = new User(2L,"A","a@example.org");
        ExamAttempt existing = new ExamAttempt(); existing.setExam(exam);
        existing.setStartTime(LocalDateTime.now().minusMinutes(1));
        when(exams.findById(5L)).thenReturn(Optional.of(exam));
        when(users.findWithLockById(2L)).thenReturn(Optional.of(user));
        when(attempts.findByStudentIdAndStatus(2L, "IN_PROGRESS")).thenReturn(List.of(existing));
        assertThrows(IllegalStateException.class,
            () -> new ExamAttemptService(attempts, exams, users, solutions).startExamAttempt(5L,2L));
        verify(attempts,never()).save(any());
    }
}
