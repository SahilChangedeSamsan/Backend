package Sulekhaai.WHBRD.controller;

import Sulekhaai.WHBRD.model.RecordingSession;
import Sulekhaai.WHBRD.services.RecordingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recordings")
public class RecordingController {
    @Autowired
    private RecordingService recordingService;

    @GetMapping("/{userId}")
    public List<Map<String, Object>> getRecordings(@PathVariable Long userId) {
        List<RecordingSession> sessions = recordingService.getSessionsForUser(userId);
        return sessions.stream().map(s -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("sessionId", s.getId());
            map.put("cameraId", s.getCameraId());
            map.put("startTime", s.getStartTime());
            map.put("endTime", s.getEndTime());
            map.put("videoPath", s.getVideoPath());
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/video/{sessionId}")
    public ResponseEntity<FileSystemResource> getVideo(@PathVariable Long sessionId) {
        RecordingSession session = recordingService.getSession(sessionId).orElse(null);
        if (session == null || session.getVideoPath() == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(session.getVideoPath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recording_" + sessionId + ".mp4")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
} 