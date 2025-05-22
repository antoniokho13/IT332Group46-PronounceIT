package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.WordEntity;
import com.capstone.group46.pronounceit.service.SpeechToTextService;
import com.capstone.group46.pronounceit.service.WordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.speech.v1.RecognitionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/words")
public class WordController {
    private static final Logger logger = LoggerFactory.getLogger(WordController.class);
    private final WordService wordService;
    private final SpeechToTextService speechToTextService;

    public WordController(WordService wordService, SpeechToTextService speechToTextService) {
        this.wordService = wordService;
        this.speechToTextService = speechToTextService;
    }

    @GetMapping("/{wordId}")
    public ResponseEntity<?> getWordById(@PathVariable Long wordId) {
        logger.info("Fetching word with ID: {}", wordId);
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
                        logger.error("Audio file not found at path: {}", audioPath);
                        return new ResponseEntity<>("Audio file not found", HttpStatus.NOT_FOUND);
                    }
                } catch (IOException e) {
                    logger.error("Error serving audio file for wordId {}: {}", wordId, e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error serving audio file");
                }
            } else {
                logger.warn("Audio URL is not set for wordId: {}", wordId);
                return new ResponseEntity<>("Audio URL is not set for this word", HttpStatus.NOT_FOUND);
            }
        } else {
            logger.warn("Word not found for wordId: {}", wordId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<WordEntity>> getAllWords() {
        logger.info("Fetching all words");
        List<WordEntity> words = wordService.getAllWords();
        return ResponseEntity.ok(words);
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<WordEntity>> getWordsByLessonId(@PathVariable Long lessonId) {
        logger.info("Fetching words for lessonId: {}", lessonId);
        List<WordEntity> words = wordService.getWordsByLessonId(lessonId);
        return ResponseEntity.ok(words);
    }

    @PostMapping
    public ResponseEntity<?> createWord(@RequestBody WordEntity word) {
        logger.info("Creating new word: {}", word.getWord());
        try {
            WordEntity createdWord = wordService.createWord(word);
            return ResponseEntity.ok(createdWord);
        } catch (IllegalStateException e) {
            logger.error("Error creating word: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating word: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating word");
        }
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createWord(
            @RequestPart("word") String wordJson,
            @RequestPart("image") MultipartFile imageFile) throws IOException {
        logger.info("Creating word with image upload");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WordEntity word = objectMapper.readValue(wordJson, WordEntity.class);

            String imageUrl = wordService.uploadImage(imageFile);
            word.setImageURL(imageUrl);

            WordEntity createdWord = wordService.createWord(word);
            return ResponseEntity.ok(createdWord);
        } catch (IllegalStateException e) {
            logger.error("Error creating word with image: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating word with image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating word");
        }
    }

    @PutMapping("/{wordId}")
    public ResponseEntity<WordEntity> updateWord(@PathVariable Long wordId, @RequestBody WordEntity updatedWord) {
        logger.info("Updating word with ID: {}", wordId);
        return wordService.updateWord(wordId, updatedWord)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Word not found for update, wordId: {}", wordId);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping(value = "/{wordId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WordEntity> updateWord(
            @PathVariable Long wordId,
            @RequestPart("word") String wordJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        logger.info("Updating word with ID: {} and image", wordId);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WordEntity updatedWord = objectMapper.readValue(wordJson, WordEntity.class);

            Optional<WordEntity> updatedEntity = wordService.updateWord(wordId, updatedWord, imageFile);
            return updatedEntity.map(ResponseEntity::ok).orElseGet(() -> {
                logger.warn("Word not found for update, wordId: {}", wordId);
                return ResponseEntity.notFound().build();
            });
        } catch (IOException e) {
            logger.error("Error updating word with image, wordId {}: {}", wordId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{wordId}")
    public ResponseEntity<Void> deleteWord(@PathVariable Long wordId) {
        logger.info("Deleting word with ID: {}", wordId);
        wordService.deleteWord(wordId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/audio/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<FileSystemResource> getAudioFile(@PathVariable String filename) throws IOException {
        logger.info("Fetching audio file: {}", filename);
        try {
            Path audioPath = Paths.get("src", "main", "resources", "audio", filename);
            FileSystemResource fileSystemResource = new FileSystemResource(audioPath);

            if (!fileSystemResource.exists()) {
                logger.error("Audio file not found: {}", audioPath);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .body(fileSystemResource);
        } catch (Exception e) {
            logger.error("Error fetching audio file {}: {}", filename, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{wordId}/check-pronunciation")
    public ResponseEntity<?> checkPronunciation(@PathVariable Long wordId,
                                               @RequestParam("audio") MultipartFile audioFile) {
        logger.info("Received pronunciation check request for wordId: {}, audio size: {} bytes", wordId, audioFile.getSize());

        // 1. Fetch the correct text of the word
        Optional<WordEntity> wordOptional = wordService.getWordById(wordId);
        if (!wordOptional.isPresent()) {
            logger.warn("Word not found for wordId: {}", wordId);
            return new ResponseEntity<>("Word not found", HttpStatus.NOT_FOUND);
        }
        String correctWordText = wordOptional.get().getWord();
        logger.debug("Correct word text: {}", correctWordText);

        // 2. Validate audio file
        if (audioFile.isEmpty()) {
            logger.error("Audio file is empty for wordId: {}", wordId);
            return new ResponseEntity<>("Audio file is empty", HttpStatus.BAD_REQUEST);
        }

        try {
            // 3. Convert audio to PCM WAV
            File inputFile = File.createTempFile("audio", ".mp4");
            audioFile.transferTo(inputFile);
            File outputWavFile = convertToPcmWav(inputFile);
            byte[] audioBytes = Files.readAllBytes(outputWavFile.toPath());
            logger.debug("Converted audio to PCM WAV, size: {} bytes", audioBytes.length);

            if (audioBytes.length < 2000) { // ~0.1s at 16kHz mono PCM
                logger.warn("Audio file too short for wordId: {}", wordId);
                return new ResponseEntity<>("Audio too short. Please record longer.", HttpStatus.BAD_REQUEST);
            }

            // 4. Send audio to Speech-to-Text Service
            RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.LINEAR16;
            int sampleRateHertz = 16000;
            String languageCode = "en-US";
            String transcribedText = speechToTextService.transcribeAudio(audioBytes, languageCode, encoding, sampleRateHertz);
            logger.info("Transcribed text: {}", transcribedText);

            // 5. Compare transcribed text with correct word
            boolean isCorrect = false;
            String feedbackMessage;

            if (transcribedText != null && !transcribedText.trim().isEmpty()) {
                if (correctWordText.trim().equalsIgnoreCase(transcribedText.trim())) {
                    isCorrect = true;
                    feedbackMessage = "Correct!";
                } else {
                    feedbackMessage = "Try again. You said: \"" + transcribedText + "\"";
                }
            } else {
                feedbackMessage = "Could not understand. Please try again.";
            }

            // 6. Return result to frontend
            return new ResponseEntity<>(new PronunciationCheckResponse(isCorrect, feedbackMessage, transcribedText), HttpStatus.OK);

        } catch (IOException e) {
            logger.error("Error reading audio file for wordId {}: {}", wordId, e.getMessage(), e);
            return new ResponseEntity<>("Error reading audio file.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Error during transcription for wordId {}: {}", wordId, e.getMessage(), e);
            return new ResponseEntity<>("Error during transcription: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private File convertToPcmWav(File inputFile) throws Exception {
        File outputWavFile = File.createTempFile("converted", ".wav");
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        audio.setChannels(1);
        audio.setSamplingRate(16000);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("wav");
        attrs.setAudioAttributes(audio);

        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(inputFile), outputWavFile, attrs);
        return outputWavFile;
    }

    public static class PronunciationCheckResponse {
        private boolean correct;
        private String feedbackMessage;
        private String transcribedText;

        public PronunciationCheckResponse(boolean correct, String feedbackMessage, String transcribedText) {
            this.correct = correct;
            this.feedbackMessage = feedbackMessage;
            this.transcribedText = transcribedText;
        }

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