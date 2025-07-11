package Sulekhaai.WHBRD.repository;

import Sulekhaai.WHBRD.model.MeetingParticipant;
import Sulekhaai.WHBRD.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
    List<MeetingParticipant> findByMeeting(Meeting meeting);
} 