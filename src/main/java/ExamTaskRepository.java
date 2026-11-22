package com.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamTaskRepository extends JpaRepository<ExamTask, Long> {
    List<ExamTask> findByExamId(Long examId);
}
