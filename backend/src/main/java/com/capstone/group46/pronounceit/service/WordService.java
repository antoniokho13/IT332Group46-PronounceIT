package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.LessonEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import com.capstone.group46.pronounceit.entity.WordEntity;
import com.capstone.group46.pronounceit.repository.LessonRepository;
import com.capstone.group46.pronounceit.repository.UserRepository;
import com.capstone.group46.pronounceit.repository.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WordService {
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

    public WordEntity createWord(WordEntity word) {
        // Fetch the LessonEntity from the database
        Long lessonId = word.getLesson().getLessonId();
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson with ID " + lessonId + " not found"));

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
            e.printStackTrace();
            word.setAudioURL(null); // Or a default audio URL
        }

        // Set the creation date
        word.setCreatedDate(LocalDateTime.now());

        // Save the WordEntity
        return wordRepository.save(word);
    }

    public Optional<WordEntity> updateWord(Long wordId, WordEntity updatedWord) {
        return wordRepository.findById(wordId).map(word -> {
            word.setWord(updatedWord.getWord());
            word.setImageURL(updatedWord.getImageURL());
            try {
                byte[] audioContent = textToSpeechService.synthesizeText(word.getWord());
                String audioURL = storeAudio(audioContent, word.getWord());
                word.setAudioURL(audioURL);
            } catch (IOException e) {
                // Handle exception appropriately
                e.printStackTrace();
                word.setAudioURL(null); // Or a default audio URL
            }
            return wordRepository.save(word);
        });
    }

    public void deleteWord(Long wordId) {
        wordRepository.deleteById(wordId);
    }

    private String storeAudio(byte[] audioContent, String word) throws IOException {
        // Get the resource directory
        File resourceDir = new File("src/main/resources/audio/");

        // Ensure the directory exists
        if (!resourceDir.exists()) {
            resourceDir.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + "_" + word + ".mp3";
        Path filePath = Paths.get(resourceDir.getAbsolutePath(), fileName);

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(audioContent);
        }

        return "/audio/" + fileName; // Return the relative path to access the audio
    }

    public byte[] synthesizeAudioForWord(WordEntity word) throws IOException {
        return textToSpeechService.synthesizeText(word.getWord());
    }

    public String uploadImage(MultipartFile imageFile) throws IOException {
        // Define the directory to store images (relative to the backend folder)
        Path imageDirPath = Paths.get("./uploads/images/"); // Changed from ../ to ./
        File imageDir = imageDirPath.toFile();

        // Create the directory if it doesn't exist
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        // Generate a unique file name
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = imageDirPath.resolve(fileName);

        // Save the file to the directory
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(imageFile.getBytes());
        }

        // Return the relative URL to access the image
        return "/uploads/images/" + fileName;
    }

    public List<WordEntity> getWordsByLessonId(Long lessonId) {
        return wordRepository.findAll().stream()
                .filter(word -> word.getLesson().getLessonId().equals(lessonId))
                .toList();
    }
}