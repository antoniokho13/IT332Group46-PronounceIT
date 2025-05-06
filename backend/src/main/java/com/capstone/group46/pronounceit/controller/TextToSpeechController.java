package com.capstone.group46.pronounceit.controller; // Use your actual package

import com.capstone.group46.pronounceit.service.TextToSpeechService; // Import your service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController // Marks this class as a REST Controller
@RequestMapping("/api/tts") // Sets the base path for this controller's endpoints
public class TextToSpeechController {

    private final TextToSpeechService textToSpeechService; // Use final and constructor injection

    // @Autowired // Autowired on constructor is preferred
    public TextToSpeechController(TextToSpeechService textToSpeechService) {
        this.textToSpeechService = textToSpeechService;
    }

    /**
     * Handles POST requests to /api/tts to synthesize text.
     * @param text The text to synthesize, provided in the request body.
     * @return A ResponseEntity containing the audio data as a byte array and appropriate headers.
     */
    @PostMapping // Maps POST requests to /api/tts
    public ResponseEntity<byte[]> synthesize(@RequestBody String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        try {
            byte[] audioContent = textToSpeechService.synthesizeText(text);

            HttpHeaders headers = new HttpHeaders();
            // Set the correct Content-Type based on the AudioEncoding used in the service (MP3 in this case)
            headers.setContentType(MediaType.valueOf("audio/mpeg")); // Use audio/mpeg for MP3
            headers.setContentLength(audioContent.length); // Optional but good practice

            // Return the audio content with OK status and headers
            return new ResponseEntity<>(audioContent, headers, HttpStatus.OK);

        } catch (IOException e) {
            // Log the exception properly in a real application
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}