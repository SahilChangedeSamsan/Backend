package Sulekhaai.WHBRD.controller;

import Sulekhaai.WHBRD.model.Task;
import Sulekhaai.WHBRD.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TaskRepository taskRepo;

    @GetMapping
    public List<Task> getTasks(@RequestParam String email, @RequestParam(required = false) String date) {
        if (date != null && !date.isBlank()) {
            return taskRepo.findByUserEmailAndDate(email, LocalDate.parse(date));
        } else {
            return taskRepo.findByUserEmail(email);
        }
    }

    @PostMapping
    public Task addTask(@RequestBody Task task) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authenticated user: " + auth.getName());
        for (GrantedAuthority authority : auth.getAuthorities()) {
            System.out.println("Authority: " + authority.getAuthority());
        }
        return taskRepo.save(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task updated) {
        Optional<Task> opt = taskRepo.findById(id);
        if (opt.isPresent()) {
            Task t = opt.get();
            t.setText(updated.getText());
            t.setDone(updated.isDone());
            t.setDate(updated.getDate());
            return taskRepo.save(t);
        } else {
            throw new RuntimeException("Task not found");
        }
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskRepo.deleteById(id);
    }
} 