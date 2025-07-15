package Sulekhaai.WHBRD.controller;

import Sulekhaai.WHBRD.Websocket.ImagePushService;
import Sulekhaai.WHBRD.services.RecordingService;
import Sulekhaai.WHBRD.model.RecordingSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/proxy")
public class StreamController {

    @Autowired
    private ImagePushService imagePushService;

    @Autowired
    private RecordingService recordingService;

    // In-memory image store
    private final Map<String, byte[]> latestImageMap = new ConcurrentHashMap<>();
    // In-memory session tracking: cameraId -> sessionId
    private final Map<String, Long> activeSessionMap = new ConcurrentHashMap<>();

    /**
     * Start a new recording session for a user/camera
     */
    @PostMapping("/start_session")
    public Map<String, Object> startSession(@RequestBody Map<String, Object> req) {
        Long userId = Long.parseLong(req.get("userId").toString());
        String cameraId = req.get("cameraId").toString();
        RecordingSession session = recordingService.startSession(userId, cameraId);
        activeSessionMap.put(cameraId, session.getId());
        return Map.of("success", true, "sessionId", session.getId());
    }

    /**
     * End a recording session and generate video
     */
    @PostMapping("/end_session")
    public Map<String, Object> endSession(@RequestBody Map<String, Object> req) throws Exception {
        String cameraId = req.get("cameraId").toString();
        Long sessionId = activeSessionMap.get(cameraId);
        if (sessionId == null) return Map.of("success", false, "message", "No active session");
        recordingService.endSession(sessionId);
        activeSessionMap.remove(cameraId);
        return Map.of("success", true, "sessionId", sessionId);
    }

    /**
     * Endpoint to receive image from Raspberry Pi script
     * Python sends { code: cameraId, image: imageFile }
     */
    @PostMapping(value = "/stream", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> receiveImage(
            @RequestPart("code") String cameraId,
            @RequestPart("image") MultipartFile imageFile) throws IOException {

        // Convert image to byte array
        byte[] imageBytes = StreamUtils.copyToByteArray(imageFile.getInputStream());

        // Store for GET access
        latestImageMap.put(cameraId, imageBytes);

        // Also push via WebSocket (if applicable)
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        imagePushService.push(cameraId, base64);

        // Save image to session if active
        if (activeSessionMap.containsKey(cameraId)) {
            Long sessionId = activeSessionMap.get(cameraId);
            recordingService.saveImage(sessionId, imageBytes);
        }

        return Map.of(
                "success", true,
                "cameraId", cameraId,
                "message", "Image received and stored"
        );
    }

    /**
     * Endpoint to serve latest image for a given cameraId
     * Frontend hits this: /api/proxy/stream?cameraId=123
     */
    @GetMapping(value = "/stream", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> serveLatestImage(@RequestParam String cameraId) {
        if (!latestImageMap.containsKey(cameraId)) {
            return ResponseEntity.status(404)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .body(("No image found for cameraId: " + cameraId).getBytes());
        }

        byte[] imageBytes = latestImageMap.get(cameraId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageBytes);
    }
}
