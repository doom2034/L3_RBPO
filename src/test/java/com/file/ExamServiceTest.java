package com.file;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExamServiceTest {
    @Test
    void cannotPublishWithPassingScoreAboveMaximum() {
        ExamRepository exams = mock(ExamRepository.class);
        ExamTaskRepository tasks = mock(ExamTaskRepository.class);
        Exam exam = new Exam(); exam.setId(1L); exam.setTitle("Test");
        exam.setPassingScore(11); exam.setTimeLimit(30); exam.setStatus("DRAFT");
        ExamTask task = new ExamTask(); task.setMaxScore(10); task.setReferenceAnswer("yes");
        when(exams.findById(1L)).thenReturn(Optional.of(exam));
        when(tasks.findByExamId(1L)).thenReturn(List.of(task));
        assertThrows(IllegalStateException.class, () -> new ExamService(exams, tasks).publishExam(1L));
        verify(exams,never()).save(any());
    }

    @Test
    void rejectsInvalidTimeLimit() {
        ExamRepository exams = mock(ExamRepository.class);
        Exam exam = new Exam(); exam.setTitle("Test"); exam.setPassingScore(1); exam.setTimeLimit(0);
        assertThrows(IllegalArgumentException.class,
            () -> new ExamService(exams, mock(ExamTaskRepository.class)).createExam(exam));
        verify(exams, never()).save(any());
    }
}
