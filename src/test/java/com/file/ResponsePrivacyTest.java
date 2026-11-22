package com.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResponsePrivacyTest {
    @Test
    void taskResponseNeverContainsReferenceAnswer() throws Exception {
        Exam exam = new Exam(); exam.setId(1L);
        ExamTask task = new ExamTask(); task.setId(2L); task.setExam(exam);
        task.setReferenceAnswer("SECRET_ANSWER"); task.setDescription("question"); task.setMaxScore(5);
        String json = new ObjectMapper().writeValueAsString(ExamTaskResponse.from(task));
        assertFalse(json.contains("SECRET_ANSWER"));
        assertFalse(json.contains("referenceAnswer"));
    }
}
