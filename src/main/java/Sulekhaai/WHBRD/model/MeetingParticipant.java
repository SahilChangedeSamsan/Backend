package Sulekhaai.WHBRD.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class MeetingParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @Column(nullable = false)
    private String name;

    private boolean isWhiteboard = false;

    private LocalDateTime joinedAt = LocalDateTime.now();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Meeting getMeeting() { return meeting; }
    public void setMeeting(Meeting meeting) { this.meeting = meeting; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isWhiteboard() { return isWhiteboard; }
    public void setWhiteboard(boolean whiteboard) { isWhiteboard = whiteboard; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
} 