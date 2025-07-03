package Sulekhaai.WHBRD.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_settings")
@Data
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String firstName;
    private String lastName;
    private String mobile;
    private String dob;
    private String gender;
    private String profession;
    private String fatherName;
    private String residency;
    private String orgName;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String phone;
    private String mapLink;
    private String theme;
    private boolean notifyEmail;
    private boolean notifySMS;
    private String timeZone;
    private String dateFormat;
    private boolean twoFA;
    private boolean autoLogout;
    private String storagePref;
    private boolean cloudSyncEnabled;
    private String lastSynced;
    private String syncStatus;
    private String syncFrequency;
}
