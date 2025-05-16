package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.WordEntity;
import com.capstone.group46.pronounceit.service.SpeechToTextService;
import com.capstone.group46.pronounceit.service.WordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.speech.v1.RecognitionConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/words")
public class WordController {
    private final WordService wordService;
    private final SpeechToTextService speechToTextService; // Declare the new service

    // Update the constructor to include the new service
    public WordController(WordService wordService, SpeechToTextService speechToTextService) {
        this.wordService = wordService;
        this.speechToTextService = speechToTextService; // Initialize the new service
    }

    @GetMapping("/{wordId}")
    public ResponseEntity<?> getWordById(@PathVariable Long wordId) {
        Optional<WordEntity> wordOptional = wordService.getWordById(wordId);

        if (wordOptional.isPresent()) {
            WordEntity word = wordOptional.get();
            String audioURL = word.getAudioURL();

            if (audioURL != null && !audioURL.isEmpty()) {
                try {
                    Path audioPath = Paths.get("src", "main", "resources", "audio", audioURL.substring(7));
                    FileSystemResource fileSystemResource = new FileSystemResource(audioPath);

                    if (fileSystemResource.exists()) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
                        headers.setContentLength(fileSystemResource.contentLength());

                        return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>("Audio file not found", HttpStatus.NOT_FOUND);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error serving audio file");
                }
            } else {
                return new ResponseEntity<>("Audio URL is not set for this word", HttpStatus.NOT_FOUND);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<WordEntity>> getAllWords() {
        List<WordEntity> words = wordService.getAllWords();
        return ResponseEntity.ok(words);
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<WordEntity>> getWordsByLessonId(@PathVariable Long lessonId) {
        List<WordEntity> words = wordService.getWordsByLessonId(lessonId);
        return ResponseEntity.ok(words);
    }

    @PostMapping
    public WordEntity createWord(@RequestBody WordEntity word) {
        return wordService.createWord(word);
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public WordEntity createWord(
            @RequestPart("word") String wordJson,
            @RequestPart("image") MultipartFile imageFile) throws IOException {
        // Convert the JSON string to a WordEntity object
        ObjectMapper objectMapper = new ObjectMapper();
        WordEntity word = objectMapper.readValue(wordJson, WordEntity.class);

        // Upload the image and set the image URL
        String imageUrl = wordService.uploadImage(imageFile);
        word.setImageURL(imageUrl);

        // Save the WordEntity
        return wordService.createWord(word);
    }

    @PutMapping("/{wordId}")
    public ResponseEntity<WordEntity> updateWord(@PathVariable Long wordId, @RequestBody WordEntity updatedWord) {
        return wordService.updateWord(wordId, updatedWord)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{wordId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WordEntity> updateWord(
            @PathVariable Long wordId,
            @RequestPart("word") String wordJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WordEntity updatedWord = objectMapper.readValue(wordJson, WordEntity.class);

            Optional<WordEntity> updatedEntity = wordService.updateWord(wordId, updatedWord, imageFile);
            return updatedEntity.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{wordId}")
    public ResponseEntity<Void> deleteWord(@PathVariable Long wordId) {
        wordService.deleteWord(wordId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/audio/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<FileSystemResource> getAudioFile(@PathVariable String filename) throws IOException {
        try {
            Path audioPath = Paths.get("src", "main", "resources", "audio", filename);
            FileSystemResource fileSystemResource = new FileSystemResource(audioPath);

            if (!fileSystemResource.exists()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .body(fileSystemResource);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * Endpoint to receive user's pronunciation audio and compare it to the target word.
     * The frontend should send the audio file as multipart/form-data with the parameter name "audio".
     * @param wordId The ID of the word the user is pronouncing.
     * @param audioFile The audio recording from the user.
     * @return A response indicating if the pronunciation was correct and providing feedback.
     */
    @PostMapping("/{wordId}/check-pronunciation")
    public ResponseEntity<?> checkPronunciation(@PathVariable Long wordId,
                                                @RequestParam("audio") MultipartFile audioFile) {
        // 1. Fetch the correct text of the word
        Optional<WordEntity> wordOptional = wordService.getWordById(wordId);
        if (!wordOptional.isPresent()) {
            return new ResponseEntity<>("Word not found", HttpStatus.NOT_FOUND);
        }
        String correctWordText = wordOptional.get().getWord(); // Use getWord() to get the text of the word

        // 2. Receive and process the audio file
        if (audioFile.isEmpty()) {
            return new ResponseEntity<>("Audio file is empty", HttpStatus.BAD_REQUEST);
        }

        try {
            byte[] audioBytes = audioFile.getBytes();

            // --- Crucial Configuration for STT ---
            // You MUST know the exact audio encoding and sample rate used by your
            // frontend when recording. This is vital for accurate transcription.
            // Examples:
            // For LINEAR16 (WAV), sample rate is typically 16000 Hz for speech.
            // For MP3, sample rates can vary (e.g., 8000, 16000, 44100 Hz).
            //
            // Adjust these parameters based on your frontend's audio recording implementation:
            RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.MP3; // Or LINEAR16, OGG_OPUS, etc.
            int sampleRateHertz = 16000; // Replace with the actual sample rate from your frontend

            // You might add logic to inspect the audio file headers if necessary,
            // but it's often simpler to standardize the frontend recording format.


            // 3. Send audio to Speech-to-Text Service for transcription
            // Use the transcribeAudio method or a helper specific to your encoding
            String transcribedText = speechToTextService.transcribeAudio(audioBytes, "en-US", encoding, sampleRateHertz);
            // Consider supporting other languages if needed

            // 4. Compare the transcribed text with the correct word text
            boolean isCorrect = false;
            String feedbackMessage;

            if (transcribedText != null && !transcribedText.trim().isEmpty()) {
                // Perform a case-insensitive and trimmed comparison
                if (correctWordText.trim().equalsIgnoreCase(transcribedText.trim())) {
                    isCorrect = true;
                    feedbackMessage = "Correct!";
                } else {
                    // Provide feedback including what was transcribed
                    feedbackMessage = "Try again. You said: \"" + transcribedText + "\"";
                }
            } else {
                feedbackMessage = "Could not understand. Please try again.";
            }

            // 5. Return result to frontend
            // Use the inner class defined below for the response body
            return new ResponseEntity<>(new PronunciationCheckResponse(isCorrect, feedbackMessage, transcribedText), HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace(); // Log the file processing error
            return new ResponseEntity<>("Error reading audio file.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // Catch potential errors from the Google Cloud STT call
            e.printStackTrace(); // Log the transcription error
            return new ResponseEntity<>("Error during transcription.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Inner class to define the response structure for the frontend
    public static class PronunciationCheckResponse {
        private boolean correct;
        private String feedbackMessage;
        private String transcribedText; // Include transcribed text for user feedback

        // Constructor
        public PronunciationCheckResponse(boolean correct, String feedbackMessage, String transcribedText) {
            this.correct = correct;
            this.feedbackMessage = feedbackMessage;
            this.transcribedText = transcribedText;
        }

        // Getters (needed for Spring to serialize to JSON)
        public boolean isCorrect() {
            return correct;
        }

        public String getFeedbackMessage() {
            return feedbackMessage;
        }

        public String getTranscribedText() {
            return transcribedText;
        }
    }
}