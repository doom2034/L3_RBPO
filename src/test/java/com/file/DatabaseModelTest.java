package com.file;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import jakarta.persistence.EntityManager;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:labmodel;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DatabaseModelTest {
    @Autowired EntityManager em;

    @Test void userEmailIsUniqueAtDatabaseLevel() {
        User first = new User(null, "Первый", "duplicate@example.test");
        em.persist(first);
        em.flush();
        User second = new User(null, "Второй", "duplicate@example.test");
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            em.persist(second);
            em.flush();
        });
    }

    @Test void entitiesMappedAndRelationshipsWork() {
        User student = new User(null, "Ученик", "relationship@example.test");
        em.persist(student);
        Exam exam = new Exam();
        exam.setTitle("SQL"); exam.setPassingScore(5); exam.setTimeLimit(30); exam.setStatus("ACTIVE");
        em.persist(exam);
        ExamTask task = new ExamTask();
        task.setExam(exam); task.setDescription("SELECT?"); task.setReferenceAnswer("SELECT"); task.setMaxScore(5);
        em.persist(task);
        ExamAttempt attempt = new ExamAttempt();
        attempt.setExam(exam); attempt.setStudent(student);
        attempt.setStartTime(java.time.LocalDateTime.now()); attempt.setStatus("IN_PROGRESS"); attempt.setFinalScore(0);
        em.persist(attempt);
        TaskSolution solution = new TaskSolution();
        solution.setAttempt(attempt); solution.setTask(task); solution.setStudentAnswer("SELECT"); solution.setScore(5);
        em.persist(solution); em.flush(); em.clear();
        assertThat(em.find(TaskSolution.class, solution.getId()).getAttempt().getStudent().getEmail())
            .isEqualTo("relationship@example.test");
    }
}
