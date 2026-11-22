package com.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskSolutionRepository extends JpaRepository<TaskSolution, Long> {
    List<TaskSolution> findByAttemptId(Long attemptId);
    boolean existsByAttemptIdAndTaskId(Long attemptId, Long taskId);
    Optional<TaskSolution> findByIdAndAttemptId(Long id, Long attemptId);
}
