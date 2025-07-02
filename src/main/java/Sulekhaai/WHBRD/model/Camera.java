package Sulekhaai.WHBRD.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "camera")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Camera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String cameraId;

    private String deviceName;

    private boolean active;

    private boolean online;

    @ManyToMany
    @JoinTable(
        name = "camera_user_link",
        joinColumns = @JoinColumn(name = "camera_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> users = new HashSet<>();
}
