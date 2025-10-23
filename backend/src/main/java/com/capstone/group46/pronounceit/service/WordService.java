package com.capstone.group46.pronounceit.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files; // <-- ADD IMPORT
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ADD IMPORTS
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- ADD IMPORT
import org.springframework.web.multipart.MultipartFile;

import com.capstone.group46.pronounceit.entity.LessonEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import com.capstone.group46.pronounceit.entity.WordEntity;
import com.capstone.group46.pronounceit.repository.LessonRepository;
import com.capstone.group46.pronounceit.repository.UserRepository;
import com.capstone.group46.pronounceit.repository.WordRepository;

import jakarta.persistence.EntityNotFoundException; // <-- ADD IMPORT

@Service
public class WordService {

    // ADD LOGGER AND PATH CONSTANTS
    private static final Logger logger = LoggerFactory.getLogger(WordService.class);
    private final Path audioBaseDir = Paths.get("/app/uploads/audio");
    private final Path imageBaseDir = Paths.get("/app/uploads/images");

    private final WordRepository wordRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final TextToSpeechService textToSpeechService;

    public WordService(WordRepository wordRepository, LessonRepository lessonRepository, UserRepository userRepository, TextToSpeechService textToSpeechService) {
        this.wordRepository = wordRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.textToSpeechService = textToSpeechService;
    }

    public List<WordEntity> getAllWords() {
        return wordRepository.findAll();
    }

    public Optional<WordEntity> getWordById(Long wordId) {
        return wordRepository.findById(wordId);
    }

    @Transactional // ADD ANNOTATION
    public WordEntity createWord(WordEntity word) {
        // Fetch the LessonEntity from the database
        Long lessonId = word.getLesson().getLessonId();
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson with ID " + lessonId + " not found"));

        // Check the current number of words for this lesson
        long wordCount = wordRepository.findAll().stream()
                .filter(w -> w.getLesson().getLessonId().equals(lessonId))
                .count();

        if (wordCount >= lesson.getSequence()) {
            throw new IllegalStateException("Word limit reached for this lesson.");
        }

        // Fetch the UserEntity from the database
        Long userId = word.getCreatedBy().getId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        // Set the managed entities
        word.setLesson(lesson);
        word.setCreatedBy(user);

        // Generate audio for the word using Text-to-Speech
        try {
            byte[] audioContent = textToSpeechService.synthesizeText(word.getWord());
            String audioURL = storeAudio(audioContent, word.getWord());
            word.setAudioURL(audioURL);
        } catch (IOException e) {
            // Handle exception appropriately (e.g., log it and set a default audio URL or null)
            logger.error("Error creating audio for word: {}", word.getWord(), e);
            word.setAudioURL(null); // Or a default audio URL
        }

        // Set the creation date
        word.setCreatedDate(LocalDateTime.now());

        // Save the WordEntity
        return wordRepository.save(word);
    }

    @Transactional // ADD ANNOTATION
    public Optional<WordEntity> updateWord(Long wordId, WordEntity updatedWord) {
        return wordRepository.findById(wordId).map(word -> {
            word.setWord(updatedWord.getWord());
            word.setImageURL(updatedWord.getImageURL());
            try {
                // Clean up old files before adding new ones
                deleteFileFromServer(word.getAudioURL(), audioBaseDir);

                byte[] audioContent = textToSpeechService.synthesizeText(word.getWord());
                String audioURL = storeAudio(audioContent, word.getWord());
                word.setAudioURL(audioURL);
            } catch (IOException e) {
                // Handle exception appropriately
                logger.error("Error updating audio for word: {}", word.getWord(), e);
                word.setAudioURL(null); // Or a default audio URL
            }
            return wordRepository.save(word);
        });
    }

    @Transactional // ADD ANNOTATION
    public Optional<WordEntity> updateWord(Long wordId, WordEntity updatedWord, MultipartFile imageFile) {
        return wordRepository.findById(wordId).map(word -> {
            // Check if the word has been updated
            boolean isWordUpdated = !word.getWord().equals(updatedWord.getWord());

            // Update the word field
            word.setWord(updatedWord.getWord());

            // Update the image if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    // Delete old image if it exists
                    deleteFileFromServer(word.getImageURL(), imageBaseDir);

                    String imageUrl = uploadImage(imageFile);
                    word.setImageURL(imageUrl);
                } catch (IOException e) {
                    logger.error("Error uploading new image for wordId: {}", wordId, e);
                }
            }

            // Generate new audio only if the word has been updated
            if (isWordUpdated) {
                try {
                    // Delete old audio
                    deleteFileFromServer(word.getAudioURL(), audioBaseDir);

                    byte[] audioContent = textToSpeechService.synthesizeText(word.getWord());
                    String audioURL = storeAudio(audioContent, word.getWord());
                    word.setAudioURL(audioURL);
                } catch (IOException e) {
                    logger.error("Error updating audio for wordId: {}", wordId, e);
                    word.setAudioURL(null); // Or retain the existing audio URL
                }
            }

            return wordRepository.save(word);
        });
    }

    // --- REPLACE THIS ENTIRE METHOD ---
    @Transactional // ADD ANNOTATION
    public void deleteWord(Long wordId) {
        // 1. Find the word entity first to get file paths
        WordEntity word = wordRepository.findById(wordId)
                .orElseThrow(() -> new EntityNotFoundException("Word not found with ID: " + wordId));

        // 2. Get file paths *before* deleting the entity
        String imageURL = word.getImageURL();
        String audioURL = word.getAudioURL();

        // 3. Delete the entity from the database
        // This will cascade and delete child PronounciationAttemptEntity records
        wordRepository.delete(word);
        logger.info("Database record deleted for wordId: {}. Now deleting files.", wordId);

        // 4. If DB deletion is successful, delete the files from the server
        deleteFileFromServer(imageURL, imageBaseDir);
        deleteFileFromServer(audioURL, audioBaseDir);
    }
    // --- END OF REPLACED METHOD ---


    // --- ADD THIS NEW HELPER METHOD ---
    private void deleteFileFromServer(String fileUrl, Path baseDir) {
        if (fileUrl == null || fileUrl.isBlank()) {
            logger.warn("File URL is null or blank, skipping delete.");
            return; // No file to delete
        }

        try {
            // Extract filename from URL (e.g., "/images/filename.jpg" -> "filename.jpg")
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            if (fileName.isBlank()) {
                logger.warn("Could not extract filename from URL: {}", fileUrl);
                return;
            }

            Path filePath = baseDir.resolve(fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("Successfully deleted file: {}", filePath);
            } else {
                logger.warn("File to delete not found: {}", filePath);
            }
        } catch (Exception e) {
            // Log the error but don't stop the process
            logger.error("Error deleting file: {} from directory {}. Error: {}", fileUrl, baseDir, e.getMessage());
        }
    }
    // --- END OF NEW HELPER METHOD ---


    // --- UPDATE storeAudio TO USE PATH CONSTANTS ---
    private String storeAudio(byte[] audioContent, String word) throws IOException {
        File audioDir = audioBaseDir.toFile(); // Use constant
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + "_" + word.replaceAll("\\s+", "_") + ".mp3";
        Path filePath = audioBaseDir.resolve(fileName); // Use constant

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(audioContent);
        }

        logger.info("Audio file stored at: {}", filePath);
        return "/audio/" + fileName;
    }
    // --- END OF storeAudio UPDATE ---


    public byte[] synthesizeAudioForWord(WordEntity word) throws IOException {
        return textToSpeechService.synthesizeText(word.getWord());
    }

    // --- UPDATE uploadImage TO USE PATH CONSTANTS ---
    public String uploadImage(MultipartFile imageFile) throws IOException {
        File imageDir = imageBaseDir.toFile(); // Use constant
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = imageBaseDir.resolve(fileName); // Use constant

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(imageFile.getBytes());
        }

        logger.info("Image file stored at: {}", filePath);
        return "/images/" + fileName;
    }
    // --- END OF uploadImage UPDATE ---


    public List<WordEntity> getWordsByLessonId(Long lessonId) {
        return wordRepository.findAll().stream()
                .filter(word -> word.getLesson().getLessonId().equals(lessonId))
                .toList();
    }
}