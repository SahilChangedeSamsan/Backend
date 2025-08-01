package Sulekhaai.WHBRD.controller;

import Sulekhaai.WHBRD.model.UserEntity;
import Sulekhaai.WHBRD.repository.UserRepository;
import Sulekhaai.WHBRD.services.EmailService;
import Sulekhaai.WHBRD.services.OtpService;
import Sulekhaai.WHBRD.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://192.168.1.63:5173",
        "https://sulekha-aii.netlify.app"
})
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    // ---------------------- SIGNUP ---------------------- //
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");
        String name = req.get("name");
        // Ignore any 'role' field in the request
        // Always set role to ROLE_USER

        if (email == null || password == null || name == null ||
                email.isBlank() || password.isBlank() || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email, name, and password are required"
            ));
        }

        if (userRepo.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "message", "User already exists"
            ));
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setName(name);
        user.setRole("ROLE_USER"); // Always set to ROLE_USER
        user.setCameras(new HashSet<>());
        userRepo.save(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User registered"
        ));
    }

    // ---------------------- LOGIN ---------------------- //
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");

        if (email == null || password == null ||
                email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email and password are required"
            ));
        }

        Optional<UserEntity> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent() && encoder.matches(password, userOpt.get().getPassword())) {
            UserEntity user = userOpt.get();
            System.out.println("[DEBUG] Login successful for: " + user.getEmail() + ", Role: " + user.getRole());
            // Ensure user has a role
            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("ROLE_USER");
                userRepo.save(user);
            }
            String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", token,
                    "role", user.getRole(),
                    "userId", user.getId()
            ));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "success", false,
                "message", "Invalid credentials"
        ));
    }

    // ---------------------- SEND OTP ---------------------- //
    @PostMapping(value = "/send-otp", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email is required"
            ));
        }

        try {
            String otp = otpService.generateOtp(email);
            emailService.sendOtpEmail(email, otp);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "OTP sent to " + email
            ));
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send OTP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to send OTP. Please try again."
            ));
        }
    }

    // ---------------------- VERIFY OTP ---------------------- //
    @PostMapping(value = "/verify-otp", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody(required = false) Map<String, String> req) {
        System.out.println("[DEBUG] /auth/verify-otp called");

        if (req == null || !req.containsKey("email") || !req.containsKey("otp")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email and OTP are required in request body"
            ));
        }

        String email = req.get("email");
        String otp = req.get("otp");

        if (email == null || otp == null || email.isBlank() || otp.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email and OTP must not be blank"
            ));
        }

        boolean isValid = otpService.verifyOtp(email, otp);
        if (isValid) {
            System.out.println("[DEBUG] OTP verified successfully for: " + email);
            Optional<UserEntity> userOpt = userRepo.findByEmail(email);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                System.out.println("[DEBUG] User found: " + user.getEmail() + ", Role: " + user.getRole());
                String token = jwtUtil.generateToken(email, user.getId(), user.getRole());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "OTP verified successfully",
                        "token", token,
                        "role", user.getRole(),
                        "userId", user.getId()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "User not found"
                ));
            }
        } else {
            System.out.println("[DEBUG] OTP verification failed for: " + email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid or expired OTP"
            ));
        }
    }

    // ---------------------- RESET PASSWORD ---------------------- //
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");
        String newPassword = req.get("newPassword");

        if (email == null || otp == null || newPassword == null ||
            email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email, OTP, and new password are required"
            ));
        }

        // Verify OTP
        boolean isValid = otpService.verifyOtp(email, otp);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Invalid or expired OTP"
            ));
        }

        // Find user and update password
        Optional<UserEntity> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "User not found"
            ));
        }
        UserEntity user = userOpt.get();
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Password reset successfully"
        ));
    }

    // --- Get user by ID ---
    @GetMapping("/api/user/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Long id) {
        return userRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // --- Debug endpoint to check current user role ---
    @GetMapping("/debug/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Optional<UserEntity> userOpt = userRepo.findByEmail(email);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                return ResponseEntity.ok(Map.of(
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "userId", user.getId(),
                    "name", user.getName()
                ));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
    }

    // ---------------------- CHANGE PASSWORD ---------------------- //
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String currentPassword = req.get("currentPassword");
        String newPassword = req.get("newPassword");

        if (email == null || currentPassword == null || newPassword == null ||
            email.isBlank() || currentPassword.isBlank() || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email, current password, and new password are required"
            ));
        }

        // Find user
        Optional<UserEntity> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "User not found"
            ));
        }

        UserEntity user = userOpt.get();

        // Verify current password
        if (!encoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Current password is incorrect"
            ));
        }

        // Check if new password is different from current
        if (encoder.matches(newPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "New password must be different from current password"
            ));
        }

        // Update password
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Password changed successfully"
        ));
    }
}
