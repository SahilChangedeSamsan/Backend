package Sulekhaai.WHBRD.repository;

import Sulekhaai.WHBRD.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserEmailAndDate(String userEmail, String date);
    List<Task> findByUserEmail(String userEmail);
} 