package Sulekhaai.WHBRD.controller;

import Sulekhaai.WHBRD.model.UserActionLog;
import Sulekhaai.WHBRD.repository.UserActionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class UserActionLogController {
    @Autowired
    private UserActionLogRepository logRepo;

    @PostMapping
    public UserActionLog logAction(@RequestBody Map<String, String> payload, Principal principal) {
        UserActionLog log = new UserActionLog();
        // If userId is provided, use it; otherwise, try to extract from principal (if JWT is set up)
        if (payload.containsKey("userId")) {
            log.setUserId(Long.parseLong(payload.get("userId")));
        } else if (principal != null) {
            try {
                log.setUserId(Long.parseLong(principal.getName()));
            } catch (Exception ignored) {}
        }
        log.setAction(payload.getOrDefault("action", "unknown"));
        log.setDetails(payload.getOrDefault("details", ""));
        log.setTimestamp(LocalDateTime.now());
        return logRepo.save(log);
    }
} 