package Sulekhaai.WHBRD.repository;

import Sulekhaai.WHBRD.model.Camera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera, Long> {

    // Get all cameras linked to a user (ManyToMany)
    List<Camera> findByUsers_Id(Long userId);

    // Find camera by string-based ID
    Optional<Camera> findByCameraId(String cameraId);
}
