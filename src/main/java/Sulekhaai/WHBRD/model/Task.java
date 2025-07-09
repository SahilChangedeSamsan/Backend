package Sulekhaai.WHBRD.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;
    private String text;
    private String date; // ISO yyyy-MM-dd
    private boolean done;
} 