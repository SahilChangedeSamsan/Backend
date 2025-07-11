package Sulekhaai.WHBRD.controller;

import Sulekhaai.WHBRD.model.Meeting;
import Sulekhaai.WHBRD.model.MeetingParticipant;
import Sulekhaai.WHBRD.repository.MeetingRepository;
import Sulekhaai.WHBRD.repository.MeetingParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.*;

@RestController
@RequestMapping("/api/meeting")
public class MeetingController {
    @Autowired private MeetingRepository meetingRepo;
    @Autowired private MeetingParticipantRepository participantRepo;

    private static final String CODE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 14; // abcd-efgh-ijkl

    private String generateMeetingCode() {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            if (i > 0 && i % 4 == 0 && i < CODE_LENGTH) sb.append('-');
            sb.append(CODE_CHARS.charAt(rnd.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createMeeting(@RequestBody Map<String, String> body) {
        String cameraId = body.get("cameraId");
        String createdBy = body.getOrDefault("createdBy", "");
        if (cameraId == null || cameraId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "cameraId is required"));
        }
        String code = generateMeetingCode();
        Meeting meeting = new Meeting();
        meeting.setMeetingCode(code);
        meeting.setCameraId(cameraId);
        meeting.setCreatedBy(createdBy);
        meetingRepo.save(meeting);
        // Add whiteboard/camera as participant
        MeetingParticipant whiteboard = new MeetingParticipant();
        whiteboard.setMeeting(meeting);
        whiteboard.setName("Whiteboard / Camera");
        whiteboard.setWhiteboard(true);
        participantRepo.save(whiteboard);
        return ResponseEntity.ok(Map.of("meetingCode", code));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinMeeting(@RequestBody Map<String, String> body) {
        String code = body.get("meetingCode");
        String name = body.get("name");
        if (code == null || name == null || code.isBlank() || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "meetingCode and name are required"));
        }
        Optional<Meeting> meetingOpt = meetingRepo.findByMeetingCode(code);
        if (meetingOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Meeting not found"));
        }
        Meeting meeting = meetingOpt.get();
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(meeting);
        participant.setName(name);
        participant.setWhiteboard(false);
        participantRepo.save(participant);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/{meetingCode}")
    public ResponseEntity<?> getMeeting(@PathVariable String meetingCode) {
        Optional<Meeting> meetingOpt = meetingRepo.findByMeetingCode(meetingCode);
        if (meetingOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Meeting not found"));
        }
        Meeting meeting = meetingOpt.get();
        List<MeetingParticipant> participants = participantRepo.findByMeeting(meeting);
        List<Map<String, Object>> participantList = new ArrayList<>();
        for (MeetingParticipant p : participants) {
            participantList.add(Map.of(
                "id", p.getId(),
                "name", p.getName(),
                "isWhiteboard", p.isWhiteboard()
            ));
        }
        return ResponseEntity.ok(Map.of(
            "meetingCode", meeting.getMeetingCode(),
            "cameraId", meeting.getCameraId(),
            "createdBy", meeting.getCreatedBy(),
            "createdAt", meeting.getCreatedAt(),
            "participants", participantList
        ));
    }
} 