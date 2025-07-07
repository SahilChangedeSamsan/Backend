package Sulekhaai.WHBRD.controller;

import Sulekhaai.WHBRD.model.UserEntity;
import Sulekhaai.WHBRD.repository.UserRepository;
import Sulekhaai.WHBRD.services.EmailService;
import Sulekhaai.WHBRD.services.OtpService;
import Sulekhaai.WHBRD.util.JwtUtil;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://192.168.1.63:5173",
    "https://sulekha-ai.netlify.app" // ✅ Add production Netlify frontend
})
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private OtpService otpService;
    @Autowired private EmailService emailService;

    // ---------------------- SIGNUP ---------------------- //
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");
        String name = req.get("name");

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

        // ✅ Generate OTP secret using Google Authenticator
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String otpSecret = key.getKey();
        String otpUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL("SulekhaAI", email, key);

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setName(name);
        user.setRole("ROLE_USER");
        user.setCameras(new HashSet<>());
        user.setOtpSecret(otpSecret); // Store secret in DB
        userRepo.save(user);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "User registered successfully",
            "otpQr", otpUrl  // Can be shown to user on frontend
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
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password matched. Awaiting OTP verification",
                "requiresOtp", true,
                "userId", user.getId(),
                "email", user.getEmail()
            ));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "success", false,
            "message", "Invalid credentials"
        ));
    }

    // ---------------------- VERIFY OTP ---------------------- //
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");

        if (email == null || otp == null || email.isBlank() || otp.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email and OTP must not be blank"
            ));
        }

        Optional<UserEntity> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "User not found"
            ));
        }

        UserEntity user = userOpt.get();
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean isValid = gAuth.authorize(user.getOtpSecret(), Integer.parseInt(otp));

        if (isValid) {
            String token = jwtUtil.generateToken(email, user.getId(), user.getRole());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP verified successfully",
                "token", token,
                "role", user.getRole(),
                "userId", user.getId()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Invalid OTP"
            ));
        }
    }
}
