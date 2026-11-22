package com.file;

import jakarta.persistence.*;

@Entity
@Table(name = "task_solutions", uniqueConstraints = @UniqueConstraint(name = "uk_solution_attempt_task", columnNames = {"attempt_id", "task_id"}))
public class TaskSolution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private ExamAttempt attempt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private ExamTask task;

    @Column(name = "student_answer", nullable = false)
    private String studentAnswer;

    @Column(nullable = false)
    private Integer score;

    public TaskSolution() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ExamAttempt getAttempt() { return attempt; }
    public void setAttempt(ExamAttempt attempt) { this.attempt = attempt; }
    public ExamTask getTask() { return task; }
    public void setTask(ExamTask task) { this.task = task; }
    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}
