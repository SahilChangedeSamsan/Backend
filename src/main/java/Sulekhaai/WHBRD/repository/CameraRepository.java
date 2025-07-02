package Sulekhaai.WHBRD.repository;

import Sulekhaai.WHBRD.model.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera, Long> {
    // Removed findByUserId(Long userId) because Camera does not have a userId field
    Optional<Camera> findByCameraId(String cameraId);
}
