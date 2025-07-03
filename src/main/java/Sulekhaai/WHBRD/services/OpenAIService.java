package Sulekhaai.WHBRD.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final OkHttpClient client = new OkHttpClient();

    public String getChatCompletion(String prompt) {
        MediaType mediaType = MediaType.parse("application/json");

        String requestBody = "{\n" +
                "  \"model\": \"gpt-3.5-turbo\",\n" +
                "  \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]\n" +
                "}";

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(RequestBody.create(mediaType, requestBody))
                .addHeader("Authorization", "Bearer " + openAiApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return "Error: " + response.message();
            return response.body() != null ? response.body().string() : "No response from OpenAI.";
        } catch (IOException e) {
            return "Error calling OpenAI: " + e.getMessage();
        }
    }
}
