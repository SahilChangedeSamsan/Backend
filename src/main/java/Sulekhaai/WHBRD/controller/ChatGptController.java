package Sulekhaai.WHBRD.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import Sulekhaai.WHBRD.service.OpenAIService;

import java.util.Map; // ✅ Add this
import java.util.HashMap; // ✅ Optional, if used

@RestController
public class ChatGptController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");
        String reply = openAIService.getChatCompletion(prompt);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
